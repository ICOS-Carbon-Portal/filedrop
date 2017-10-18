package se.lu.nateko.cp.filedrop

import java.nio.file.{ Files, Path, Paths }

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure

import akka.Done
import akka.event.LoggingAdapter
import akka.stream.IOResult
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import se.lu.nateko.cp.cpauth.core.UserId

class FiledropService(conf: FiledropConfig, log: LoggingAdapter)(implicit ctxt: ExecutionContext) {

	def getUserFolder(uid: UserId): Path = {
		val dir = Paths.get(conf.folder, uid.email)
		if(!Files.exists(dir)) Files.createDirectory(dir)
		dir
	}

	def getSink(uid: UserId, fileName: String): Sink[ByteString, Future[Done]] = {
		val targetFile = getUserFolder(uid).resolve(fileName)
		FileIO.toPath(targetFile).mapMaterializedValue(postUpload(targetFile))
	}

	private def postUpload(file: Path)(res: Future[IOResult]): Future[Done] = res
		.flatMap(iores => Future.fromTry(iores.status))
		.andThen{
			case Failure(err) =>
				log.error(err, s"Failed uploading file $file")
				Files.deleteIfExists(file)
		}

	def getFiles(uid: UserId): Seq[FileInfo] = {
		val dir = getUserFolder(uid)
		Files.list(dir).toArray((i: Int) => Array.ofDim[Path](i))
			.map(path => FileInfo(name = path.getFileName.toString, isPublic = false))
	}
}
