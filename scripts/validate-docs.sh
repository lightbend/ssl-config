#!/usr/bin/env bash

sbt makeSite

markdown_count=$(ls documentation/src/main/paradox/*.md | wc -l)
html_count=$(ls documentation/target/site/*.html | wc -l)

if [ "$markdown_count" == "$html_count" ]; then
    echo "Website generated correctly!"
else
    echo "Documenation was not generated correctly: md = ${markdown_count} html = ${html_count}"
    echo "MARKDOWN FILES:"
    tree documentation/src/main/paradox
    echo "GENERATED HTML FILES:"
    tree documentation/target/site
fi
