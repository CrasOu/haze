package org.haze.webapp.sitemesh;

import javax.servlet.FilterConfig;

import org.apache.log4j.Logger;

import com.opensymphony.module.sitemesh.Config;
import com.opensymphony.module.sitemesh.Factory;
import com.opensymphony.sitemesh.ContentProcessor;
import com.opensymphony.sitemesh.DecoratorSelector;
import com.opensymphony.sitemesh.compatability.DecoratorMapper2DecoratorSelector;
import com.opensymphony.sitemesh.compatability.PageParser2ContentProcessor;
import com.opensymphony.sitemesh.webapp.SiteMeshFilter;
import com.opensymphony.sitemesh.webapp.SiteMeshWebAppContext;

public class SitemeshFilter extends SiteMeshFilter {
	private static Logger LOG = Logger.getLogger(SitemeshFilter.class);
	private static Factory factory = null;
	private static ContentProcessor contentProcessor = null;
	private static DecoratorSelector decoratorSelector = null;
	
	public void init(FilterConfig filterConfig) {
		super.init(filterConfig);
		if(factory == null){
			factory =new DefaultFactory(new Config(filterConfig));
			factory.refresh();
		}
	}

	protected ContentProcessor initContentProcessor(
			SiteMeshWebAppContext webAppContext) {
		// TODO: Remove heavy coupling on horrible SM2 Factory
		if(contentProcessor == null){
			LOG.info(" init  ContentProcessor ");
			contentProcessor = new PageParser2ContentProcessor(factory);
		}
		return contentProcessor;
	}

	protected DecoratorSelector initDecoratorSelector(
			SiteMeshWebAppContext webAppContext) {
		// TODO: Remove heavy coupling on horrible SM2 Factory
		if(decoratorSelector == null){
			LOG.info(" init  DecoratorSelector ");
			decoratorSelector =  new DecoratorMapper2DecoratorSelector(
				factory.getDecoratorMapper());
		}
		return decoratorSelector;
	}
}
