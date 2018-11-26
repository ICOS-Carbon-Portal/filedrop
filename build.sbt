scalaVersion in ThisBuild := "2.12.3"
organization in ThisBuild := "se.lu.nateko.cp"

lazy val commonJvmSettings = Seq(
	scalacOptions ++= Seq(
		"-unchecked",
		"-deprecation",
		"-Xlint",
		"-Ywarn-dead-code",
		"-language:_",
		"-target:jvm-1.8",
		"-encoding", "UTF-8"
	)
)

name := "filedrop root project"

lazy val root = project.in(file("."))
	.aggregate(filedropJS, filedropJVM)

lazy val filedrop = crossProject
	.in(file("."))
	.settings(
		name := "filedrop",
		libraryDependencies ++= Seq(
			"com.typesafe.play" %%% "play-json" % "2.6.6"
		)
	)
	.jvmSettings(commonJvmSettings: _*)
	.jsSettings(
		name := "filedrop-js",
		libraryDependencies ++= Seq(
			"com.lihaoyi" %%% "scalatags" % "0.6.7"
		),
		scalaJSUseMainModuleInitializer := true
	)
	.jvmSettings(
		name := "filedrop-jvm",
		libraryDependencies ++= Seq(
			"com.typesafe.akka" %% "akka-http"           % "10.0.10",
			"de.heikoseeberger" %% "akka-http-play-json" % "1.18.0",
			"se.lu.nateko.cp"   %% "views-core"          % "0.4.0-SNAPSHOT",
			"se.lu.nateko.cp"   %% "cpauth-core"         % "0.6.0-SNAPSHOT"
		),
		baseDirectory in reStart := {
			baseDirectory.in(reStart).value.getParentFile
		},
		assemblyMergeStrategy.in(assembly) := {
			case PathList(name) if(name.endsWith("-fastopt.js")) =>
				MergeStrategy.discard
			case x =>
				val originalStrategy = assemblyMergeStrategy.in(assembly).value
				originalStrategy(x)
		}
	)
	.jvmConfigure(_.enablePlugins(SbtTwirl, IcosCpSbtDeployPlugin))

lazy val filedropJS = filedrop.js
lazy val filedropJVM = filedrop.jvm
	.settings(
		cpDeployTarget := "filedrop",
		cpDeployBuildInfoPackage := "se.lu.nateko.cp.filedrop",

		resources.in(Compile) += fastOptJS.in(filedropJS, Compile).value.data,

		watchSources ++= watchSources.in(filedropJS, Compile).value,

		assembledMappings.in(assembly) := {

			val finalJsFile = fullOptJS.in(filedropJS, Compile).value.data

			assembledMappings.in(assembly).value :+ sbtassembly.MappingSet(None, Vector((finalJsFile, finalJsFile.getName)))
		}
	)

