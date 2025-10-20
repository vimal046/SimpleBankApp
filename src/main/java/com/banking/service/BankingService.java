package com.banking.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.banking.dao.AccountDao;
import com.banking.dao.CustomerDao;
import com.banking.dao.TransactionDao;
import com.banking.model.Account;
import com.banking.model.Customer;
import com.banking.model.Transaction;

public class BankingService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BankingService.class);

	private CustomerDao customerDao;
	private AccountDao accountDao;
	private TransactionDao transactionDao;

	public void setCustomerDao(CustomerDao customerDao) {
		this.customerDao = customerDao;
	}

	public void setAccountDao(AccountDao accountDao) {
		this.accountDao = accountDao;
	}

	public void setTransactionDao(TransactionDao transactionDao) {
		this.transactionDao = transactionDao;
	}

	// generate unique account number
	private String generateAccountNumber() {
		Random random = new Random();
		long number = 1_000_000_000L + (long) (random.nextDouble() * 9_000_000_000L);
		return "ACC" + number;
	}

	// create account for customer
	public Account createAccount(String firstName, String lastName, String email, String phone,
			BigDecimal initialDeposit, String accountType) {

		// check if customer exists
		Customer customer = customerDao.getCustomerByEmail(email);

		if (customer == null) {
			// create new customer
			customer = Customer.builder()
					.firstName(firstName)
					.lastName(lastName)
					.email(email)
					.phone(phone)
					.build();
			int customerId = customerDao.createCustomer(customer);
			customer.setCustomerId(customerId);
			LOGGER.info("new customer created: customerId={}", customerId);
		}

		// create account
		String accountNumber = generateAccountNumber();

		Account account = Account.builder()
				.customerId(customer.getCustomerId())
				.accountNumber(accountNumber)
				.balance(initialDeposit)
				.accountType(accountType)
				.build();

		int accountId = accountDao.createAccount(account);
		account.setAccountId(accountId);

		// record initial deposit transaction if amount > 0
		if (initialDeposit.compareTo(BigDecimal.ZERO) > 0) {
			Transaction transaction = Transaction.builder()
					.accountId(accountId)
					.transactionType("DEPOSIT")
					.amount(initialDeposit)
					.description("initial deposit")
					.balanceAfter(initialDeposit)
					.build();
			transactionDao.createTransaction(transaction);
		}

		LOGGER.info("account created: accountId={},accountNumber={},initialBalance={}",
				accountId,
				accountNumber,
				initialDeposit);

		return accountDao.getAccountById(accountId);
	}

	// get account details
	public Account getAccountDetails(int accountId) {
		return accountDao.getAccountById(accountId);
	}

	// deposit money
	public Account deposit(int accountId, BigDecimal amount) {
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("deposit amount must be posetive");
		}

		Account account = accountDao.getAccountById(accountId);
		BigDecimal newBalance = account.getBalance()
				.add(amount);

		accountDao.updateBalance(accountId, newBalance);

		Transaction transaction = Transaction.builder()
				.accountId(accountId)
				.transactionType("DEPOSIT")
				.amount(amount)
				.description("Deposit")
				.balanceAfter(newBalance)
				.build();

		transactionDao.createTransaction(transaction);

		LOGGER.info("deposit successful: accountId={}, amount={}, newBalance={}", accountId, amount, newBalance);

		account.setBalance(newBalance);
		return account;
	}

	// withdraw money
	public Account withdraw(int accountId, BigDecimal amount) {

		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("withdraw amount must be posetive.");
		}

		Account account = accountDao.getAccountById(accountId);

		if (account.getBalance()
				.compareTo(amount) < 0) {
			throw new IllegalArgumentException("insufficient balance");
		}

		BigDecimal newBalance = account.getBalance()
				.subtract(amount);
		accountDao.updateBalance(accountId, newBalance);

		Transaction transaction = Transaction.builder()
				.accountId(accountId)
				.transactionType("WITHDRAWAL")
				.amount(amount)
				.description("withdrawal")
				.balanceAfter(newBalance)
				.build();

		transactionDao.createTransaction(transaction);

		LOGGER.info("withdrawal successful accountId={},amount={},newBalance={}", accountId, amount, newBalance);

		account.setBalance(newBalance);
		return account;
	}

	// transfer money between accounts
	public void transfer(int fromAccountId, int toAccountId, BigDecimal amount) {

		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("transfer amount must be posetive.");
		}

		if (fromAccountId == toAccountId) {
			throw new IllegalArgumentException("cannot transfer to the same account.");
		}

		Account fromAccount = accountDao.getAccountById(fromAccountId);
		Account toAccount = accountDao.getAccountById(toAccountId);

		if (fromAccount.getBalance()
				.compareTo(amount) < 0) {
			throw new IllegalArgumentException("insufficient balance to transfer.");
		}

		// debit from source account
		BigDecimal fromNewBalance = fromAccount.getBalance()
				.subtract(amount);
		accountDao.updateBalance(fromAccountId, fromNewBalance);

		Transaction debitTransaction = Transaction.builder()
				.accountId(fromAccountId)
				.transactionType("TRANSFER_OUT")
				.amount(amount)
				.description("Transfer to " + toAccount.getAccountNumber())
				.balanceAfter(fromNewBalance)
				.build();
		transactionDao.createTransaction(debitTransaction);

		// credit to destination account
		BigDecimal toNewBalance = toAccount.getBalance()
				.add(amount);
		accountDao.updateBalance(toAccountId, toNewBalance);

		Transaction creditTransaction = Transaction.builder()
				.accountId(toAccountId)
				.transactionType("TRANSFER_IN")
				.amount(amount)
				.description("Transfer from " + fromAccount.getAccountNumber())
				.balanceAfter(toNewBalance)
				.build();
		transactionDao.createTransaction(creditTransaction);

		LOGGER.info("transfer successful: fromAccountId={}, toAccountId={},amount={},fromBalance={},toBalance={}",
				fromAccountId,
				toAccountId,
				amount,
				fromNewBalance,
				toNewBalance);
	}

	// get transaction history
	public List<Transaction> getTransactionHistory(int accountId) {
		return transactionDao.getTransactionsByAccountId(accountId);
	}

	// get all accounts
	public List<Account> getAllAccounts() {
		return accountDao.getAllAccounts();
	}

	// get customer by Id
	public Customer getCustomerById(int customerId) {
		return customerDao.getCustomerById(customerId);
	}

}