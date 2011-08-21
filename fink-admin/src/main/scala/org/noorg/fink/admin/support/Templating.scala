package org.noorg.fink.admin.support

import org.fusesource.scalate.servlet.ServletRenderContext._

object Templating {

	var templateName = "simple"

	def base(path: String) = {
		renderContext.uri("/templates/" + templateName + path)
	}

}