package org.haze.sso.server;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.haze.base.lang.ResponseResult;
import org.haze.base.util.UtilDateTime;
import org.haze.sso.cache.SsoCacheItem;
import org.haze.sso.cache.SsoCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/server")
public class SsoServerController {

	@Autowired
	SsoCacheService ssoCacheService;

	@Autowired
	SsoServerConfig ssoServerConfig;


	@RequestMapping(value = "/serviceValidate", method = RequestMethod.GET)
	public ResponseResult verify(
			@RequestParam(value = "ticket", required = true) String _token,
			HttpServletRequest request) throws IOException, ServletException {
		SsoCacheItem cacheItem = ssoCacheService.getByToken(_token);
		ResponseResult result = new ResponseResult();
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
					result.setSuccess(true);
					result.put("ticket", ssoCacheItem.getKey());
					result.put("user", ssoCacheItem.getValue());
					return result;
				}
			} else {
				ssoCacheService.delete(cacheItem);
			}
		}
		result.setSuccess(false);
		return result;

	}
	
}
