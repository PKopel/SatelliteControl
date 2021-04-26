name := "SatelliteControl"

version := "0.1"

scalaVersion := "2.13.5"

enablePlugins(SbtSQLSQLite)

jdbcURL := "jdbc:sqlite:satellite.db"

val AkkaVersion = "2.6.14"
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" % Runtime
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.7.2"
libraryDependencies += "com.typesafe.slick" %% "slick" % "3.3.3"
libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3"