define [
	"model/app"
	"model/post"
	"model/post_collection"
	"controllers/application"
	"controllers/signin"
	"controllers/posts"
	"controllers/home"
], (app, Post, PostCollection, Application) ->
	$ ->

		# setup menu defaults
		menu = []
		menu.push
			href: "#/home"
			label: "Home"
		menu.push
			href: "#/posts"
			label: "Posts"
		menu.push
			href: "#/signout"
			label: "Signout"
		app.model.defaults.menu = menu

		# setup global ajax error handler
		app.handle_ajax_error = (resp, next)->
			if resp.status == 401
				unless _.isEmpty(app.model.get("username"))
					app.flash 
						kind: "error"
						title: "Unauthorized! "
						message: "You are not authorized to do perform that action.  Perhaps you need to sign in under a different username."
						actions: "<a href='#/signin' class='btn'>Sign In</a>"
				else
					app.router.navigate "/signin", true
			else
				if next
					next(resp)
				else
					app.flash 
						kind: "error"
						title: "Server Error"
						message: "The server is expirencing some problems right now.  Try again later."

		application = new Application()
		app.router.navigate("/signin", true) unless Backbone.history.start({})
