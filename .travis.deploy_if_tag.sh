#!/bin/bash
if [ -n "$TRAVIS_TAG" ]; then
  mvn -s .travis.maven.settings.xml -DskipTests clean deploy
  VERSION=${TRAVIS_TAG//[^0-9.]/}
  cp target/elton-app.jar elton_no_head.jar
  cat .travis.jar.magic elton_no_head.jar > elton.jar
fi
