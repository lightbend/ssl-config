#!/usr/bin/env bash

sbt ++${TRAVIS_SCALA_VERSION} validateCode
sbt ++${TRAVIS_SCALA_VERSION} test
sbt ++${TRAVIS_SCALA_VERSION} doc
sbt ++${TRAVIS_SCALA_VERSION} mimaReportBinaryIssues