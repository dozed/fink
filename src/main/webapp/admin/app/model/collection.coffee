define [
	'frameworks'
], ->
	a = 1

	class A extends Backbone.Model
		foo: ->
			console.log("bar");
		
