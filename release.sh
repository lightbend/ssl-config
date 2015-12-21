#!/bin/bash

# print usage info
function usage {
  cat <<EOM
Usage: ${script_name} VERSION
EOM
}

# fail the script with an error message
function fail {
  echoerr "$@"
  exit 1
}

# echo an error message
function echoerr {
  echo "[${script_name}] $@" 1>&2
}

if [ $# != "1" ]; then
  usage
  fail "A release version must be specified"
fi

(read -p "The working directory will now be cleaned from all non-tracked files. Are you sure you want this? " x; test "$x" = yes) || fail "bailing out"
git clean -fxd || fail "cannot git clean -fxd"

declare -r version=$1

echo "Publishing artifacts..."
sbt '+publishSigned'

echo "Publishing docs..."
sbt makeSite
mv documentation/target/sphinx/html ../releasing-the-docs
git checkout gh-pages
rm -rf *
mv ../releasing-the-docs docs
mv docs/* .
rm -rf docs
touch .nojekyll
git add .
git commit -m "Releasing docs for $version"
git push origin gh-pages

echo "Tagging release..."
git tag -m "Releasing $version" $version
git push --tags

git checkout master

echo "[RELEASE SUCCESSFUL]"