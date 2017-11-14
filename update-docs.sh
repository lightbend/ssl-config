#!/bin/bash

sbt parafox:paradox
rm -rf docs
cp -R documentation/target/paradox/paradox/site/paradox docs

echo "New docs generated, please commit and PR the update"
