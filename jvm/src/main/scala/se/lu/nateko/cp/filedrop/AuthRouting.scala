package se.lu.nateko.cp.filedrop

import se.lu.nateko.cp.cpauth.core.PublicAuthConfig
import akka.http.scaladsl.server.Directives._
import se.lu.nateko.cp.cpauth.core.Authenticator
import akka.http.scaladsl.server.Route
import se.lu.nateko.cp.cpauth.core.UserId
import scala.util.Success
import scala.util.Failure
import se.lu.nateko.cp.cpauth.core.CookieToToken

class AuthRouting(authConfig: PublicAuthConfig) {

	private[this] val authenticator = Authenticator(authConfig).get

	def user(inner: Option[UserId] => Route): Route = cookie(authConfig.authCookieName)(cookie => {
		val tokenTry = for(
			signedToken <- CookieToToken.recoverToken(cookie.value);
			token <- authenticator.unwrapToken(signedToken)
		) yield token

		tokenTry match {
			case Success(token) => inner(Some(token.userId))
			case Failure(err) => inner(None)
		}
	}) ~ inner(None)

}
