package se.lu.nateko.cp.filedrop.gui

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success

import org.scalajs.dom.raw.FileList

import scalatags.JsDom.all._
import se.lu.nateko.cp.filedrop.FileInfo

class MainView {

	private val fileSender: FileList => Unit = Backend.sendFiles(_, updateFileList)

	private var filesTable = new FilesTable(Nil).elem.render
	private val filesContainer = div(cls := "col-md-6")(filesTable).render

	val elem = div(cls := "container-fluid")(
		div(cls := "row")(
			filesContainer,
			div(cls := "col-md-6")(
				new DropArea(fileSender).elem
			)
		)
	)

	private val updateFileList: Seq[FileInfo] => Unit = files => {
		val newFilesTable = new FilesTable(files).elem.render
		filesContainer.replaceChild(newFilesTable, filesTable)
		filesTable = newFilesTable
	}

	Backend.getFiles.onComplete{
		case Success(files) => updateFileList(files)
		case Failure(err) => println(err.getMessage)
	}
}
