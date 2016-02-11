package org.haze.sso.server.dao.support;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.haze.sso.server.UserAttempts;
import org.haze.sso.server.dao.UserDetailsDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.security.authentication.LockedException;

public class UserDetailsDaoImpl extends JdbcDaoSupport implements
		UserDetailsDao {

	private static final String SQL_USERS_UPDATE_LOCKED = "UPDATE t_users SET d_accountnonlocked = ? WHERE d_username = ?";
	private static final String SQL_USERS_COUNT = "SELECT COUNT(*) FROM t_users WHERE d_username = ?";

	private static final String SQL_USER_ATTEMPTS_GET = "SELECT d_username username,d_attempts attempts,d_lastmodified lastmodified FROM t_user_attempts WHERE d_username = ?";
	private static final String SQL_USER_ATTEMPTS_INSERT = "INSERT INTO t_user_attempts (d_username, d_attempts, d_lastmodified) VALUES(?,?,?)";
	private static final String SQL_USER_ATTEMPTS_UPDATE_ATTEMPTS = "UPDATE t_user_attempts SET d_attempts = d_attempts + 1, d_lastmodified = ? WHERE d_username = ?";
	private static final String SQL_USER_ATTEMPTS_RESET_ATTEMPTS = "UPDATE t_user_attempts SET d_attempts = 0, d_lastmodified = null WHERE d_username = ?";

	private int maxAttempts = 3;

	@Autowired
	private DataSource dataSource;

	@PostConstruct
	private void initialize() {
		setDataSource(dataSource);
	}

	@Override
	public void updateFailAttempts(String username) {
		UserAttempts user = getUserAttempts(username);
		if (user == null) {
			if (isUserExists(username)) {
				// if no record, insert a new
				getJdbcTemplate().update(SQL_USER_ATTEMPTS_INSERT,
						new Object[] { username, 1, new Date() });
			}
		} else {

			if (isUserExists(username)) {
				// update attempts count, +1
				getJdbcTemplate().update(SQL_USER_ATTEMPTS_UPDATE_ATTEMPTS,
						new Object[] { new Date(), username });
			}

			if (user.getAttempts() + 1 >= maxAttempts) {
				// locked user
				getJdbcTemplate().update(SQL_USERS_UPDATE_LOCKED,
						new Object[] { false, username });
				// throw exception
				throw new LockedException("登录错误次数太多，该用户已被锁定!");
			}

		}
	}

	@Override
	public void resetFailAttempts(String username) {
		getJdbcTemplate().update(SQL_USER_ATTEMPTS_RESET_ATTEMPTS,
				new Object[] { username });

	}

	@Override
	public UserAttempts getUserAttempts(String username) {
		try {

			UserAttempts userAttempts = getJdbcTemplate().queryForObject(
					SQL_USER_ATTEMPTS_GET, new Object[] { username },
					new RowMapper<UserAttempts>() {
						public UserAttempts mapRow(ResultSet rs, int rowNum)
								throws SQLException {

							UserAttempts user = new UserAttempts();
							user.setUsername(rs.getString("username"));
							user.setAttempts(rs.getInt("attempts"));
							user.setLastModified(rs.getDate("lastModified"));

							return user;
						}

					});
			return userAttempts;

		} catch (EmptyResultDataAccessException e) {
			return null;
		}

	}

	private boolean isUserExists(String username) {

		boolean result = false;

		int count = getJdbcTemplate().queryForObject(SQL_USERS_COUNT,
				new Object[] { username }, Integer.class);
		if (count > 0) {
			result = true;
		}

		return result;
	}

	public int getMaxAttempts() {
		return maxAttempts;
	}

	public void setMaxAttempts(int maxAttempts) {
		this.maxAttempts = maxAttempts;
	}

}
