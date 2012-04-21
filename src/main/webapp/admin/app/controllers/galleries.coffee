define [
	"controllers/collection_controller"
	"model/app"
	"model/gallery"
	"model/gallery_collection"
	"views/jade"
], (CollectionController, app, Gallery, GalleryCollection, jade) ->

	class EditGalleryView extends CoffeeBar.TemplateController
		events:
			"click .btn-ok": "do_submit"
			"click .btn-cancel": "do_cancel"

		initialize: ->
			super
			@model.bind "change", @render, @

		template: jade["gallery/edit.jade"]
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

	class GalleriesPage extends CoffeeBar.TemplateController
		template: jade["gallery/index.jade"]

		initialize: ->
			super
			#@model.bind "all", => @render()

			@collection_controller = new CollectionController
				collection: app.galleries
				child_control: (model) -> new GalleryItem({ model: model })

		on_render: ->
			el = @collection_controller.render().el
			@$(".galleries").empty().append(el)

		poll: -> @model.fetch()

	class GalleryItem extends CoffeeBar.TemplateController
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
		app.page new EditGalleryView({ model: gallery })
		gallery.fetch()
		
	GalleriesPage