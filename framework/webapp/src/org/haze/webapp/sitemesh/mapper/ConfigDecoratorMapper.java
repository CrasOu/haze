/*
 * Title:        ConfigDecoratorMapper
 * Description:
 *
 * This software is published under the terms of the OpenSymphony Software
 * License version 1.1, of which a copy has been included with this
 * distribution in the LICENSE.txt file.
 */

package org.haze.webapp.sitemesh.mapper;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.opensymphony.module.sitemesh.Config;
import com.opensymphony.module.sitemesh.Decorator;
import com.opensymphony.module.sitemesh.DecoratorMapper;
import com.opensymphony.module.sitemesh.Page;
import com.opensymphony.module.sitemesh.mapper.AbstractDecoratorMapper;

/**
 * Default implementation of DecoratorMapper. Reads decorators and
 * mappings from the <code>config</code> property (default '/WEB-INF/decorators.xml').
 *
 * @author <a href="joe@truemesh.com">Joe Walnes</a>
 * @author <a href="mcannon@internet.com">Mike Cannon-Brookes</a>
 * @version $Revision: 1.2 $
 *
 * @see com.opensymphony.module.sitemesh.DecoratorMapper
 * @see com.opensymphony.module.sitemesh.mapper.DefaultDecorator
 * @see com.opensymphony.module.sitemesh.mapper.ConfigLoader
 */
public class ConfigDecoratorMapper extends AbstractDecoratorMapper {
	
	private static final Logger LOG = Logger.getLogger(ConfigDecoratorMapper.class);
	
	private static final String SITEMESH_THEME ="sitemesh.theme";
	
    private ConfigLoader configLoader = null;

    /** Create new ConfigLoader using '/WEB-INF/decorators.xml' file. */
    public void init(Config config, Properties properties, DecoratorMapper parent) throws InstantiationException {
        super.init(config, properties, parent);
        try {
            String fileName = properties.getProperty("config", "decorators.xml");
            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
            Resource[] configResources = resourcePatternResolver.getResources(fileName);
            if(configResources != null && configResources.length > 0){
            	configLoader = new ConfigLoader(configResources);
            }else{
            	LOG.error(" not find decorators.xml");
            }
        }
        catch (Exception e) {
        	e.printStackTrace();
            throw new InstantiationException(e.getMessage());
        }
    }

    /** Retrieve {@link com.opensymphony.module.sitemesh.Decorator} based on 'pattern' tag. */
    public Decorator getDecorator(HttpServletRequest request, Page page) {
        String thisPath = request.getServletPath();

        // getServletPath() returns null unless the mapping corresponds to a servlet
        if (thisPath == null) {
            String requestURI = request.getRequestURI();
            if (request.getPathInfo() != null) {
                // strip the pathInfo from the requestURI
                thisPath = requestURI.substring(0, requestURI.indexOf(request.getPathInfo()));
            }
            else {
                thisPath = requestURI;
            }
        }
        else if ("".equals(thisPath)) {
            // in servlet 2.4, if a request is mapped to '/*', getServletPath returns null (SIM-130)
            thisPath = request.getPathInfo();
        }

        try {
        	if(configLoader != null){
        		String theme = this.getSitemeshThemeName(request);
	        	String name = configLoader.getMappedName(theme,thisPath);
	
	            Decorator result = getNamedDecorator(request, name);

	            
	            return result == null ? super.getDecorator(request, page) : result;
        	}
        }
        catch (ServletException e) {
            LOG.warn("load Decorator fail " + thisPath);
        }catch(IOException e){
            LOG.warn("load Decorator fail " + thisPath);
        	
        }
        return null;
    }

    /** Retrieve Decorator named in 'name' attribute. Checks the role if specified. */
    public Decorator getNamedDecorator(HttpServletRequest request, String name) {
        try {
        	if(configLoader != null){
        		String theme = this.getSitemeshThemeName(request);
	        	Decorator result = configLoader.getDecoratorByName(theme,name);
	            if (result == null || (result.getRole() != null && !request.isUserInRole(result.getRole()))) {
	                // if the result is null or the user is not in the role
	                return super.getNamedDecorator(request, name);
	            }
	            return result;
        	}
        }
        catch (ServletException e) {
            LOG.warn("load Decorator by name fail " + name);
        }catch(IOException e){
            LOG.warn("load Decorator by name fail " + name);
        }
            return null;
    }
    
    private String getSitemeshThemeName(HttpServletRequest request){
		String theme = request.getParameter(SITEMESH_THEME);
		if(StringUtils.isBlank(theme)){
			theme = (String)request.getSession(true).getAttribute(SITEMESH_THEME);
		}else if(StringUtils.isBlank(theme)){
			theme = (String)request.getServletContext().getAttribute(SITEMESH_THEME);
		}
		
		return theme;
    	
    }
}
