#!/usr/bin/env bash

set -e

sbt ++${TRAVIS_SCALA_VERSION} publishM2

# Downloads maven schema file which will be used to validate the generated pom file
wget -c http://maven.apache.org/xsd/maven-4.0.0.xsd

# Use xmllint to validate the generated pom.xml
find . -name *.pom -exec xmllint --noout -schema maven-4.0.0.xsd {} \;
