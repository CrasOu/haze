package org.haze.sso.server;

import java.util.Date;

import org.apache.log4j.Logger;
import org.haze.sso.server.dao.UserDetailsDao;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class HazeLoginAuthenticationProvider extends
		DaoAuthenticationProvider {

	UserDetailsDao userDetailsDao;
	
	Logger log = Logger.getLogger(this.getClass());

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {

		try {
			Authentication auth = super.authenticate(authentication);
			// if reach here, means login success, else exception will be thrown
			// reset the user_attempts
			log.debug("login successfully...");
			userDetailsDao.resetFailAttempts(authentication.getName());
			return auth;

		} catch (BadCredentialsException e) {
			log.debug("invalid username or password...");
			userDetailsDao.updateFailAttempts(authentication.getName());
			throw e;
		} catch (LockedException e) {
			log.debug("username is locked...");
			String error = "";
			UserAttempts userAttempts = userDetailsDao
					.getUserAttempts(authentication.getName());
			if (userAttempts != null) {
				Date lastAttempts = userAttempts.getLastModified();
				error = "因登录错误次数太多，该用户已被锁定! <br>用户名 : "
						+ authentication.getName() + "<br>锁定日期 : "
						+ lastAttempts + "<br/>请联系管理员解锁！";
			} else {
				error = e.getMessage();
			}
			throw new LockedException(error);
		}

	}

	public UserDetailsDao getUserDetailsDao() {
		return userDetailsDao;
	}

	public void setUserDetailsDao(UserDetailsDao userDetailsDao) {
		this.userDetailsDao = userDetailsDao;
	}

}
