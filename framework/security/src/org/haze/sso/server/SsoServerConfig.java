package org.haze.sso.server;

public final class SsoServerConfig {

	/**
	 * spring-cache中登录凭证的过期时间(小时为单位)
	 */
	private int cacheCredentialExpireHours;

	/**
	 * sso写cookie token时,cookie的过期时间(分钟为单位),null表示随浏览器进程
	 */
	private Integer cookieTokenExpriredMinutes;

	public int getCacheCredentialExpireHours() {
		return cacheCredentialExpireHours;
	}

	public void setCacheCredentialExpireHours(int cacheCredentialExpireHours) {
		this.cacheCredentialExpireHours = cacheCredentialExpireHours;
	}

	public Integer getCookieTokenExpriredMinutes() {
		return cookieTokenExpriredMinutes;
	}

	public void setCookieTokenExpriredMinutes(Integer cookieTokenExpriredMinutes) {
		this.cookieTokenExpriredMinutes = cookieTokenExpriredMinutes;
	}

}
