package com.banking.dao;

import com.banking.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

public class UserDao {

	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private RowMapper<User> userRMapper = (rs, rowNum) -> {

		User user = User.builder()
				.userId(rs.getInt("user_id"))
				.username(rs.getString("username"))
				.password(rs.getString("password"))
				.email(rs.getString("email"))
				.role(rs.getString("role"))
				.customerId(rs.getObject("customer_id") != null ? rs.getInt("customer_id") : null)
				.active(rs.getBoolean("active"))
				.createdDate(rs.getTimestamp("created_date"))
				.build();
		return user;
	};

	public int createUser(User user) {
		String sql = "INSERT INTO user (username, password, email, role, customer_id, active) VALUES(?,?,?,?,?,?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, user.getUsername());
			ps.setString(2, user.getPassword());
			ps.setString(3, user.getEmail());
			ps.setString(4, user.getRole());
			if (user.getCustomerId() != null) {
				ps.setInt(5, user.getCustomerId());
			} else {
				ps.setNull(5, java.sql.Types.INTEGER);
			}
			ps.setBoolean(6, user.isActive());
			return ps;

		}, keyHolder);

		return keyHolder.getKey()
				.intValue();
	}

	public User getUserById(int userId) {
		String sql = "SELECT * FROM user WHERE user_id=?";
		List<User> users = jdbcTemplate.query(sql, userRMapper, userId);
		return users.isEmpty() ? null : users.get(0);
	}

	public User getUserByUsername(String username) {
		String sql = "SELECT * FROM user WHERE username=?";
		List<User> users = jdbcTemplate.query(sql, userRMapper, username);
		return users.isEmpty() ? null : users.get(0);
	}

	public boolean usernameExists(String username) {
		String sql = "SELECT COUNT(*) FROM user WHERE username=?";
		Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
		return count != null && count > 0;
	}

	public boolean emailExists(String email) {
		String sql = "SELECT COUNT(*) FROM user WHERE email=?";
		Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
		return count != null && count > 0;
	}
}