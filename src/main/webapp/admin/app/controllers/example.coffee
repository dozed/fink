
define [ 
	"model/app", 
	"views/jade"
	"model/example", 
], (app, jade, Example) ->
	
	class ExampleController extends CoffeeBar.TemplateController
		template: jade["example_page/index.jade"]
		template_data: -> @model.toJSON()
		
		initialize: ->
			@model = new Example
			@model.bind "all", => @render()
			@model.fetch()
			console.log "example"
			
		poll: -> @model.fetch()

	console.log "example"
	app.router.route "/example", "example", ->
		app.page new ExampleController 

	ExampleController