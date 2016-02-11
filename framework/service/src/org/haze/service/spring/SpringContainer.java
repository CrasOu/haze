package org.haze.service.spring;

import java.util.HashMap;
import java.util.Map;

import org.haze.base.component.ComponentConfig;
import org.haze.base.config.GenericConfigException;
import org.haze.base.container.Container;
import org.haze.base.container.ContainerConfig;
import org.haze.base.container.ContainerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class SpringContainer extends SpringApplicationContextLoader  implements Container{
	private static final Logger LOG = LoggerFactory.getLogger(SpringContainer.class);
	
	private String configFile;
	
	private static SpringApplicationContext springContext = null;
	
	
	@Override
	public void init(String[] args, String configFile)
			throws ContainerException {
		// TODO Auto-generated method stub
		this.configFile = configFile;
	}

	@Override
	public boolean start() throws ContainerException {
		LOG.info("Start Spring container");

        // make sure the subclass sets the config name
        if (this.getContainerConfigName() == null) {
            throw new ContainerException("Unknown container config name");
        }
        // get the container config
        ContainerConfig.Container cfg = ContainerConfig.getContainer(this.getContainerConfigName(), configFile);
        if (cfg == null) {
            throw new ContainerException("No " + this.getContainerConfigName() + " configuration found in container config!");
        }
     
        StringBuffer contextConfigLocation = new StringBuffer();
        for (ComponentConfig.ServiceResourceInfo componentResourceInfo: ComponentConfig.getAllServiceResourceInfos("spring")) {
        	try {
        		String path = componentResourceInfo.createResourceHandler().getURL().toExternalForm();
        		if(contextConfigLocation.length() > 0 && StringUtils.hasText(path)){
        			contextConfigLocation.append(",");
        		}
        		contextConfigLocation.append(path);
			} catch (GenericConfigException e) {
				LOG.warn(componentResourceInfo.createResourceHandler().getLocation() + " not find ! ");
			}
        }
        
        Map<String,String> initParams = new HashMap<String,String>();
        initParams.put("contextConfigLocation", contextConfigLocation.toString());
        /**
         * 创建上下文
         */
        springContext = SpringApplicationContextUtils.createSpringContext(initParams);
        initApplicationContext(springContext);
        
       // Object dataSource = SpringApplicationContextUtils.getApplicationContext().getBean("dataSource");
       // LOG.info("dataSource:" + dataSource.toString());
		return true;
	}

	@Override
	public void stop() throws ContainerException {
		// TODO Auto-generated method stub
		if(springContext != null){
			closeApplicationContext(springContext);
		}
	}
	
    public String getContainerConfigName() {
        return "spring-container";
    }

}
