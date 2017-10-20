package se.lu.nateko.cp.filedrop.gui

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success

import org.scalajs.dom.raw.FileList

import scalatags.JsDom.all._
import se.lu.nateko.cp.filedrop.FileInfo
import scala.concurrent.Future

class MainView {

	private val fileSender: FileList => Future[Unit] = Backend.sendFiles(_, updateFileList)

	private val filesContainer = div(cls := "col-md-6")(filesTable(Nil)).render
	private val dropArea = new DropArea(fileSender)

	val elem = div(cls := "container-fluid")(
		div(cls := "row")(
			filesContainer,
			div(cls := "col-md-6")(dropArea.elem)
		)
	)

	private def filesTable(files: Seq[FileInfo]) =
		new FilesTable(files).elem.render

	private val updateFileList: Seq[FileInfo] => Unit = files => {
		filesContainer.innerHTML = ""
		filesContainer.appendChild(filesTable(files))
	}

	Backend.getFiles.onComplete{
		case Success(files) => updateFileList(files)
		case Failure(err) => println(err.getMessage)
	}
}
