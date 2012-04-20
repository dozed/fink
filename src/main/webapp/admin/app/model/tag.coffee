define [ 
  "model/category"
  "frameworks"
], (category) ->

	class Tag extends CoffeeBar.Model
		urlRoot: "api/tags"
		defaults:
			id: 0
			name: ""

	Tag