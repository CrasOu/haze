package com.hs.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class WebRootInterceptor extends HandlerInterceptorAdapter {

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		request.setAttribute("webRoot",RequestUtil.getAppURL(request));//项目根路径
		request.setAttribute("ctx",request.getContextPath());//项目根路径
		super.postHandle(request, response, handler, modelAndView);
	}

}
