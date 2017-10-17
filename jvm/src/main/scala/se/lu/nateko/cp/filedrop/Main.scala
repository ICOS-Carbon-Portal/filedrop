package se.lu.nateko.cp.filedrop

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import akka.stream.ActorMaterializer

import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json._

import scala.concurrent.ExecutionContext
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.Success
import scala.util.Failure

object Main extends PlayJsonSupport{

	private[this] implicit val pageMarsh = TemplatePageMarshalling.marshaller

	def main(args: Array[String]): Unit = {

		implicit val system = ActorSystem("filedrop")
		import system.dispatcher
		implicit val materializer = ActorMaterializer()

		val config = FiledropConfig.getConfig

		val authRouting = new AuthRouting(config.auth)

		val exceptionHandler = ExceptionHandler{
			case e: Exception =>
				complete((StatusCodes.InternalServerError, e.getMessage))
		}

		def mainPage(development: Boolean) = {
			authRouting.user{uidOpt =>
				complete(views.html.filedrop.FiledropPage(uidOpt, config.auth, development))
			}
		}

		val route = handleExceptions(exceptionHandler){
			get{
				pathSingleSlash(mainPage(false)) ~
				path("develop")(mainPage(true)) ~
				path("buildInfo"){
					complete(BuildInfo.toString)
				} ~
				path("whoami"){
					authRouting.user{uidOpt =>
						val email = uidOpt
							.map(uid => JsString(uid.email))
							.getOrElse(JsNull)
						complete(JsObject("email" -> email :: Nil))
					}
				} ~
				getFromResourceDirectory("")
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

