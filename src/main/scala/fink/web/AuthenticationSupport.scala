package fink.web

import fink.data.User
import fink.data.UserRepository

import org.scalatra.auth.{ScentryConfig, ScentrySupport}
import org.scalatra.ScalatraKernel
import org.scalatra.auth.strategy.{BasicAuthStrategy, BasicAuthSupport}
import org.scalatra.auth.{ScentrySupport, ScentryConfig}
import org.scalatra.liftjson.LiftJsonRequestBodyWithoutFormats
import org.scalatra._

import java.net.URL
import scalate.ScalateSupport
import com.codahale.jerkson.Json._
import scala.collection.mutable.ListBuffer

import annotation.tailrec
import util.RicherString._
import java.nio.CharBuffer

/**
 * https://gist.github.com/732347
 * https://gist.github.com/660701
 */
trait AuthenticationSupport extends ScentrySupport[User] { self: ScalatraKernel =>

	val realm = "omnipass"

	protected def fromSession = { case name: String => UserRepository.find(name) getOrElse null  }
	protected def toSession   = { case usr: User => user.name }

	protected val scentryConfig = (new ScentryConfig {}).asInstanceOf[ScentryConfiguration]

	override protected def configureScentry = {
		scentry.unauthenticated {
			scentry.strategies('UserPassword).unauthenticated()
		}
	}

	override protected def registerAuthStrategies = {
		// scentry.registerStrategy('Basic, app => new CloudsBasicAuthStrategy(app, realm))
		scentry.registerStrategy('UserPassword, app => new UserPasswordStrategy(app))
	}

}

trait AuthenticationRoutes extends ScalatraServlet with LiftJsonRequestBodyWithoutFormats with AuthenticationSupport {

	def checkAuthenticated[T](f: => T): T = {
		if (!isAuthenticated) {
			 redirect("/") 
		}
		f
	}

	post("/auth/login") {
		authenticate
		""
	}

	get("/auth/logout") {
		logOut
		""
	}

	get("/auth/test") {
		if (!isAuthenticated) halt(401, "Unauthenticated")
		<h1>foo</h1>
	}

	get("/auth/whoami") {
		if (!isAuthenticated) halt(401, "Unauthenticated")
		"{\"username\": \"admin\"}"
	}

	get("/auth/closure-test") {
		checkAuthenticated {
			<h1>foo</h1>
		}
	}

}
