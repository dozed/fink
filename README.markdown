
# What is it?

Fink is a light-weight web publishing solution.

At the moment the following content elements are supported:

  * Posts
  * Pages for static content
  * Galleries

# Running the demo

    $ git clone git@github.com:dozed/fink.git
    $ cd fink
    $ vi src/main/resources/fink.properties
    $ vi src/main/resources/c3p0.properties
    $ bin/sbt
    > container:start

The sample application is running on: [http://localhost:8080/](http://localhost:8080/)

The admin panel is reachable under: [http://localhost:8080/admin](http://localhost:8080/admin)

# Built with

  * Web micro-framework [Scalatra](http://scalatra.org/)
  * Database abstraction [Slick](http://slick.typesafe.com/)
  * JSON data interchange format using [json4s](http://json4s.org/)
  * Admin UI [AngularJS](http://angularjs.org/)
  * Jade template using [Scalate](http://scalate.fusesource.org/)
  * The theme for the demo is: [Foghorn](http://wptheming.com/foghorn).

# Useful tools

## html2jade

This handy tool helps converting HTML to Jade syntax.

  * [html2jade](https://github.com/donpark/html2jade)
  * [html_to_jade.py Sublime TextCommand](https://gist.github.com/dozed/a5573c87a953711d12e8)

