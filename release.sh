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

# get the current project version from sbt
# a little messy as the ansi escape codes are included
function get_current_version {
  local result=$(sbt version | grep -v warn | tail -1 | cut -f2)
  # remove ansi escape code from end
  local code0=$(echo -e "\033[0m")
  echo ${result%$code0}
}

(read -p "The working directory will now be cleaned from all non-tracked files. Are you sure you want this? " x; test "$x" = yes) || fail "bailing out"
git clean -fxd || fail "cannot git clean -fxd"

declare -r currentVersion=$(get_current_version)
declare -r version=$1

echo "Updating version.sbt to $version..."
sed -i "s/$currentVersion/$version/" version.sbt
git commit version.sbt -m "Update version to $version"

echo "Publishing artifacts..."
sbt '+publishSigned'

echo "Publishing docs..."
sbt makeSite
rm -rf documentation/target/paradox/paradox # Paradox site bug?
mv documentation/target/paradox ../releasing-the-docs
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
git tag -m "Releasing $version" "v$version"
git push --tags

git checkout master

echo "[RELEASE SUCCESSFUL]"
