package org.haze.sso.cache;

import java.io.Serializable;
import java.util.Date;

public class SsoCacheItem  implements Serializable{

	private static final long serialVersionUID = -5255088026348016251L;

	private String key;
	private Object value;
	private Date expiredDate;
	
	public SsoCacheItem(String key, String value, Date expriedDate) {
		this.key = key;
		this.value = value;
		this.expiredDate = expriedDate;
	}


	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Date getExpiredDate() {
		return expiredDate;
	}

	public void setExpiredDate(Date expiredDate) {
		this.expiredDate = expiredDate;
	}

	@Override
	public String toString() {
		return super.toString() + ",expiredDate:" + this.expiredDate;
	}

}
