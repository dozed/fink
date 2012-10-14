package fink.web

import fink.data._

import org.scalatra.auth.{ScentryConfig, ScentrySupport}
import org.scalatra.ScalatraKernel
import org.scalatra.auth.strategy.{BasicAuthStrategy, BasicAuthSupport}
import org.scalatra._

import java.net.URL
import scalate.ScalateSupport
import scala.collection.mutable.ListBuffer

import annotation.tailrec
import servlet.ServletBase
import util.RicherString._
import java.nio.CharBuffer

/**
 * https://gist.github.com/732347
 * https://gist.github.com/660701
 */
trait AuthenticationSupport extends ScentrySupport[User] { self: ScalatraBase =>

	val realm = "omnipass"

	protected def fromSession = { case name: String => UserRepository.find(name) getOrElse null  }
	protected def toSession   = { case usr: User => user.name }

	protected val scentryConfig = (new ScentryConfig {}).asInstanceOf[ScentryConfiguration]
  protected val userPasswordKey = "UserPassword"

	override protected def configureScentry = {
		scentry.unauthenticated {
			scentry.strategies(userPasswordKey).unauthenticated()
		}
	}

	override protected def registerAuthStrategies = {
		// scentry.registerStrategy('Basic, app => new CloudsBasicAuthStrategy(app, realm))
		scentry.register(userPasswordKey, app => new UserPasswordStrategy(app))
	}

}

trait AuthenticationRoutes extends ScalatraServlet with AuthenticationSupport {

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
