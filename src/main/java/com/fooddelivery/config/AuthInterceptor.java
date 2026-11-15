package com.fooddelivery.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = (contextPath != null && !contextPath.isEmpty()) ? uri.substring(contextPath.length()) : uri;

        // Normalize path
        if (path.isEmpty()) {
            path = "/";
        }

        // 1. Allow Public Routes explicitly
        if (isPublicPath(path)) {
            return true;
        }

        // 2. Validate Session Authentication
        HttpSession session = request.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("userRole") : null;

        if (role == null) {
            response.sendRedirect(contextPath + "/login");
            return false;
        }

        // 3. Role-Based Access Enforcement ('CUSTOMER' vs 'ADMIN')
        if ("CUSTOMER".equalsIgnoreCase(role)) {
            // Customer is restricted from Admin Scope:
            // Admin Scope: /foods/**, /deliveries/**, /customers (audit directory/edit/delete), /orders/status/**, /orders (admin table), /
            if (isAdminScope(path)) {
                response.sendRedirect(contextPath + "/orders/my-orders");
                return false;
            }
        } else if ("ADMIN".equalsIgnoreCase(role)) {
            // Admin has broad oversight; redirect customer-only pages if accessed
            if (path.equals("/orders/my-orders")) {
                response.sendRedirect(contextPath + "/orders");
                return false;
            }
            if (path.equals("/customers/profile")) {
                response.sendRedirect(contextPath + "/customers");
                return false;
            }
        }

        return true;
    }

    private boolean isPublicPath(String path) {
        return path.equals("/login")
                || path.equals("/logout")
                || path.equals("/customers/new")
                || path.startsWith("/customers/new/")
                || path.equals("/customers/register")
                || path.startsWith("/customers/register/")
                || path.startsWith("/static/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.equals("/favicon.ico")
                || path.startsWith("/error");
    }

    private boolean isAdminScope(String path) {
        if (path.equals("/")) {
            return true;
        }
        if (path.startsWith("/foods")) {
            return true;
        }
        if (path.startsWith("/deliveries")) {
            return true;
        }
        if (path.equals("/orders")) {
            return true;
        }
        if (path.startsWith("/orders/") && !path.startsWith("/orders/my-orders") && !path.startsWith("/orders/new") && !path.startsWith("/orders/create")) {
            return true;
        }
        // Admin customer audit directory & mutations (excluding customer's self-profile and registration)
        if (path.startsWith("/customers")) {
            return !path.equals("/customers/profile") && !path.startsWith("/customers/profile/")
                    && !path.equals("/customers/new") && !path.startsWith("/customers/new/")
                    && !path.equals("/customers/register") && !path.startsWith("/customers/register/");
        }
        return false;
    }
}
