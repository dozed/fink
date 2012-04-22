
define [
	"model/app"
	"frameworks"
], (app)->

	class TabsController extends CoffeeBar.Controller
		initialize: ->
			@state = new CoffeeBar.Model
			@tabs = new CoffeeBar.Collection
			tabs = for id,value of @options.tabs
				value.id = id
				value
			@tabs.reset(tabs)
			@state.set({tab:@options.tab || @options.tabs[0].id})
			@tabs.bind "reset", => @render()
			
		select: (tab)->
			@state.set({tab: tab})
			
		render_part: (value)->
			switch typeof(value)
				when 'string' then value
				when 'function' then @render_part(value(@))
				else value.render().el
				
		render: ->

			tab_menu = $(@make("ul", {class:"tabs"}))
			tab_page = $(@make("div"))

			$(@el).each ->
				$(@).empty()
				$(@).append(tab_menu)
				$(@).append(tab_page)

			for model in @tabs.models
				closure = => 
					item = model.toJSON()
					menu_item = $(@make("li"))
					menu_a = $(@make("a", item.a))
					label = @render_part(item.label)
					menu_item.each -> 
						menu_a.append(label)
						$(@).append(menu_a)

					update_active = =>
						if item.id == @state.get("tab")
							if @active_item != item
								@active_item = item
								menu_item.addClass("active")
								page = @render_part(item.page)
								tab_page.each ->
									$(@).empty();
									$(@).append( page )
						else
							menu_item.removeClass("active")
							
					update_active()
					menu_item.click => 
						app.router.navigate(item.route) if item.route
						@state.set({tab: item.id})
					@state.bind "change", => update_active()
					tab_menu.each -> $(@).append(menu_item)

				closure()
			@

	TabsController
