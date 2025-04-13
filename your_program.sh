#!/bin/sh

set -e # Exit early if any commands fail

(
  cd "$(dirname "$0")"
  mvn -B package -Ddir=/tmp/build-tarik-http-server-java
)

exec java -jar /tmp/build-tarik-http-server-java/tarik-http-server.jar "$@"