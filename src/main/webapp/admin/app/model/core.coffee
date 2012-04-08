define [ 
  "frameworks" 
], ->

	class Post extends Backbone.Model
		defaults:
			title: ""

		initialize: () ->
			console.log("init foo")

	class PostCollection extends Backbone.Collection
		model: Post

		foo: () ->
			@
