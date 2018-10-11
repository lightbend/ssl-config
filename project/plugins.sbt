resolvers += Resolver.url("sbts3 ivy resolver", url("https://dl.bintray.com/emersonloureiro/sbt-plugins"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.github.gseitz"     % "sbt-release"         % "1.0.9")
addSbtPlugin("com.typesafe.sbt"      % "sbt-native-packager" % "1.3.10")
addSbtPlugin("com.typesafe.sbt"      % "sbt-osgi"            % "0.9.4")
addSbtPlugin("cf.janga"              % "sbts3"               % "0.10.3")
addSbtPlugin("org.scalariform"       % "sbt-scalariform"     % "1.8.2")
addSbtPlugin("com.typesafe.sbt"      % "sbt-site"            % "1.3.2")

addSbtPlugin("de.heikoseeberger"     % "sbt-header"          % "5.0.0")
addSbtPlugin("org.xerial.sbt"        % "sbt-sonatype"        % "2.3")
addSbtPlugin("com.jsuereth"          % "sbt-pgp"             % "1.1.2")
addSbtPlugin("com.typesafe"          % "sbt-mima-plugin"     % "0.3.0")
addSbtPlugin("com.lightbend.paradox" % "sbt-paradox"         % "0.4.2")
