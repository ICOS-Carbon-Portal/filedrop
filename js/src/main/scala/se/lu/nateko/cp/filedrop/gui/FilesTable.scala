package se.lu.nateko.cp.filedrop.gui

import se.lu.nateko.cp.filedrop.FileInfo
import scalatags.JsDom.all._

class FilesTable(info: Seq[FileInfo]) {

	def elem = {
		div(cls := "panel panel-default")(
			div(cls := "panel-body")(
				if(info.size > 0) filesTable(info)
				else DropArea.jumboInfo("No files at the moment")
			)
		)
	}

	private def filesTable(files: Seq[FileInfo]) =
		table(cls := "table")(
			caption("Your files are listed below. No operations are supported at the moment."),
			thead(tr(th("#"), th("File name"), th("File size (bytes)"))),
			tbody(
				for((file, i) <- info.zipWithIndex) yield tr(
					th(i + 1), td(file.name), td(file.size)
				)
			)
		)
}
