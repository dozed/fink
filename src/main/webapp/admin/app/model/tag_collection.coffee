define [ 
  "frameworks"
  "model/tag"
], (Frameworks, Tag) ->

	class TagCollection extends Backbone.Collection
		model: Tag
		url: "api/tags"

	TagCollection
