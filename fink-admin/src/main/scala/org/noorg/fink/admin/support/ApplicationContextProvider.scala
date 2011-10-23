package org.noorg.fink.admin.support;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

class ApplicationContextProvider extends ApplicationContextAware {
	override def setApplicationContext(applicationContext: ApplicationContext) = {
		ApplicationContextProvider.context = applicationContext;
	}
}

object ApplicationContextProvider {

	private var context: ApplicationContext = null

	def getContext() = context

}
