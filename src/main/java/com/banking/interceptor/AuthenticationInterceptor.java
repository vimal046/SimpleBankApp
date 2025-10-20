package com.banking.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AuthenticationInterceptor implements HandlerInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		String uri = request.getRequestURI();
		logger.info("URI :"+uri);

		// Allow access to login, register, and static resources
		if (uri.endsWith("/login") || uri.endsWith("/register") || uri.contains("/css/") || uri.contains("/js/")
				|| uri.contains("/images/")) {
			return true;
		}

		HttpSession session = request.getSession(false);

		if (session == null || session.getAttribute("loggedInUser") == null) {
			logger.warn("Unauthorized access attempt: {}", uri);
			response.sendRedirect(request.getContextPath() + "/login");
			return false;
		}

		return true;
	}
}