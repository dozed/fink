
# Requirements

sbt 0.11.2

# Built with

	scalatra, lift-json, neo4j, backbone.js, coffeejade

# Running the sample app

	$ git clone git@github.com:dozed/fink.git
	$ cd fink
	$ sbt container:start

The sample application is running on: http://localhost:8080/

The admin panel is reachable under: http://localhost:8080/admin

# Development notes

## Edit .coffee files

- rake watch

## Use JRebel:

- Download and install JRebel

	java -noverify -javaagent:/usr/local/jrebel/jrebel.jar -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=512M -Xmx3024M -Xss2M -jar /usr/local/sbt-launch.jar "$@"
