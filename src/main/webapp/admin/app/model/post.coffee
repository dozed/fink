define [ 
  "frameworks" 
], ->

	class Post extends CoffeeBar.Model
		urlRoot: "api/posts"
		defaults:
			id: 0
			title: ""
			author: ""
			date: null
			text: ""
			category: null

	Post