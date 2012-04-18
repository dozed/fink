
define [
	"frameworks"
], ->
	#
	# {
	#    open: boolean | default: false
	#    title: string | requried
	#    content: function Controller | required
	# }
	#
	CoffeeBar.Controller.extend(Backbone.Events).extend
		title: ""
		open: (new CoffeeBar.Model()).property("open")
		
		initialize: ->
			@open = @options.open if @options.open
			@title = @options.title if @options.title?
			@open.bind @render, @

		render_part: (value)->
			switch typeof(value)
				when 'string' then value
				when 'function' then @render_part(value(@))
				else value.render().el

		remove: ->
			@open.unbind @render
			CoffeeBar.Controller.prototype.remove.call(@)
			
		render: ->
			$(@el).empty();
			link = @make("a", {class:"accordion", href:"#"})
			@el.appendChild(link)
			link.appendChild(@make("h3", {}, @title))
			if @open()
				$(link).addClass("accordion-opened")
				$(link).click =>
					@open(false)
					false
					
				content_div = $(@make("div"))
				content_div.hide()
				$(@el).append(content_div)
				rendered_content = @render_part(@content)
				content_div.append(rendered_content)
				content_div.toggle "slow"
			else
				$(link).removeClass("accordion-opened")
				$(link).click =>
					@open(true)
					false
			@