
define [ 
	"frameworks"
], ->
	class Example extends CoffeeBar.Model
		url: "api/example"
		defaults:
			"name":null
			"address":[]
			"time":null

	return Example
