define [ 
  "frameworks"
  "model/gallery"
], (Frameworks, Gallery) ->

	class GalleryCollection extends Backbone.Collection
		model: Gallery
		url: "api/galleries"

	GalleryCollection
