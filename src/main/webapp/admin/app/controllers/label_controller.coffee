
define [ 
	"frameworks"
], ->
	CoffeeBar.Controller.extend
		initialize: ->
			if @options.event
				@options.model.bind @options.event, => @render()
			else
				@options.model.bind "change", => @render()

		render: ->
			$(@el).html(@options.template(@model.toJSON()))
			@