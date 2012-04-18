define [
	"controllers/collection_controller"
	"model/app"
	"model/post"
	"model/post_collection"
	"views/jade"
], (CollectionController, app, Post, PostCollection, jade) ->

	class CreatePostView extends CoffeeBar.TemplateController
		events:
			"click .btn-ok": "do_submit"
			"click .btn-cancel": "do_cancel"

		model: new Post()

		template: jade["posts/create.jade"]
		template_data: ->
			model: @model
			categories: app.categories.toJSON()

		initialize: ->
			super
			app.categories.bind "change", @render, @

		on_render: ->
			Backbone.ModelBinding.bind(@)
			@$("#tagsInput").tagsInput
				height: "100px"
				width: "300px"

		do_cancel: ->
			app.router.navigate "/posts", { trigger: true }
			false

		do_submit: ->
			catId = @model.attributes.category
			cat = app.categories.get(catId)

			modelClone = @model.clone()
			modelClone.attributes.category = cat

			modelClone.save()
			app.router.navigate "/posts", { trigger: true }
			false

	class EditPostView extends CoffeeBar.TemplateController
		template: jade["posts/edit.jade"]
		events:
			"click .btn-ok": "do_submit"
			"click .btn-cancel": "do_cancel"

		initialize: ->
			super
			@model.bind "change", @render, @

		template_data: ->
			model: @model.toJSON()
			categories: app.categories.toJSON()

		on_render: ->
			# Backbone.ModelBinding.bind(@)
			@$("#title").val(@model.get("title"))
			@$("#author").val(@model.get("author"))
			@$("#text").val(@model.get("text"))

			@$('input[name="tags"]').val(@model.get("tags").map (tag) -> tag.name)

			@tagsInput = @$('input[name="tags"]').tagsInput
				height: "100px"
				width: "300px"

			# handle tags
			@$('input[name="tags"]').importTags

			# selected category
			cat = @model.get("category")
			if cat != null && cat != 'undefined'
				@$('select#categorySelect option[value="' + cat.id + '"]').prop("selected", true)

		do_cancel: ->
			app.router.navigate "/posts", { trigger: true }
			false

		# TODO need to click twice
		do_submit: ->
			catId = @$("select#categorySelect option:selected").val()
			cat = app.categories.get(catId)

			tags = @$('input[name="tags"]').val().split(",").map (tag) -> {id: 0, name: tag}

			@model.set
				title: @$("#title").val()
				author: @$("#author").val()
				text: @$("#text").val()
				category: cat
				tags: tags

			@model.save()
			app.router.navigate "/posts", { trigger: true }

			false

	class PostsPage extends CoffeeBar.TemplateController
		template: jade["posts/index.jade"]

		initialize: ->
			super
			@model = new PostCollection
			@model.bind "all", => @render()
			@model.fetch()

			@collection_controller = new CollectionController
				collection: @model
				child_control: (model) -> new PostRow({ model: model })

		on_render: ->
			el = @collection_controller.render().el
			@$(".post_list").empty().append(el)

		poll: -> @model.fetch()

	class PostRow extends CoffeeBar.TemplateController
		template: jade["posts/post_row.jade"]
		events:
			"click a.btn-delete": "do_delete"

		# TODO why is the explicit call needed?
		on_render: ->	@delegateEvents()

		template_data: -> @model.toJSON()
		do_delete: -> @model.destroy(); false

	app.router.route "/posts", "posts", ->
		app.page new PostsPage

	app.router.route "/posts/create", "posts", ->
		app.page new CreatePostView

	app.router.route "/posts/edit/:id", "posts", (id) ->
		post = new Post({id: id})
		app.page new EditPostView({ model: post })
		post.fetch()

	PostsPage