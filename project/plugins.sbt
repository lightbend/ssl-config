addSbtPlugin("com.github.gseitz"     % "sbt-release"         % "1.0.6")
addSbtPlugin("com.typesafe.sbt"      % "sbt-native-packager" % "1.2.2")
addSbtPlugin("com.typesafe.sbt"      % "sbt-osgi"            % "0.9.2")
addSbtPlugin("com.typesafe.sbt"      % "sbt-s3"              % "0.9")
addSbtPlugin("com.typesafe.sbt"      % "sbt-scalariform"     % "1.3.0")
addSbtPlugin("com.typesafe.sbt"      % "sbt-site"            % "1.3.1")
addSbtPlugin("com.jsuereth"          % "sbt-pgp"             % "1.1.0")
addSbtPlugin("com.lightbend.paradox" % "sbt-paradox"         % "0.2.13")
addSbtPlugin("com.eed3si9n"          % "sbt-doge"            % "0.1.5")
addSbtPlugin("com.typesafe"          % "sbt-mima-plugin"     % "0.3.0")


// XXX is this really needed since we don't use JDK 1.6 any more?
// why not use https://github.com/sbt/sbt-header ?
addSbtPlugin("com.typesafe.tmp"      % "sbt-header"          % "1.5.0-JDK6-0.1")
