package se.lu.nateko.cp.filedrop

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import se.lu.nateko.cp.cpauth.core.PublicAuthConfig

case class FiledropConfig(folder: String, auth: PublicAuthConfig)

object FiledropConfig {

	def getConfig: FiledropConfig = {
		val allConf = getAppConfig
		FiledropConfig(
			folder = allConf.getString("cpfiledrop.folder"),
			auth = getAuthConfig(allConf)
		)
	}

	private def getAppConfig: Config = {
		val default = ConfigFactory.load
		val confFile = new java.io.File("application.conf").getAbsoluteFile
		if(!confFile.exists) default
		else ConfigFactory.parseFile(confFile).withFallback(default)
	}

	private def getAuthConfig(allConf: Config): PublicAuthConfig = {
		val auth = allConf.getConfig("cpauth.auth.pub")
		PublicAuthConfig(
			authCookieName = auth.getString("authCookieName"),
			authCookieDomain = auth.getString("authCookieDomain"),
			cpauthHost = auth.getString("cpauthHost"),
			publicKeyPath = auth.getString("publicKeyPath")
		)
	}
}

