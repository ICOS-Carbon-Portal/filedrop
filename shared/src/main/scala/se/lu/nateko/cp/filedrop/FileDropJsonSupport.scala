package se.lu.nateko.cp.filedrop

import play.api.libs.json.Json

trait FileDropJsonSupport {

	implicit val fileInfoFormat = Json.format[FileInfo]
}
