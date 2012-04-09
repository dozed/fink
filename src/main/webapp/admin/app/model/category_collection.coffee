define [ 
  "frameworks"
  "model/category"
], (Frameworks, Category) ->

	class CategoryCollection extends Backbone.Collection
		model: Category
		url: "api/categories"

	CategoryCollection
