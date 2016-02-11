package org.haze.base.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;

public class UtilCookie {

	static final String root_domain = "/";

	/**
	 * 写入Cookie
	 * 
	 * @param key
	 * @param value
	 * @param expiredMinute
	 */
	public static void writeCookie(HttpServletResponse response, String key,
			String value, Integer expiredMinute) {
		writeCookie(response,root_domain,key,value,expiredMinute);
	}
	

	public static void writeCookie(HttpServletResponse response,String domain, String key,
			String value, Integer expiredMinute) {
		Cookie cookie;
		try {
			if (!StringUtils.isEmpty(value)) {
				value = URLEncoder.encode(value, "UTF-8");
			}
			cookie = new Cookie(key, value);
			cookie.setPath(domain);
			if (expiredMinute != null) {
				cookie.setMaxAge(expiredMinute * 60);
			}
			response.addCookie(cookie);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public static void writeCookie(HttpServletResponse response, String key,
			String value) {
		writeCookie(response, key, value, null);
	}

	/**
	 * 读Cookie
	 * 
	 * @param request
	 * @param key
	 * @return
	 */
	public static Cookie getCookie(HttpServletRequest request, String key) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}
		for (Cookie c : cookies) {
			if (c.getName().equals(key)) {
				return c;
			}
		}
		return null;
	}

	/**
	 * 读Cookie的值
	 * 
	 * @param request
	 * @param key
	 * @return
	 */
	public static String getCookieValue(HttpServletRequest request, String key) {
		Cookie cookie = getCookie(request, key);
		if (cookie != null) {
			try {
				return URLDecoder.decode(cookie.getValue(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 删除Cookie
	 * 
	 * @param response
	 * @param key
	 */
	public static void deleteCookie(HttpServletResponse response, String key) {
		writeCookie(response, key, null, 0);

	}
}
