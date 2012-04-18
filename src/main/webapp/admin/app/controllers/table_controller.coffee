
define [ 
	"controllers/collection_controller"
], (CollectionController)->
	
	#
	# new table 
	#   template: jade["some-table.jade"]
	#   row_template: jade["some-tr.jade"]
	#   collection: A Collection
	#
	table = CoffeeBar.TemplateController.extend
		tagName: "table"
		initialize: ->
			CoffeeBar.TemplateController.prototype.initialize.call(this);
			@row_template = @options.row_template if @options.row_template
			@row_template_data = @options.row_template_data if @options.row_template_data
			@collection_controller = new CollectionController
				tagName: "tbody"
				collection: @options.collection
				child_control: (model)=> @child_control(model)
			
			@row_controls = @collection_controller.child_controls
			
		on_render: ->
			tbody = @collection_controller.render().el
			$(@el).append(tbody)
		
		row_template_data: (model)-> model.toJSON()
		
		child_control: (model)->
			controller = new CoffeeBar.TemplateController
				model: model
				tagName: "tr"
				template: @options.row_template
				template_data: => @row_template_data(model)
			if @options.on_row_render
				controller.bind "render", @options.on_row_render 
			model.bind "change", -> controller.render()
			controller
