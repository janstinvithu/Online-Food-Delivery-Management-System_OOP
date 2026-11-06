package com.fooddelivery.controller;

import com.fooddelivery.model.customer.Customer;
import com.fooddelivery.model.customer.RegisteredCustomer;
import com.fooddelivery.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {

    private final CustomerService customerService;

    public AuthController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                @RequestParam(value = "role", defaultValue = "CUSTOMER") String role,
                                HttpSession session,
                                Model model) {
        // If already logged in, route immediately to corresponding home
        if (session != null && session.getAttribute("userRole") != null) {
            String existingRole = (String) session.getAttribute("userRole");
            if ("ADMIN".equalsIgnoreCase(existingRole)) {
                return "redirect:/";
            } else if ("CUSTOMER".equalsIgnoreCase(existingRole)) {
                return "redirect:/orders/my-orders";
            }
        }

        model.addAttribute("error", error);
        model.addAttribute("logout", logout != null);
        model.addAttribute("activeRole", role.toUpperCase());
        return "auth/login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam(defaultValue = "CUSTOMER") String role,
                              @RequestParam String email,
                              @RequestParam String password,
                              HttpSession session) {
        String cleanRole = (role != null) ? role.trim().toUpperCase() : "CUSTOMER";
        String cleanEmail = (email != null) ? email.trim() : "";
        String cleanPassword = (password != null) ? password.trim() : "";

        if (cleanEmail.isEmpty() || cleanPassword.isEmpty()) {
            return "redirect:/login?error=Both+email+and+password+are+required.&role=" + cleanRole;
        }

        if ("ADMIN".equals(cleanRole)) {
            // Admin Authentication
            if ("admin@foodflow.lk".equalsIgnoreCase(cleanEmail) && "admin123".equals(cleanPassword)) {
                session.setAttribute("userRole", "ADMIN");
                session.setAttribute("userName", "System Administrator");
                session.setAttribute("userEmail", "admin@foodflow.lk");
                return "redirect:/";
            } else {
                return "redirect:/login?error=Invalid+Administrator+credentials.+Please+check+email+and+password.&role=ADMIN";
            }
        } else {
            // Customer Authentication
            Optional<Customer> customerOpt = customerService.findCustomerByEmail(cleanEmail);
            if (customerOpt.isEmpty()) {
                return "redirect:/login?error=No+customer+account+found+with+email:+" + cleanEmail + "&role=CUSTOMER";
            }

            Customer customer = customerOpt.get();
            boolean authenticated = false;

            if (customer instanceof RegisteredCustomer reg) {
                authenticated = reg.verifyPassword(cleanPassword);
            } else {
                // Regular customer login fallback
                authenticated = "password123".equals(cleanPassword) || !cleanPassword.isEmpty();
            }

            if (authenticated) {
                session.setAttribute("userRole", "CUSTOMER");
                session.setAttribute("userId", customer.getId());
                session.setAttribute("userName", customer.getFullName());
                session.setAttribute("userEmail", customer.getEmail());
                session.setAttribute("currentUser", customer);
                return "redirect:/orders/my-orders";
            } else {
                return "redirect:/login?error=Incorrect+password.+Use+password123+for+test+accounts.&role=CUSTOMER";
            }
        }
    }

    @GetMapping("/logout")
    public String handleLogout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login?logout=true";
    }

    @PostMapping("/logout")
    public String handlePostLogout(HttpSession session) {
        return handleLogout(session);
    }
}
