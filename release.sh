#!/usr/bin/env bash

# Clean up the directory of local changes so that we can be sure
# the HEAD from release branch is used to run the release.
(read -p "The working directory will now be cleaned from all non-tracked files. Are you sure you want this? " x; test "$x" = yes) || fail "bailing out"
git clean -fxd || fail "cannot git clean -fxd"

echo "Starting release. You will be prompt to confirm the version and push changes at the end of the process"
sbt 'release cross'

# Get the latest tag which is the one created above
version=$(git tag --sort=-taggerdate | head -1)

echo "Publishing documentation for version $version"
sbt makeSite
mv -v documentation/target/site ../releasing-the-docs

git checkout gh-pages
rm -rf *
mv ../releasing-the-docs docs
mv docs/* .
rm -rf docs
touch .nojekyll
git add .
git commit -m "Releasing docs for version $version"
git push origin gh-pages

echo "Docs where published. Getting back to release branch."
git checkout -

echo "[RELEASE SUCCESSFUL]"
echo Make sure to update mimaPreviousArtifacts
