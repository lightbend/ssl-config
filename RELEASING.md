* Make sure you're running jdk8
* Make sure you have gpg configured with a key that is known to the sonatype keyservers
* Make sure you have your sonatype credentials configured in ~/.sbt:

  credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)

* ./release.sh
* git push --tags
* close and release the release staging repo on oss.sonatype.org
* delete the snapshot staging repo on oss.sonatype.org
* create a release in github with a short changelog
