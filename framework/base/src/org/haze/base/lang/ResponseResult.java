package org.haze.base.lang;

import java.util.HashMap;
import java.util.Map;

import org.haze.base.json.JSONUtils;
import org.junit.Test;

public class ResponseResult extends HashMap<String,Object>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4720315165513595962L;
	
	public void setSuccess(Boolean success){
		super.put("success", success);
	}
	
	public Boolean getSuccess(){
		Boolean success = (Boolean)this.get("success");
		if(success == null){
			return false;
		}
		return success;
	}
	

	public void setMessage(String message){
		this.put("message", message);
	}

	public String getMessage(){
		return (String)this.get("message");
	}

	public String getString(String name){
		return (String)this.get(name);
	}
	
}
