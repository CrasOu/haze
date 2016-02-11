package org.haze.sso.server;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.haze.base.util.UtilDateTime;
import org.haze.sso.SsoConsts;
import org.haze.sso.cache.SsoCacheItem;
import org.haze.sso.cache.SsoCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/")
public class LoginController {

	@Autowired
	SsoCacheService ssoCacheService;

	@Autowired
	SsoServerConfig ssoServerConfig;

	@RequestMapping(value = "/welcome", method = RequestMethod.GET)
	public ModelAndView welcome() {
		ModelAndView model = new ModelAndView();
		model.setViewName("welcome");
		return model;

	}

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public ModelAndView login(
			@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "logout", required = false) String logout,
			HttpServletRequest request) {

		ModelAndView model = new ModelAndView();
		if (error != null) {
			model.addObject("error",
					getErrorMessage(request, "SPRING_SECURITY_LAST_EXCEPTION"));
		}

		if (logout != null) {
			model.addObject("msg", "您已经安全退出！");
		}

		model.setViewName("login");

		return model;

	}

	// for 403 access denied page
	@RequestMapping(value = "/403", method = RequestMethod.GET)
	public ModelAndView accesssDenied() {

		ModelAndView model = new ModelAndView();

		// check if user is login
		Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();
		if (!(auth instanceof AnonymousAuthenticationToken)) {
			UserDetails userDetail = (UserDetails) auth.getPrincipal();
			model.addObject("username", userDetail.getUsername());
		}

		model.setViewName("comm/403");
		return model;

	}

	private String getErrorMessage(HttpServletRequest request, String key) {

		Exception exception = (Exception) request.getSession()
				.getAttribute(key);

		String error = "";
		if (exception instanceof BadCredentialsException) {
			error = "用户名或密码错误!";
		} else if (exception instanceof LockedException) {
			error = exception.getMessage();
		} else {
			error = "用户名或密码错误!";
		}

		return error;
	}

	@RequestMapping(value = "/verify", method = RequestMethod.GET)
	@ResponseBody
	public String verify(
			@RequestParam(value = SsoConsts.TOKEN_NAME, required = true) String _token,
			HttpServletRequest request) throws IOException, ServletException {
		SsoCacheItem cacheItem = ssoCacheService.getByToken(_token);
		if (cacheItem != null) {
			if (cacheItem instanceof SsoCacheItem) {
				SsoCacheItem ssoCacheItem = (SsoCacheItem) cacheItem;
				if ((new Date()).after(ssoCacheItem.getExpiredDate())) {
					// _token已过期，删除过期Cache
					ssoCacheService.delete(cacheItem);
				} else {
					ssoCacheService.delete(cacheItem);
					// 动态更换新token，提高安全性
					ssoCacheItem.setKey(UUID.randomUUID().toString());
					ssoCacheItem.setExpiredDate(UtilDateTime.addHours(new Date(),
							ssoServerConfig.getCacheCredentialExpireHours()));
					ssoCacheService.put(ssoCacheItem);
					Authentication auth = SecurityContextHolder.getContext()
							.getAuthentication();
					System.out.println("username:" + auth.getName());
					return "1," + ssoCacheItem.getValue() + ","
							+ ssoCacheItem.getKey();
				}
			} else {
				ssoCacheService.delete(cacheItem);
			}
		}
		return "0,null,null";

	}
}
