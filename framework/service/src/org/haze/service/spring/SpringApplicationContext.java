package org.haze.service.spring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpringApplicationContext {
	public static String ROOT_APPLICATION_CONTEXT_ATTRIBUTE = SpringApplicationContext.class
			.getName() + ".ROOT";
	private String name;
	/**
	 * 属性
	 */
	private Map<String, Object> attributeMap = new ConcurrentHashMap<String, Object>();
	/**
	 * 初始化参数
	 */
	private Map<String, String> initParameterStore = new ConcurrentHashMap<String, String>();

	SpringApplicationContext(Map<String, String> initParameter) {
		if (initParameter != null) {
			initParameterStore = new ConcurrentHashMap<String, String>(
					initParameter);
		}

	}

	public Object getAttribute(String attribute) {
		return attributeMap.get(attribute);
	}

	public void removeAttribute(String attribute) {
		attributeMap.remove(attribute);
	}

	public void setAttribute(String attribute, Object value) {
		attributeMap.put(attribute, value);
	}

	public String getInitParameter(String parameter) {
		return initParameterStore.get(parameter);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}
