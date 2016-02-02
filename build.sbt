name := "IMAPMailDownloader"

version := "1.0"

scalaVersion := "2.11.7"

resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Spray Repository"    at "http://repo.spray.io")

libraryDependencies ++= {
  val akkaVersion       = "2.3.9"
  val sprayVersion      = "1.3.2"
  Seq(
    "com.typesafe.akka" %% "akka-actor"      % akkaVersion,
    "io.spray"          %% "spray-can"       % sprayVersion,
    "io.spray"          %% "spray-routing"   % sprayVersion,
    "io.spray"          %% "spray-json"      % "1.3.1",
    "com.typesafe.akka" %% "akka-slf4j"      % akkaVersion,
    "ch.qos.logback"    %  "logback-classic" % "1.1.2",
    "org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka23",
    "com.typesafe.akka" %% "akka-testkit"    % akkaVersion  % "test",
    "io.spray"          %% "spray-testkit"   % sprayVersion % "test",
    "org.specs2"        %% "specs2"          % "2.3.13"     % "test",
    "javax.mail" % "mail" % "1.4.7",
    "commons-httpclient" % "commons-httpclient" % "3.1",
    "oauth.signpost" % "signpost-core" % "1.2.1.2",
    "oauth.signpost" % "signpost-commonshttp4" % "1.2.1.2",
    "com.typesafe.play" % "play-json_2.11" % "2.4.6"
  )
}