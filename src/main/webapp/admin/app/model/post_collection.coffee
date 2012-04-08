define [ 
  "frameworks"
  "model/post"
], (Frameworks, Post) ->

	class PostCollection extends Backbone.Collection
		model: Post
		url: "api/posts"

		foo: () ->
			@

	PostCollection
