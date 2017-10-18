package se.lu.nateko.cp.filedrop.gui

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js.URIUtils

import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.ext.AjaxException
import org.scalajs.dom.raw.File
import org.scalajs.dom.raw.FileList
import org.scalajs.dom.raw.XMLHttpRequest

import play.api.libs.json.Json
import se.lu.nateko.cp.filedrop.FileDropJsonSupport
import se.lu.nateko.cp.filedrop.FileInfo

object Backend extends FileDropJsonSupport {


	def getFiles: Future[Seq[FileInfo]] = Ajax
		.get("/drop", withCredentials = true)
		.recoverWith(recovery("fetch file list"))
		.map{xhr =>
			Json.parse(xhr.responseText).as[Seq[FileInfo]]
		}

	def isLoggedIn: Future[Boolean] = Ajax
		.get("/whoami", withCredentials = true)
		.recoverWith(recovery("check if logged in"))
		.map{xhr =>
			(Json.parse(xhr.responseText) \ "email").asOpt[String].isDefined
		}

	def sendFile(file: File): Future[Seq[FileInfo]] = {
		val url = "/drop?filename=" + URIUtils.encodeURIComponent(file.name)
		Ajax.put(url, file, withCredentials = true)
			.recoverWith(recovery("send file"))
			.map{xhr =>
				Json.parse(xhr.responseText).as[Seq[FileInfo]]
			}
	}

	def sendFiles(files: FileList, refresh: Seq[FileInfo] => Unit): Future[Unit] = {
		def sendFromNth(n: Int): Future[Unit] = {
			if(n >= files.length) Future.successful(())
			else sendFile(files(n)).flatMap{latest =>
				refresh(latest)
				sendFromNth(n + 1)
			}
		}
		sendFromNth(0)
	}

	private def recovery(hint: String): PartialFunction[Throwable, Future[XMLHttpRequest]] = {
		case AjaxException(xhr) =>
			val msg = if(xhr.responseText.isEmpty)
				s"Got HTTP status ${xhr.status} when trying to $hint"
			else s"Error when trying to $hint:\n" + xhr.responseText

			Future.failed(new Exception(msg))
	}
}