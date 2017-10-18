package se.lu.nateko.cp.filedrop.gui

import org.scalajs.dom.raw.DragEvent
import scalatags.JsDom.all._
import org.scalajs.dom.raw.FileList

class DropArea(fileSender: FileList => Unit){

	def elem = {

		val dropHandler = (ev: DragEvent) => {
			ev.preventDefault()
			fileSender(ev.dataTransfer.files)
		}

		div(
			cls := "panel panel-default",
			ondragover := {(ev: DragEvent) => ev.preventDefault()},
			ondrop := dropHandler
		)(
			div(cls := "panel-body")(
				DropArea.jumboInfo("Drag and drop files here with a mouse")
			)
		)
	}
}

object DropArea{

	def jumboInfo(msg: String) =
		div(cls := "jumbotron", marginBottom := 0)(
			h1(cls := "text-center")(small(msg))
		)

}
