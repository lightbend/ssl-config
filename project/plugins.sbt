// TODO remove when https://github.com/lightbend/mima/issues/422 is fixed
resolvers += Resolver.url(
  "typesafe sbt-plugins",
  url("https://dl.bintray.com/typesafe/sbt-plugins")
)(Resolver.ivyStylePatterns)

addSbtPlugin("com.github.sbt"     % "sbt-release"         % "1.1.0")
addSbtPlugin("com.typesafe.sbt"      % "sbt-osgi"            % "0.9.6")
addSbtPlugin("org.scalariform"       % "sbt-scalariform"     % "1.8.3")
addSbtPlugin("com.typesafe.sbt"      % "sbt-site"            % "1.4.1")
addSbtPlugin("de.heikoseeberger"     % "sbt-header"          % "5.6.0")
addSbtPlugin("org.xerial.sbt"        % "sbt-sonatype"        % "3.9.7")
addSbtPlugin("com.github.sbt"        % "sbt-pgp"             % "2.1.2")
addSbtPlugin("com.typesafe"          % "sbt-mima-plugin"     % "0.9.2")
addSbtPlugin("com.lightbend.paradox" % "sbt-paradox"         % "0.9.2")
