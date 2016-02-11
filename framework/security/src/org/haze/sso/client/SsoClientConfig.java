package org.haze.sso.client;

public class SsoClientConfig {

	private String loginUrl;
	private String verifyUrl;
	private String logoutUrl;
	private String deleteUserUrl;
	private String updatePasswordUrl;
	private String addUserUrl;

	public String getLoginUrl() {
		return loginUrl;
	}

	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}

	public String getVerifyUrl() {
		return verifyUrl;
	}

	public void setVerifyUrl(String verifyUrl) {
		this.verifyUrl = verifyUrl;
	}

	public String getLogoutUrl() {
		return logoutUrl;
	}

	public void setLogoutUrl(String logoutUrl) {
		this.logoutUrl = logoutUrl;
	}

	public String getDeleteUserUrl() {
		return deleteUserUrl;
	}

	public void setDeleteUserUrl(String deleteUserUrl) {
		this.deleteUserUrl = deleteUserUrl;
	}

	public String getUpdatePasswordUrl() {
		return updatePasswordUrl;
	}

	public void setUpdatePasswordUrl(String updatePasswordUrl) {
		this.updatePasswordUrl = updatePasswordUrl;
	}

	public String getAddUserUrl() {
		return addUserUrl;
	}

	public void setAddUserUrl(String addUserUrl) {
		this.addUserUrl = addUserUrl;
	}

}
