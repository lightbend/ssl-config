// TODO remove when https://github.com/lightbend/mima/issues/422 is fixed
resolvers += Resolver.url(
  "typesafe sbt-plugins",
  url("https://dl.bintray.com/typesafe/sbt-plugins")
)(Resolver.ivyStylePatterns)

addSbtPlugin("com.github.gseitz"     % "sbt-release"         % "1.0.13")
addSbtPlugin("com.typesafe.sbt"      % "sbt-osgi"            % "0.9.6")
addSbtPlugin("org.scalariform"       % "sbt-scalariform"     % "1.8.3")
addSbtPlugin("com.typesafe.sbt"      % "sbt-site"            % "1.4.1")
addSbtPlugin("de.heikoseeberger"     % "sbt-header"          % "5.6.0")
addSbtPlugin("org.xerial.sbt"        % "sbt-sonatype"        % "3.9.7")
addSbtPlugin("com.jsuereth"          % "sbt-pgp"             % "2.0.2")
addSbtPlugin("com.typesafe"          % "sbt-mima-plugin"     % "0.8.1")
addSbtPlugin("com.lightbend.paradox" % "sbt-paradox"         % "0.9.0")
