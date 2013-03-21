name := "opensocial"

version := "0.0"

scalaVersion := "2.9.1"

seq(webSettings: _*)

resolvers ++= Seq("Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
              "Sonatype scala-tools repo" at "https://oss.sonatype.org/content/groups/scala-tools/",
              "spray repo" at "http://repo.spray.io",
              "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/")

libraryDependencies ++= Seq("net.liftweb" %% "lift-webkit" % "2.4" % "compile",
                        "org.mortbay.jetty" % "jetty" % "6.1.26" % "container",
                        "ch.qos.logback" % "logback-classic" % "0.9.26",
                        "org.scalatest" %% "scalatest" % "1.8" % "test",
                        "com.google.inject" % "guice" % "3.0",
                        "io.spray" % "spray-client" % "1.0-M7",
                        "com.typesafe.akka" % "akka-actor" % "2.0.5",
                        "com.sun.jersey" % "jersey-core" % "1.17")

