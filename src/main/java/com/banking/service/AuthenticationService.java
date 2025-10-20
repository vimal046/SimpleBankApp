package com.banking.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.banking.dao.CustomerDao;
import com.banking.dao.UserDao;
import com.banking.model.Customer;
import com.banking.model.User;

public class AuthenticationService {

	private static final String ALGORITHAM = "SHA-256";

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

	private UserDao userDao;
	private CustomerDao customerDao;

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public void setCustomerDao(CustomerDao customerDao) {
		this.customerDao = customerDao;
	}

	// Hash password using SHA-256
	private String hashPassword(String password) {
		try {
			MessageDigest digest = MessageDigest.getInstance(ALGORITHAM);
			byte[] hash = digest.digest(password.getBytes());
			return Base64.getEncoder()
					.encodeToString(hash);
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("Error hashing password ", e);
			throw new RuntimeException("Password hashing failed");
		}

	}

	// Register new customer with customer info.
	public User registerUser(String username, String password, String email, String firstName, String lastName,
			String phone) {

		// validate inputs
		if (username == null || username.trim()
				.isEmpty()) {
			throw new IllegalArgumentException("Username is required");
		}

		if (password == null || password.length() < 6) {
			throw new IllegalArgumentException("password must be atleast 6 characters.");
		}

		if (email == null || !email.contains("@")) {
			throw new IllegalArgumentException("valid email is required.");
		}

		// check if username is already exists
		if (userDao.usernameExists(username)) {
			throw new IllegalArgumentException("username is already registered");
		}

		// check if email is already exists
		if (userDao.emailExists(email)) {
			throw new IllegalArgumentException("email is already registered");
		}

		// create customer first
		Customer customer = Customer.builder()
				.firstName(firstName)
				.lastName(lastName)
				.email(email)
				.phone(phone)
				.build();
		int customerID = customerDao.createCustomer(customer);

		// create user account
		String hashedPassword = hashPassword(password);
		User user = User.builder()
				.username(username)
				.password(hashedPassword)
				.email(email)
				.role("CUSTOMER")
				.build();

		int userId = userDao.createUser(user);
		user.setUserId(userId);

		LOGGER.info("user registered: username={}, userId={},customerId={}", username, userId, customerID);
		return user;
	}

	// authenticate user
	public User authenticate(String username, String password) {
		if (username == null || password == null) {
			throw new IllegalArgumentException("username and password are required");
		}

		User user = userDao.getUserByUsername(username);

		if (user == null) {
			LOGGER.warn("login attempt failed: username not found - {} ", username);
			return null;
		}

		if (!user.isActive()) {
			LOGGER.warn("login attempt failed: account inactive - {}", username);
			throw new IllegalArgumentException("Account is inactive");
		}

		String hashPassword = hashPassword(password);

		if (user.getPassword()
				.equals(hashPassword)) {
			LOGGER.info("user authenticated successfully: username={}, userId-{}", username, user.getUserId());
			return user;
		} else {
			LOGGER.warn("login attempt failed: invalid password - {}", username);
			return null;
		}
	}

}