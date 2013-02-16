#!/bin/sh

java -Xmx512M -XX:MaxPermSize=200m -jar bin/sbt-launch.jar "$@"
