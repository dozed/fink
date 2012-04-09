define [ 
  "frameworks" 
], ->

	class Category extends CoffeeBar.Model
		urlRoot: "api/categories"
		defaults:
			id: 0
			name: ""

	Category