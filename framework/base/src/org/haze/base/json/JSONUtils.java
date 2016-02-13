package org.haze.base.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONUtils {
	
	private static final Logger LOG = LoggerFactory.getLogger(JSONUtils.class);
	
	 private final static ObjectMapper objectMapper = new ObjectMapper();

	    private JSONUtils() {

	    }

	    public static ObjectMapper getInstance() {

	        return objectMapper;
	    }

	    /**
	     * javaBean,list,array convert to json string
	     */
	    public static String toJson(Object obj) {
	        try {
				return objectMapper.writeValueAsString(obj);
			} catch (IOException e) {
				LOG.error(e.getMessage());
			}
	        return null;
	    }

	    /**
	     * json string convert to javaBean
	     */
	    public static <T> T fromJson(String jsonStr, Class<T> clazz){
	        try {
				return objectMapper.readValue(jsonStr, clazz);
			} catch (IOException e) {
				LOG.error(e.getMessage());
			}
	        return null;
	    }

	    /**
	     * json string convert to map
	     */
	    public static <T> Map<String, Object> fromJson(String jsonStr){
	        try {
				return objectMapper.readValue(jsonStr, Map.class);
			} catch (IOException e) {
				LOG.error(e.getMessage());
			}
	        return null;
	    }

	    /**
	     * json string convert to map with javaBean
	     */
	    public static <T> Map<String, T> MapFromJson(String jsonStr, Class<T> clazz)
	            throws Exception {
	        Map<String, Map<String, Object>> map = objectMapper.readValue(jsonStr,
	                new TypeReference<Map<String, T>>() {
	                });
	        Map<String, T> result = new HashMap<String, T>();
	        for (Entry<String, Map<String, Object>> entry : map.entrySet()) {
	            result.put(entry.getKey(), fromMap(entry.getValue(), clazz));
	        }
	        return result;
	    }

	    /**
	     * json array string convert to list with javaBean
	     */
	    public static <T> List<T> listFromJson(String jsonArrayStr, Class<T> clazz)
	            throws Exception {
	        List<Map<String, Object>> list = objectMapper.readValue(jsonArrayStr,
	                new TypeReference<List<T>>() {
	                });
	        List<T> result = new ArrayList<T>();
	        for (Map<String, Object> map : list) {
	            result.add(fromMap(map, clazz));
	        }
	        return result;
	    }

	    /**
	     * map convert to javaBean
	     */
	    public static <T> T fromMap(Map map, Class<T> clazz) {
	        return objectMapper.convertValue(map, clazz);
	    }
}
