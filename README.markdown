
# Requirements

sbt 0.11.2

# Built with

	scalatra, lift-json, scalaquery, angular.js

# Running the sample app

	$ git clone git@github.com:dozed/fink.git
	$ cd fink
	$ cp src/main/resources/fink.properties-default src/main/resources/fink.properties
	$ vi src/main/resources/fink.properties
	$ sbt container:start

The sample application is running on: http://localhost:8080/

The admin panel is reachable under: http://localhost:8080/admin

