package com.banking.dao;

import com.banking.model.Customer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

public class CustomerDao {

	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private RowMapper<Customer> custRowMapper = (rs, rowNum) -> {

		Customer customer = Customer.builder()
				.customerId(rs.getInt("customer_id"))
				.firstName(rs.getString("first_name"))
				.lastName(rs.getString("last_name"))
				.email(rs.getString("email"))
				.phone(rs.getString("phone"))
				.createdDate(rs.getTimestamp("created_date"))
				.build();
		return customer;
	};

	public int createCustomer(Customer customer) {
		String sql = "INSERT INTO customer (first_name, last_name, email, phone) VALUES (?, ?, ?, ?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

			ps.setString(1, customer.getFirstName());
			ps.setString(2, customer.getLastName());
			ps.setString(3, customer.getEmail());
			ps.setString(4, customer.getPhone());
			return ps;
		}, keyHolder);
		return keyHolder.getKey()
				.intValue();
	}

	public Customer getCustomerById(int customerId) {
		String sql = "SELECT * FROM customer WHERE customer_id=?";
		return jdbcTemplate.queryForObject(sql, custRowMapper, customerId);
	}

	public Customer getCustomerByEmail(String email) {
		String sql = "SELECT * FROM customer WHERE email=?";
		List<Customer> customers = jdbcTemplate.query(sql, custRowMapper, email);
		return customers.isEmpty() ? null : customers.get(0);
	}
}
