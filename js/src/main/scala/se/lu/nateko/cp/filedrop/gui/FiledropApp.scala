package se.lu.nateko.cp.filedrop.gui

import org.scalajs.dom.document
import scalatags.JsDom.all._
import scala.concurrent.ExecutionContext.Implicits.global

object FiledropApp{

	def main(args: Array[String]): Unit = {

		Backend.isLoggedIn.map{loggedIn =>
			if(loggedIn)
				new MainView().elem.render
			else
				errorView("Please log in to use this service").render
		}.recover{
			case err: Throwable =>
				errorView(err.getMessage).render
		}.foreach(
			elem => document.getElementById("main").appendChild(elem)
		)

	}

	def errorView(msg: String) = div(cls := "panel panel-danger")(
		div(cls := "panel-body")(h2(msg))
	)
}
