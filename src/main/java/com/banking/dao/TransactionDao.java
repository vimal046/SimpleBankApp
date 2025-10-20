package com.banking.dao;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.banking.model.Transaction;

public class TransactionDao {

	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private RowMapper<Transaction> transactionRowMapper = (rs, rowNum) -> {

		Transaction transaction = Transaction.builder()
				.transactionId(rs.getInt("transaction_id"))
				.accountId(rs.getInt("account_id"))
				.transactionType(rs.getString("transaction_type"))
				.amount(rs.getBigDecimal("amount"))
				.description(rs.getString("description"))
				.transactionDate(rs.getTimestamp("transaction_date"))
				.balanceAfter(rs.getBigDecimal("balance_after"))
				.build();

		return transaction;
	};

	public int createTransaction(Transaction transaction) {
		String sql = "INSERT INTO transaction(account_id, transaction_type, amount, description, balance_after )"
				+ "VALUES (?, ?,?,?,?)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update((connection) -> {

			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, transaction.getAccountId());
			ps.setString(2, transaction.getTransactionType());
			ps.setBigDecimal(3, transaction.getAmount());
			ps.setString(4, transaction.getDescription());
			ps.setBigDecimal(5, transaction.getBalanceAfter());
			return ps;

		}, keyHolder);
		return keyHolder.getKey()
				.intValue();
	}

	public List<Transaction> getTransactionsByAccountId(int accountId) {
		String sql = "SELECT * FROM transaction WHERE account_id = ? ORDER BY transaction_date DESC";
		return jdbcTemplate.query(sql, transactionRowMapper, accountId);
	}

}