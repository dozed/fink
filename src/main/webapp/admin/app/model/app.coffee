define [ 
	"model/whoami"
	"model/post_collection"
	"model/category_collection"
], (WhoAmI, PostCollection, CategoryCollection) ->
	
	class AppModel extends CoffeeBar.Model
		defaults: 
			menu: []
			loading: false
			username: ""
			flash: null
			page: null
			poll_interval: 0
	
	class Router extends Backbone.Router
	
	model = new AppModel
	app =
		router: new Router()
		whoami: new WhoAmI()
		posts: new PostCollection()
		categories: new CategoryCollection()
		model: model
		flash: model.property("flash")
		page: model.property("page")
		menu: model.property("menu")

		update_menu: ->
			app.menu app.model.defaults.menu

	app.model.set({url: window.location.hash})
	$(window).bind "hashchange", (url)->
		app.model.set({url: window.location.hash})


	# Update the username when the whoami info changes..
	app.whoami.bind "change", ->
		app.model.set username: app.whoami.get("username")
		app.update_menu()

	app.posts.bind "set", ->
		console.log "retrieved posts"

	app.categories.bind "add", ->
		console.log "retrieved categories"

	original_sync = Backbone.sync
	Backbone.sync = (method, model, options) ->
		getUrl = (object) ->
			return null  unless (object and object.url)
			(if _.isFunction(object.url) then object.url() else object.url)
		
		params = _.extend({}, options)
		unless params.url
			params.url = getUrl(model) or urlError()
			parts = params.url.split("?", 2)
			#parts[0] += ".json"
			params.url = parts.join("?")
			
		params.headers = _.extend({}, params.headers)
		params.headers["AuthPrompt"] = "false"
		
		original_error = params.error
		params.error = (resp) ->
			app.handle_ajax_error(resp, original_error)
		
		original_sync method, model, params
	
	#Backbone.emulateJSON = true
		 
	app
