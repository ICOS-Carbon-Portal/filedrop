package se.lu.nateko.cp.filedrop.gui

import org.scalajs.dom.raw.DragEvent
import scalatags.JsDom.all._
import org.scalajs.dom.raw.FileList
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success

class DropArea(fileSender: FileList => Future[Unit]){

	private val messageHolder = div(cls := "panel-body").render
	private[this] var isUploading = false

	val elem = {

		val dropHandler = (ev: DragEvent) => {
			ev.preventDefault()

			if(!isUploading){
				val files = ev.dataTransfer.files

				if(files.length > 0){
					val size = (0 until files.length).map(files(_).size).sum

					changeMsg(s"Uploading $size bytes...")
					isUploading = true

					fileSender(files).onComplete{
						case Success(_) =>
							switchToReady()
						case Failure(err) =>
							changeMsg(err.getMessage)
					}
				}
			}
		}

		div(
			cls := "panel panel-default",
			ondragover := {(ev: DragEvent) => ev.preventDefault()},
			ondrop := dropHandler
		)(messageHolder).render
	}

	private def changeMsg(msg: String): Unit = {
		messageHolder.innerHTML = ""
		messageHolder.appendChild(DropArea.jumboInfo(msg).render)
	}

	def switchToReady(): Unit = {
		changeMsg(s"Drag and drop files here with a mouse")
		isUploading = false
	}

	switchToReady()
}

object DropArea{

	def jumboInfo(msg: String) =
		div(cls := "jumbotron", marginBottom := 0)(
			h1(cls := "text-center")(small(msg))
		)

}
