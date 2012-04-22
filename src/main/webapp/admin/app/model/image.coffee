define [ 
  "frameworks"
], ->

	class Image extends CoffeeBar.Model
		urlRoot: "api/images"
		defaults:
			id: 0
			date: 0
			author: ""
			title: ""
			full: ""
			medium: ""
			thumb: ""


	Image