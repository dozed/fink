define [ 
  "model/category"
  "frameworks"
], (category) ->

	class Post extends CoffeeBar.Model
		urlRoot: "api/posts"
		defaults:
			id: 0
			date: 0
			title: ""
			author: ""
			text: ""
			category: null
			tags: []

		toJSON: ->
			js = super
			if (js.category != null && js.category.toJSON)
				js.category = js.category.toJSON()
			js

	Post