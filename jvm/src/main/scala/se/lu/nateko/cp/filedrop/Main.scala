package se.lu.nateko.cp.filedrop

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink

import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json._

import scala.concurrent.ExecutionContext
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.Success
import scala.util.Failure

object Main extends PlayJsonSupport with FileDropJsonSupport{

	private[this] implicit val pageMarsh = TemplatePageMarshalling.marshaller

	def main(args: Array[String]): Unit = {

		implicit val system = ActorSystem("filedrop")
		import system.dispatcher
		implicit val materializer = ActorMaterializer()

		val config = FiledropConfig.getConfig

		val authRouting = new AuthRouting(config.auth)
		val service = new FiledropService(config, system.log)

		val exceptionHandler = ExceptionHandler{
			case e: Exception =>
				complete((StatusCodes.InternalServerError, e.getMessage))
		}

		def mainPage(development: Boolean) = {
			authRouting.userOpt{uidOpt =>
				complete(views.html.filedrop.FiledropPage(uidOpt, config.auth, development))
			}
		}

		val route = handleExceptions(exceptionHandler){
			get{
				path("drop"){
					authRouting.userOpt{uidOpt =>
						val files = uidOpt.map(service.getFiles).getOrElse(Nil)
						complete(files)
					}
				} ~
				pathSingleSlash(mainPage(false)) ~
				path("develop")(mainPage(true)) ~
				path("buildInfo"){
					complete(BuildInfo.toString)
				} ~
				path("whoami"){
					authRouting.userOpt{uidOpt =>
						val email = uidOpt
							.map(uid => JsString(uid.email))
							.getOrElse(JsNull)
						complete(JsObject("email" -> email :: Nil))
					}
				} ~
				path("logout"){
					deleteCookie(config.auth.authCookieName, domain = config.auth.authCookieDomain, path = "/"){
						complete(StatusCodes.OK)
					}
				} ~
				getFromResourceDirectory("")
			} ~
			(put & path("drop")){
				authRouting.user{uid =>
					parameter('filename){filename =>
						extractDataBytes{bytes =>
							val sink = service.getSink(uid, filename)
							val doneFut = bytes.runWith(sink)
							onSuccess(doneFut){_ =>
								complete(service.getFiles(uid))
							}
						}
					} ~
					extractRequest{req =>
						req.discardEntityBytes()
						complete(StatusCodes.BadRequest -> "query parameter 'filename' missing in the URL")
					}
				} ~
				extractDataBytes{bytes =>
					bytes.runWith(Sink.cancelled)
					complete(StatusCodes.Unauthorized)
				}
			}
		}

		Http().bindAndHandle(route, "127.0.0.1", port = 8039)
			.onComplete{
				case Success(binding) =>
					sys.addShutdownHook{
						val doneFuture = binding.unbind()
							.flatMap(_ => system.terminate())(ExecutionContext.Implicits.global)
						Await.result(doneFuture, 3 seconds)
					}
					system.log.info(s"Started CP filedrop service: $binding")
				case Failure(err) =>
					system.log.error(err, "Failed to start filedrop service")
			}
	}
}

