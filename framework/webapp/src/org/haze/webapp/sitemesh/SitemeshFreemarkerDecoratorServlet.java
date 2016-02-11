package org.haze.webapp.sitemesh;

import java.io.File;
import java.io.IOException;

import com.opensymphony.module.sitemesh.freemarker.FreemarkerDecoratorServlet;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;

public class SitemeshFreemarkerDecoratorServlet extends FreemarkerDecoratorServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 7006525670995761097L;
	
	private static TemplateLoader templateLoader = null;
	
    protected TemplateLoader createTemplateLoader(String templatePath) throws IOException {
    	if(templateLoader == null){
    		templateLoader =  new FileTemplateLoader(new File(System.getProperty("haze.home")+"/themes"));
    	}
    	return templateLoader;
    }
    
}
