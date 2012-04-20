
define [ 
	"model/app"
	"views/jade"
], (app, jade) ->

	class LoadingPage extends CoffeeBar.TemplateController
		template: jade["application/loading.jade"]

	class Navigation extends CoffeeBar.TemplateController
		el: $("#topbar_nav_container")
		template: jade["application/menu.jade"]
		template_data: ->
			model = app.model.toJSON()
			items: model.menu
			active: model.url

	class UserInfo extends CoffeeBar.TemplateController
		el: $("#topbar_user_container")
		template: jade["application/user_menu.jade"]
		template_data: ->
			app.model.toJSON()

	class Flash extends CoffeeBar.TemplateController
		el: $("#flash_container")
		template: jade["application/flash.jade"]
		template_data: -> 
			flash: app.model.get("flash")
		events: 
			"click  .close": "close"
		close: -> 
			app.flash(null)

	class Application extends CoffeeBar.Controller
		page_container: $("#page_container")
			
		initialize: ->
			topbar_nav_container = new Navigation
			app.model.bind "change:menu", => topbar_nav_container.render()
			app.model.bind "change:url", => topbar_nav_container.render()
			topbar_nav_container.bind "render", (controller)-> bind_menu_actions controller.el
			topbar_nav_container.render()

			topbar_user_container = new UserInfo
			topbar_user_container.bind "render", (controller)-> bind_menu_actions controller.el
			app.model.bind "change:username", ->
				username = app.model.get("username")
				topbar_user_container.render()
				
			app.model.bind "change:page", => 
				console.log "change:page event"
				page = app.page()
				app.flash(null)
				@page_container.empty()
				@page_container.append page.render().el if page
			
			app.model.bind "change:poll_interval", =>
				@stop_poll()
				interval = app.model.get("poll_interval")
				if interval 
					@poll_interval = setInterval (=>@poll()), interval
			
			app.whoami.fetch()
			app.posts.fetch()
			app.categories.fetch()
			app.tags.fetch()

		stop_poll: ->
			if @poll_interval
				clearInterval @poll_interval
				@poll_interval = null

		poll: ->
			page = app.page()
			page.poll()  if page and page.poll
	
	Application

