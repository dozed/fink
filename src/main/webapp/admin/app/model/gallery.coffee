define [ 
  "frameworks"
], ->

	class Gallery extends CoffeeBar.Model
		urlRoot: "api/galleries"
		defaults:
			id: 0
			date: 0
			title: ""
			shortlink: ""
			tags: []
			images: []
			cover: null

	Gallery