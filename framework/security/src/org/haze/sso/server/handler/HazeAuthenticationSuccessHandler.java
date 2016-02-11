package org.haze.sso.server.handler;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.haze.base.util.UtilCookie;
import org.haze.base.util.UtilDateTime;
import org.haze.sso.SsoConsts;
import org.haze.sso.cache.SsoCacheItem;
import org.haze.sso.cache.SsoCacheService;
import org.haze.sso.server.SsoServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.StringUtils;

public class HazeAuthenticationSuccessHandler implements
		AuthenticationSuccessHandler {

	SsoCacheService ssoCacheService;

	Logger log = Logger.getLogger(this.getClass());

	@Autowired
	SsoServerConfig ssoServerConfig;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		log.debug("HazeAuthenticationSuccessHandler.onAuthenticationSuccess() is called!");
		log.debug("username => " + authentication.getName());

		// 01.先根据登录名，找到旧Cache Record
		SsoCacheItem ssoCacheItem = ssoCacheService
				.getByUserName(authentication.getName());
		if (ssoCacheItem != null) {
			ssoCacheService.delete(ssoCacheItem);
		}
		// 02.写入新记录
		String key = UUID.randomUUID().toString();
		SsoCacheItem item = new SsoCacheItem(key, authentication.getName(),
				UtilDateTime.addHours(new Date(),
						ssoServerConfig.getCacheCredentialExpireHours()));
		this.ssoCacheService.put(item);

		log.debug("sso cache => " + ssoCacheService.getByToken(key));

		// 03.在sso所属的域中，生成cookie(以便其它同域的子应用，能实现自动登录)
		UtilCookie.writeCookie(response, SsoConsts.TOKEN_NAME, key);

		String returnUrl = request.getParameter("returnUrl");
		log.debug("returnUrl:" + returnUrl);
		if (StringUtils.isEmpty(returnUrl)) {
			returnUrl = "welcome";
		} else {
			if (returnUrl.indexOf("?") != -1) {
				returnUrl += "&" + SsoConsts.TOKEN_NAME + "=" + key;
			} else {
				returnUrl += "?" + SsoConsts.TOKEN_NAME + "=" + key;
			}
		}
		response.sendRedirect(returnUrl);

	}

	public SsoCacheService getSsoCacheService() {
		return ssoCacheService;
	}

	public void setSsoCacheService(SsoCacheService ssoCacheService) {
		this.ssoCacheService = ssoCacheService;
	}

	public SsoServerConfig getSsoServerConfig() {
		return ssoServerConfig;
	}

	public void setSsoServerConfig(SsoServerConfig ssoServerConfig) {
		this.ssoServerConfig = ssoServerConfig;
	}

}
