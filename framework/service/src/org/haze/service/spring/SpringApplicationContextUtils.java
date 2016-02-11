package org.haze.service.spring;

import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;

public class SpringApplicationContextUtils {
	
	private static SpringApplicationContext springContext = null;
	
	static SpringApplicationContext createSpringContext(Map<String,String> initParams){
		if(springContext != null){
			throw new ApplicationContextException("The created SpringContext object ");
		}else{
			springContext = new SpringApplicationContext(initParams);
		}
		return springContext;
	}
	
	public static ApplicationContext getApplicationContext(){
		return getApplicationContext(SpringApplicationContext.ROOT_APPLICATION_CONTEXT_ATTRIBUTE);
	}
	
	public static ApplicationContext getApplicationContext(String attrName){
		Assert.notNull(springContext, "ServletContext must not be null");
		Object attr = springContext.getAttribute(attrName);
		if (attr == null) {
			return null;
		}
		if (attr instanceof RuntimeException) {
			throw (RuntimeException) attr;
		}
		if (attr instanceof Error) {
			throw (Error) attr;
		}
		if (attr instanceof Exception) {
			throw new IllegalStateException((Exception) attr);
		}
		if (!(attr instanceof ApplicationContext)) {
			throw new IllegalStateException("Context attribute is not of type ApplicationContext: " + attr);
		}
		return (ApplicationContext) attr;
	}
	
}
