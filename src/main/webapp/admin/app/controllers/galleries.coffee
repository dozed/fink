define [
	"controllers/collection_controller"
	"controllers/tabs_controller"
	"model/app"
	"model/gallery"
	"model/gallery_collection"
	"model/image"
	"views/jade"
], (CollectionController, TabsController, app, Gallery, GalleryCollection, Image, jade) ->

	class GalleryEditor extends CoffeeBar.TemplateController
		template: jade["gallery/edit.jade"]

		initialize: ->
			super
			tabs = [
				{ a: "#", label: "Meta", page: new GalleryInfoEditor({ model: @model }) },
				{ a: "#", label: "Images", page: new GalleryImagesEditor({ model: @model }) }
			]
			@tabs_controller = new TabsController
				tabs: tabs

		on_render: ->
			el = @tabs_controller.render().el
			@$(".editor").empty().append(el)


	class GalleryInfoEditor extends CoffeeBar.TemplateController
		events:
			"click .btn-ok": "do_submit"
			"click .btn-cancel": "do_cancel"

		initialize: ->
			super
			@model.bind "change", @render, @

		template: jade["gallery/edit_meta.jade"]
		template_data: ->
			model: @model.toJSON()

		on_render: ->
			# Backbone.ModelBinding.bind(@)
			@$("#title").val(@model.get("title"))
			@$("#text").val(@model.get("text"))

			# handle tags
			@$("#tags").tagit()
			@$("#tags").tagit("removeAll")
			@model.get("tags").map (tag) -> @$("#tags").tagit("createTag", tag.name)

		do_cancel: ->
			app.router.navigate "/galleries", { trigger: true }
			false

		do_submit: ->
			tags = @$("#tags").tagit("assignedTags").map (tag) ->
				c = app.tags.select (a) -> a.get("name") == tag
				if (_.size(c) > 0)
					_.first(c)
				else
					{id: 0, name: tag}

			@model.set
				title: @$("#title").val()
				text: @$("#text").val()
				tags: tags

			@model.save()
			app.tags.fetch()
			app.router.navigate "/galleries", { trigger: true }

			false

	class GalleryImagesEditor extends CoffeeBar.TemplateController
		template: jade["gallery/edit_images.jade"]

		initialize: ->
			super

			@image_collection = new CoffeeBar.Collection()
			@image_collection.reset([new Image(), new Image()])

			@collection_controller = new CollectionController
				collection: @image_collection
				child_control: (model) ->
					console.log "foo"
					new ImageListItem({ model: model })

		on_render: ->
			el = @collection_controller.render().el
			console.log el
			@$(".images").empty().append(el)


	class ImageListItem extends CoffeeBar.TemplateController
		template: jade["gallery/image.jade"]


	# main gallery list
	class GalleriesPage extends CoffeeBar.TemplateController
		template: jade["gallery/index.jade"]

		initialize: ->
			super
			#@model.bind "all", => @render()

			@collection_controller = new CollectionController
				collection: app.galleries
				child_control: (model) -> new GalleryListItem({ model: model })

		on_render: ->
			el = @collection_controller.render().el
			@$(".galleries").empty().append(el)

	class GalleryListItem extends CoffeeBar.TemplateController
		template: jade["gallery/item.jade"]
		events:
			"click a.btn-delete": "do_delete"

		# TODO why is the explicit call needed?
		on_render: ->	@delegateEvents()

		template_data: -> @model.toJSON()
		do_delete: -> @model.destroy(); false

	app.router.route "/galleries", "galleries", ->
		app.page new GalleriesPage

	app.router.route "/galleries/create", "galleries", ->
		app.page new EditGalleryView({ model: new Gallery({ date: new Date().getTime() }), template: jade["gallery/create.jade"] })

	app.router.route "/galleries/edit/:id", "galleries", (id) ->
		gallery = new Gallery({id: id})
		app.page new GalleryEditor({ model: gallery })
		gallery.fetch()
		
	GalleriesPage