package fink.web

import fink.data.UserRepository
import fink.data.User
import org.scalatra.util.RicherString._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import net.iharder.Base64
import org.scalatra.{ScalatraBase, ScalatraKernel}
import org.scalatra.auth.strategy.{BasicAuthStrategy, BasicAuthSupport}
import org.scalatra.auth.{ScentrySupport, ScentryConfig, ScentryStrategy}
import org.scalatra.servlet.ServletBase

class UserPasswordStrategy(protected val app: ScalatraBase) extends ScentryStrategy[User] {

	private def username = app.params.get("username")
	private def password = app.params.get("password")

	protected def remoteAddress ={
		val proxied = app.request.getHeader("X-FORWARDED-FOR")
		val res = if (proxied.isNonBlank) proxied else app.request.getRemoteAddr
		res
	}

	override def isValid = {
		username.isDefined && password.isDefined
	}

	override def authenticate = {
		println("Authenticating in UserPasswordStrategy with: %s, %s, %s".format(username, password, remoteAddress))
		// perform authentication against database here
		// UserRepository.find("admin") match {
		// 	//case Some(user) => User.login(username.get, password.get)
		// 	case Some(user) => UserRepository.login(username.get, password.get)
		// 	case None => None
		// }
		Some(User(0, "name", "password"))
	}

	override def unauthenticated() = {
		app.halt(401, "Unauthenticated")
	}

  override def afterAuthenticate(winningStrategy: String, user: User) {
    println("User has been authenticated")
  }
}

// trait UserPasswordMixin extends ScalatraServlet {

// 	before {
// 		log info "Executing before filter in user password filter"
// 	}

// 	get("/signup") {
// 		redirectIfAuthenticated_!
// 		// Show signup view
// 	}

// 	post( "/signup") {
// 		// Perform signup and redirect to login
// 	}

// 	get("/confirm/:token?") {
// 		redirectIfAuthenticated_!
// 		val token = params("token")
// 		if (token.blank_?) {
// 			flash.update("error", "The token is missing from the url.")
// 			redirect("/")
// 		} else {
// 			// perform confirmation logic here
// 			redirect("/")
// 		}
// 	}

// 	get("/login") {
// 		redirectIfAuthenticated_!
// 		// Show login view here
// 	}

// 	post("/login") {
// 		authenticate
// 		redirectIfAuthenticated_!
// 		flash.update("error", "There was a problem with your username and/or password.")
// 		redirect("/login")
// 	}

// 	get("/logout") {
// 		// logout logic here
// 		flash.update("success", "You have been logged out")
// 		redirectIfAuthenticated_!
// 		redirect("/")
// 	}

// 	get("/forgot") {
// 		redirectIfAuthenticated_!
// 		// show forgot password form
// 	}

// 	post("/forgot") {
// 		redirectIfAuthenticated_!
// 		// Create email and send 
// 	}

// 	get("/reset/:token?") {
// 		def missingToken = {
// 			flash.update("error", "The token is missing from the url.")
// 			redirect("/")
// 		}
// 		redirectIfAuthenticated_!
// 		params.get("token") match {
// 			case None => missingToken
// 			case Some(token) => if (token.blank_?) {
// 				missingToken
// 			} else {
// 				// Verify the token
// 			}
// 		}
// 	}

// 	post("/reset") {
// 		redirectIfAuthenticated_!
// 		// Perform logic here to reset the password
// 	}
// }
