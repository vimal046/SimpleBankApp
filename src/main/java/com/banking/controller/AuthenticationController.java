package com.banking.controller;

import com.banking.model.User;
import com.banking.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
public class AuthenticationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);

	private AuthenticationService authenticationService;

	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	// show login page
	@GetMapping("/login")
	public String showLoginPage() {
		LOGGER.info("request: show login page");
		return "login";
	}

	// process login
	@PostMapping("/login")
	public String login(@RequestParam String username,
			@RequestParam String password,
			HttpSession session,
			Model model) {

		LOGGER.info("request: login attempt - username={}", username);

		try {
			User user = authenticationService.authenticate(username, password);

			if (user != null) {
				// Store user i session
				session.setAttribute("loggedInUser", user);
				session.setAttribute("userId", user.getUserId());
				session.setAttribute("username", user.getUsername());
				session.setAttribute("role", user.getRole());
				session.setAttribute("customerId", user.getCustomerId());

				LOGGER.info("response: login successful - userId={},role={}", user.getUserId(), user.getRole());
				return "redirect:/dashboard";
			} else {
				model.addAttribute("error", "invalid username or password");
				LOGGER.info("response: login failed invalid credentials");
				return "login";
			}

		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			LOGGER.error("response: login error - {}", e.getMessage());
			return "login";
		}
	}

	// show registeration page
	@GetMapping("/register")
	public String showRegisterPage() {
		LOGGER.info("request: show registeration page");
		return "register";
	}

	// process registeration
	@PostMapping("/register")
	public String register(@RequestParam String username,
			@RequestParam String password,
			@RequestParam String confirmPassword,
			@RequestParam String email,
			@RequestParam String firstName,
			@RequestParam String lastName,
			@RequestParam String phone,
			Model model) {

		LOGGER.info("request: register user - username{}, email={}", username, email);

		try {
			// validate password match
			if (!password.equals(confirmPassword)) {
				model.addAttribute("error", "password do not match");
				return "register";
			}

			User user = authenticationService.registerUser(username, password, email, firstName, lastName, phone);

			model.addAttribute("success", "registeration successful ! please login.");
			LOGGER.info("response: registeration successful - userId{}", user.getUserId());
			return "login";
		} catch (IllegalArgumentException e) {
			model.addAttribute("error", e.getMessage());
			LOGGER.error("Response: Registration failed - {}", e.getMessage());
			return "register";
		} catch (Exception e) {
			model.addAttribute("error", "Registration failed: " + e.getMessage());
			LOGGER.error("Response: Registration error - {}", e.getMessage());
			return "register";
		}
	}

	// logout
	@GetMapping("/logout")
	public String logout(HttpSession session) {
		String username = (String) session.getAttribute("username");
		LOGGER.info("request: logout - username={}", username);

		session.invalidate();

		LOGGER.info("response: logout successful");
		return "redirect:/login?logout=true";
	}

	// dashboard (landing page after login)
	@GetMapping("/dashboard")
	public String dashboard(HttpSession session,
			Model model) {
		User user = (User) session.getAttribute("loggedInUser");

		if (user == null) {
			return "redirect:/login";
		}

		LOGGER.info("request: dashboard - userId={}", user.getUserId());
		model.addAttribute("user", user);
		return "dashboard";
	}

}