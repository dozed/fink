
define [ 
  "model/app", 
  "views/jade"
  "model/example", 
], (app, jade, Example) ->
  
  ExampleController = CoffeeBar.TemplateController.extend
    template: jade["example_page/index.jade"]
    template_data: -> @model.toJSON()
    
    initialize: ->
      @model = new Example
      @model.bind "all", => @render()
      @model.fetch()
      
    poll: -> @model.fetch()

  app.router.route "/example", "example", ->
    app.page new ExampleController 

  ExampleController