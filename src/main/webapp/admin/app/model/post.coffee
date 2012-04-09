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

	Post