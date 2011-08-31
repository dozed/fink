package org.noorg.fink.admin.support

import org.fusesource.scalate.servlet.ServletRenderContext._

object Theming {

	var themeName = "simple"

	def themeBase(path: String) = {
		renderContext.uri("/themes/" + themeName + (if (path.startsWith("/")) "") + path)
	}

	def adminBase(path: String) = {
		renderContext.uri("/fink-admin" + (if (path.startsWith("/")) "") + path)
	}

	def adminAssets(path: String) = {
		renderContext.uri("/admin" + (if (path.startsWith("/")) "") + path)
	}

}