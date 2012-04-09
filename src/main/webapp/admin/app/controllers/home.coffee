define [
	"model/app"
	"views/jade"
], (app, jade) ->
	DashboardController = CoffeeBar.TemplateController.extend

		initialize: ->
			@template = jade["home/dashboard.jade"]

			@bind "render", =>
				console.log "after render"

	app.router.route "/", "home", (tab, test) ->
		app.page new DashboardController

	app.router.route "/home", "home", (tab, test) ->
		app.page new DashboardController

	DashboardController