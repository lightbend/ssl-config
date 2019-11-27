// TODO remove when https://github.com/lightbend/mima/issues/422 is fixed
resolvers += Resolver.url(
  "typesafe sbt-plugins",
  url("https://dl.bintray.com/typesafe/sbt-plugins")
)(Resolver.ivyStylePatterns)

addSbtPlugin("com.github.gseitz"     % "sbt-release"         % "1.0.12")
addSbtPlugin("com.typesafe.sbt"      % "sbt-osgi"            % "0.9.5")
addSbtPlugin("org.scalariform"       % "sbt-scalariform"     % "1.8.3")
addSbtPlugin("com.typesafe.sbt"      % "sbt-site"            % "1.4.0")
addSbtPlugin("de.heikoseeberger"     % "sbt-header"          % "5.3.1")
addSbtPlugin("org.xerial.sbt"        % "sbt-sonatype"        % "3.8.1")
addSbtPlugin("com.jsuereth"          % "sbt-pgp"             % "2.0.0")
addSbtPlugin("com.typesafe"          % "sbt-mima-plugin"     % "0.6.1")
addSbtPlugin("com.lightbend.paradox" % "sbt-paradox"         % "0.6.7")
