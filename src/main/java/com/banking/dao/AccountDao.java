package com.banking.dao;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.banking.model.Account;
import com.banking.model.Customer;

public class AccountDao {

	private JdbcTemplate jdbcTemplate;

	// Setter Injection
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private RowMapper<Account> accountRowMapper = (rs, rowNum) -> {

		Account account = Account.builder()
				.accountId(rs.getInt("account_id"))
				.customerId(rs.getInt("customer_id"))
				.accountNumber(rs.getString("account_number"))
				.balance(rs.getBigDecimal("balance"))
				.accountType(rs.getString("account_type"))
				.createdDate(rs.getTimestamp("created_date"))
				.build();

		return account;
	};

	private RowMapper<Account> accountWithCustomerRowMapper = (rs, rowNum) -> {

		Customer customer = Customer.builder()
				.customerId(rs.getInt("customer_id"))
				.firstName(rs.getString("first_name"))
				.lastName(rs.getString("last_name"))
				.email(rs.getString("email"))
				.phone(rs.getString("phone"))
				.build();

		Account account = Account.builder()
				.accountId(rs.getInt("account_id"))
				.customerId(rs.getInt("customer_id"))
				.accountNumber(rs.getString("account_number"))
				.balance(rs.getBigDecimal("balance"))
				.accountType(rs.getString("account_type"))
				.createdDate(rs.getTimestamp("created_date"))
				.customer(customer)
				.build();

		return account;
	};

	public int createAccount(Account account) {
		String sql = "INSERT INTO account (customer_id,account_number,balance,account_type) VALUES (?,?,?,?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, account.getCustomerId());
			ps.setString(2, account.getAccountNumber());
			ps.setBigDecimal(3, account.getBalance());
			ps.setString(4, account.getAccountType());
			return ps;
		}, keyHolder);

		return keyHolder.getKey()
				.intValue();

	}

	public Account getAccountById(int accountId) {
		String sql = "SELECT a.*, c.first_name ,c.last_name , c.email , c.phone "
				+ "FROM account a JOIN customer c ON a.customer_id=c.customer_id " + "WHERE a.account_id=?";
		return jdbcTemplate.queryForObject(sql, accountWithCustomerRowMapper, accountId);
	}

	public List<Account> getAccountsByCustomerId(int customerId) {
		String sql = "SELECT * FROM account WHERE customer_id = ? ORDER BY account_id";
		return jdbcTemplate.query(sql, accountRowMapper, customerId);
	}

	public List<Account> getAllAccounts() {
		String sql = "SELECT a.*, c.first_name, c.last_name , c.email , c.phone "
				+ "FROM account a JOIN customer c ON a.customer_id = c.customer_id " + "ORDER BY a.account_id";
		return jdbcTemplate.query(sql, accountWithCustomerRowMapper);
	}

	public void updateBalance(int accountId, BigDecimal newBalance) {
		String sql = "UPDATE account SET balance = ? WHERE account_id = ?";
		jdbcTemplate.update(sql, newBalance, accountId);
	}
}