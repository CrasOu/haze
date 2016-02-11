package org.haze.sso.server.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.haze.base.util.UtilCookie;
import org.haze.sso.SsoConsts;
import org.haze.sso.cache.SsoCacheItem;
import org.haze.sso.cache.SsoCacheService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.util.StringUtils;

public class HazeLogoutSuccessHandler implements LogoutSuccessHandler {

	SsoCacheService ssoCacheService;

	Logger log = Logger.getLogger(this.getClass());

	@Override
	public void onLogoutSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		log.debug("HazeLogoutSuccessHandler.onLogoutSuccess() is called!");
		if (authentication == null) {
			// 说明已经退出了,无需重复退出
			response.sendRedirect("login?logout");
			return;
		}
		log.debug("username => " + authentication.getName());

		// 删除Cache
		SsoCacheItem ssoCacheItem = ssoCacheService
				.getByUserName(authentication.getName());
		if (ssoCacheItem != null) {
			ssoCacheService.delete(ssoCacheItem);
		}
		// 删除客户端Cookie
		UtilCookie.deleteCookie(response, SsoConsts.TOKEN_NAME);

		// 处理返回页
		String returnUrl = request.getParameter(SsoConsts.RETURN_URL_NAME);
		log.debug("returnUrl:" + returnUrl);
		if (StringUtils.isEmpty(returnUrl)) {
			returnUrl = "login?logout";
		}

		response.sendRedirect(returnUrl);
	}

	public SsoCacheService getSsoCacheService() {
		return ssoCacheService;
	}

	public void setSsoCacheService(SsoCacheService ssoCacheService) {
		this.ssoCacheService = ssoCacheService;
	}
}
