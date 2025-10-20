package com.banking.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.banking.model.Account;
import com.banking.model.Customer;
import com.banking.model.Transaction;
import com.banking.service.BankingService;

@Controller
public class BankingController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BankingController.class);

	private BankingService bankingService;

	public void setBankingService(BankingService bankingService) {
		this.bankingService = bankingService;
	}

	// check if user is logged in
	private boolean isLoggedIn(HttpSession session) {
		return session != null && session.getAttribute("loggedInUser") != null;
	}

	// get customer accounts only
	private List<Account> getCustomerAccounts(HttpSession session) {
		Integer customerId = (Integer) session.getAttribute("customerId");
		List<Account> allAccounts = bankingService.getAllAccounts();

		return allAccounts.stream()
				.filter(account -> account.getCustomerId()
						.equals(customerId))
				.collect(Collectors.toList());
	}

	// home page - redirects to login or dashboard
	@GetMapping("/")
	public String home(HttpSession session) {
		LOGGER.info("request: home page");
		if (isLoggedIn(session)) {
			return "redirect:/dashboard";
		}
		return "redirect:/login";
	}

	// show create account form
	@GetMapping("/createAccount")
	public String showCreateAccountForm(HttpSession session) {
		if (!isLoggedIn(session)) {
			return "redirect:/login";
		}
		LOGGER.info("request: show create account form.");
		return "create-account";
	}

	// process create account
	@PostMapping("/createAccount")
	public String createAccount(@RequestParam BigDecimal initialDeposit,
			@RequestParam String accountType,
			HttpSession session,
			Model model) {

		if (!isLoggedIn(session)) {
			return "redirect:/login";
		}

		Integer customerId = (Integer) session.getAttribute("customerId");

		LOGGER.info("request: create account - customerId{}, initialDeposit={},accountType={}",
				customerId,
				initialDeposit,
				accountType);

		try {
			// get customer info from service
			Customer customer = bankingService.getCustomerById(customerId);

			Account account = bankingService.createAccount(customer.getFirstName(),
					customer.getLastName(),
					customer.getEmail(),
					customer.getPhone(),
					initialDeposit,
					accountType);

			model.addAttribute("account", account);
			model.addAttribute("message", "Account created successfully !");

			LOGGER.info("response: account created - accountId={}, accountNumber={},balance={}",
					account.getAccountId(),
					account.getAccountNumber(),
					account.getBalance());

			return "account-details";

		} catch (Exception e) {
			model.addAttribute("error", "error creating account: " + e.getMessage());
			LOGGER.error("response: error creating account - {}", e.getMessage());
			return "create-account";
		}
	}

	// show account details form
	@GetMapping("/viewAccount")
	public String showViewAccountForm(HttpSession session,
			Model model) {

		if (!isLoggedIn(session)) {
			return "redirect:/login";
		}
		LOGGER.info("request: show view account form.");
		List<Account> accounts = getCustomerAccounts(session);
		model.addAttribute("accounts", accounts);

		return "view-account";
	}

	// view account details
	@PostMapping("/viewAccount")
	public String viewAccount(@RequestParam int accountId,
			HttpSession session,
			Model model) {

		if (!isLoggedIn(session)) {
			return "redirect:/login";
		}

		LOGGER.info("request: view account details - accountId={}", accountId);

		try {
			Account account = bankingService.getAccountDetails(accountId);

			// verify account belongs to logged-in user
			Integer customerId = (Integer) session.getAttribute("customerId");
			if (!account.getCustomerId()
					.equals(customerId)) {
				model.addAttribute("error", "unauthorised access to account");
				List<Account> accounts = getCustomerAccounts(session);
				model.addAttribute("accounts", accounts);
				return "view-account";
			}
			model.addAttribute("account", account);

			LOGGER.info("response: account details retrived - accountNumber={},balance={},",
					account.getAccountNumber(),
					account.getBalance());
			return "account-details";
		} catch (Exception e) {
			model.addAttribute("error", "error retriving account: " + e.getMessage());
			List<Account> accounts = getCustomerAccounts(session);
			model.addAttribute("accounts", accounts);
			LOGGER.error("response: error retriving account - {}", e.getMessage());
			return "view-account";
		}
	}

	// show deposit form
	@GetMapping("/deposit")
	public String showDepositForm(HttpSession session,
			Model model) {
		if (!isLoggedIn(session)) {
			return "redirect:/login";
		}
		LOGGER.info("request: show deposit form");
		List<Account> accounts = getCustomerAccounts(session);
		model.addAttribute("accounts", accounts);
		return "deposit";
	}

	// process deposit
	@PostMapping("/deposit")
	public String deposit(@RequestParam int accountId,
			@RequestParam BigDecimal amount,
			HttpSession session,
			Model model) {

		if (!isLoggedIn(session)) {
			return "redirect:/login";
		}

		LOGGER.info("request: deposit - accountId{},amount={},", accountId, amount);

		try {
			// verify account belongs to logged-in user
			Account checkAccount = bankingService.getAccountDetails(accountId);
			Integer customerId = (Integer) session.getAttribute("customerId");
			if (!checkAccount.getCustomerId()
					.equals(customerId)) {
				model.addAttribute("error", "unauthorized access to account.");
				List<Account> accounts = getCustomerAccounts(session);
				model.addAttribute("accounts", accounts);
				return "deposit";
			}

			Account account = bankingService.deposit(accountId, amount);
			model.addAttribute("account", account);
			model.addAttribute("message", "deposit successful");

			LOGGER.info("response: deposit successful - accountId={},newBalance={}", accountId, account.getBalance());
			return "account-details";
		} catch (Exception e) {
			model.addAttribute("error", "error processing deposit: " + e.getMessage());
			List<Account> accounts = getCustomerAccounts(session);
			model.addAttribute("accounts", accounts);
			LOGGER.error("response: error processing deposit - {}", e.getMessage());
			return "deposit";
		}
	}

	// show withdrawal form
	@GetMapping("/withdraw")
	public String showWithdrawForm(HttpSession session,
			Model model) {

		if (!isLoggedIn(session)) {
			return "redirect:/login";
		}

		LOGGER.info("request: show withdrawal form.");
		List<Account> accounts = getCustomerAccounts(session);
		model.addAttribute("accounts", accounts);

		return "withdraw";
	}

	// process withdrawal
	@PostMapping("/withdraw")
	public String withdraw(@RequestParam int accountId,
			@RequestParam BigDecimal amount,
			HttpSession session,
			Model model) {

		if (!isLoggedIn(session)) {
			return "redirect:/login";
		}

		LOGGER.info("request: withdraw - accountId={}, amount={}", accountId, amount);

		try {
			// verify account belongs to logged in customer
			Account checkAccount = bankingService.getAccountDetails(accountId);
			Integer customerId = (Integer) session.getAttribute("customerId");

			if (!checkAccount.getCustomerId()
					.equals(customerId)) {
				model.addAttribute("error", "unauthorized access to account.");
				List<Account> accounts = getCustomerAccounts(session);
				model.addAttribute("accounts", accounts);
				return "withdraw";
			}

			Account account = bankingService.withdraw(accountId, amount);
			model.addAttribute("account", account);
			model.addAttribute("message", "withdrawal successful !");

			LOGGER.info("response: withdrawal successful - accountId={},newBalance={}",
					accountId,
					account.getBalance());
			return "account-details";

		} catch (Exception e) {
			model.addAttribute("error", "error processing withdrawal: " + e.getMessage());
			List<Account> accounts = getCustomerAccounts(session);
			model.addAttribute("accounts", accounts);
			LOGGER.info("response: error processing withdrawal - {}", e.getMessage());
			return "withdraw";
		}
	}

	// show transfer form
	@GetMapping("/transfer")
	public String showTransferForm(HttpSession session,
			Model model) {
		if (!isLoggedIn(session)) {
			return "redirect:/login";
		}
		LOGGER.info("request: show transfer form.");
		List<Account> userAccounts = getCustomerAccounts(session);
		List<Account> allAccounts = bankingService.getAllAccounts();
		model.addAttribute("accounts", userAccounts);
		model.addAttribute("allAccounts", allAccounts);
		return "transfer";
	}

	// process transfer
	@PostMapping("/transfer")
	public String transfer(@RequestParam int fromAccountId,
			@RequestParam int toAccountId,
			@RequestParam BigDecimal amount,
			HttpSession session,
			Model model) {

		if (!isLoggedIn(session)) {
			return "redirect:/login";
		}

		LOGGER.info("request: transfer - fromAccountId={},toAccountId={},amount={}",
				fromAccountId,
				toAccountId,
				amount);

		try {
			// verify source account belongs to logged-in user.
			Account fromAccount = bankingService.getAccountDetails(fromAccountId);
			Integer customerId = (Integer) session.getAttribute("customerId");

			if (!fromAccount.getCustomerId()
					.equals(customerId)) {
				model.addAttribute("error", "unauthorized access to source account");
				List<Account> userAccounts = getCustomerAccounts(session);
				List<Account> allAccounts = bankingService.getAllAccounts();
				model.addAttribute("accounts", userAccounts);
				model.addAttribute("allAccounts", allAccounts);
				return "transfer";
			}

			bankingService.transfer(fromAccountId, toAccountId, amount);
			Account account = bankingService.getAccountDetails(fromAccountId);
			model.addAttribute("account", account);
			model.addAttribute("message", "transfer successfully");

			LOGGER.info("response: transfer successful - fromAccountId={}, toAccountIs={},amount={}",
					fromAccount,
					toAccountId,
					amount);
			
			return "account-details";

		} catch (Exception e) {
			model.addAttribute("error", "Error processing transfer: " + e.getMessage());
            List<Account> userAccounts = getCustomerAccounts(session);
            List<Account> allAccounts = bankingService.getAllAccounts();
            model.addAttribute("accounts", userAccounts);
            model.addAttribute("allAccounts", allAccounts);
            LOGGER.error("Response: Error processing transfer - {}", e.getMessage());
            
            return "transfer";
		}
	}

	   // Show transaction history form
    @GetMapping("/transactionHistory")
    public String showTransactionHistoryForm(HttpSession session, Model model) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        LOGGER.info("Request: Show transaction history form");
        List<Account> accounts = getCustomerAccounts(session);
        model.addAttribute("accounts", accounts);
        return "transaction-history-form";
    }
    
    // View transaction history
    @PostMapping("/transactionHistory")
    public String transactionHistory(@RequestParam int accountId, HttpSession session, Model model) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        
        LOGGER.info("Request: Transaction history - accountId={}", accountId);
        
        try {
            Account account = bankingService.getAccountDetails(accountId);
            
            // Verify account belongs to logged-in user
            Integer customerId = (Integer) session.getAttribute("customerId");
            if (!account.getCustomerId().equals(customerId)) {
                model.addAttribute("error", "Unauthorized access to account");
                List<Account> accounts = getCustomerAccounts(session);
                model.addAttribute("accounts", accounts);
                return "transaction-history-form";
            }
            
            List<Transaction> transactions = bankingService.getTransactionHistory(accountId);
            
            model.addAttribute("account", account);
            model.addAttribute("transactions", transactions);
            
            LOGGER.info("Response: Transaction history retrieved - accountId={}, transactionCount={}", 
                       accountId, transactions.size());
            
            return "transaction-history";
        } catch (Exception e) {
            model.addAttribute("error", "Error retrieving transaction history: " + e.getMessage());
            List<Account> accounts = getCustomerAccounts(session);
            model.addAttribute("accounts", accounts);
            LOGGER.error("Response: Error retrieving transaction history - {}", e.getMessage());
            return "transaction-history-form";
        }
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}