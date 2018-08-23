#!/usr/bin/env bash

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
git cd -

echo "[RELEASE SUCCESSFUL]"
