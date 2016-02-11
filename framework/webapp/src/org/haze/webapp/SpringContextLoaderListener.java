package org.haze.webapp;

import javax.servlet.ServletContext;

import org.haze.service.spring.SpringApplicationContextUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoaderListener;

/**
 * 使用SpringContextLoaderListener不能再使用ContextLoaderListener 在web.xml里配locatorFactorySelector 及 parentContextKey
 * 的方式加父容器了
 * @author xueyingou
 *
 */
public class SpringContextLoaderListener extends ContextLoaderListener {
	protected ApplicationContext loadParentContext(ServletContext servletContext) {
		ApplicationContext parentContext = SpringApplicationContextUtils.getApplicationContext();
		return parentContext;
	}
}
