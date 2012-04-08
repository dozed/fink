define [
	"model/app"
	"views/jade"
], (app, jade) ->
	SigninController = CoffeeBar.TemplateController.extend

		initialize: ->
			@template = jade["signin/index.jade"]
			@template_data = -> {
				username: "admin"
			}

			@bind "render", =>
				@$("form").submit ->
						username = $(this).find("input[name=\"username\"]").val()
						password = $(this).find("input[name=\"password\"]").val()

						$.ajax
							url: "auth/login"
							dataType: "json"
							type: "POST"
							data:
								username: username
								password: password

							success: (data) ->
								app.whoami.fetch()
								app.router.navigate("#/", true)

							error: (data) ->
								if data.status == 401
									app.flash
										kind: "error"
										title: "Invalid username or password"
									app.router.navigate "#/signin", false
								else
									app.flash
										kind: "error"
										title: "Error communicating with the server."
									app.router.navigate "#/signin", true

						false


	app.router.route "/signin", "signin", (tab, test) ->
		app.menu []
		app.page new SigninController

	app.router.route "/signout", "signout", (tab, test) ->
		$.ajax
			url: "auth/logout"
			dataType: "json"
			success: (data) ->
				app.whoami.set({username:""})
				app.router.navigate "/signin", true
				app.flash
						kind: "info"
						title: "Logged out! "
						message: "Your session has been closed."

			error: (data) ->
				app.whoami.set({username:""})
				app.flash
					kind: "error"
					title: "Error communicating with the server."

	SigninController