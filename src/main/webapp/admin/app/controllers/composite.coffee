
define [
	"frameworks"
], ->
	CoffeeBar.Controller.extend
		
		initialize: ->
			@children = new CoffeeBar.Collection()
			@children.add(@options.children) if @options.children?
			@children.bind "reset", => @render()
			@children.bind "remove", (model)=> @render()
			@children.bind "add", (model)=> @on_add(model)
			
		render: ->
			$(@el).empty()
			for child in @children.models
				@on_add(child)
			@

		on_add: (child)->
			child_el = @render_part(child.toJSON())
			$(@el).append(child_el)

		render_part: (value)->
			switch typeof(value)
				when 'string' then value
				when 'function' then @render_part(value(@))
				else value.render().el

		poll: ->
			for child in @children.toJSON()
				child.poll() if child.poll?
			
