package com.fooddelivery.controller;

import com.fooddelivery.model.customer.Customer;
import com.fooddelivery.model.customer.RegisteredCustomer;
import com.fooddelivery.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9\\s\\-]{8,20}$");

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String listCustomers(@RequestParam(value = "search", required = false) String search, Model model) {
        model.addAttribute("customers", customerService.searchCustomers(search));
        model.addAttribute("searchQuery", search);
        return "customer/customer-list";
    }

    @GetMapping({"/new", "/register"})
    public String showRegisterForm(@RequestParam(value = "tab", defaultValue = "registered") String tab, Model model) {
        if (!model.containsAttribute("activeTab")) {
            model.addAttribute("activeTab", tab);
        }
        return "customer/customer-register";
    }

    @PostMapping({"/new/guest", "/register/guest"})
    public String registerGuest(@RequestParam(required = false) String fullName,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String address,
                                Model model) {
        String cleanName = (fullName != null) ? fullName.trim() : "";
        String cleanEmail = (email != null) ? email.trim() : "";
        String cleanPhone = (phone != null) ? phone.trim() : "";
        String cleanAddress = (address != null) ? address.trim() : "";

        if (cleanName.length() < 2) {
            model.addAttribute("errorMessage", "Full name is required (at least 2 characters).");
            populateGuestForm(model, cleanName, cleanEmail, cleanPhone, cleanAddress);
            return "customer/customer-register";
        }
        if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            model.addAttribute("errorMessage", "Please provide a valid email address (e.g. name@domain.lk).");
            populateGuestForm(model, cleanName, cleanEmail, cleanPhone, cleanAddress);
            return "customer/customer-register";
        }
        if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
            model.addAttribute("errorMessage", "Please provide a valid contact phone number (e.g. +94-77-1234567).");
            populateGuestForm(model, cleanName, cleanEmail, cleanPhone, cleanAddress);
            return "customer/customer-register";
        }
        if (customerService.findCustomerByEmail(cleanEmail).isPresent()) {
            model.addAttribute("errorMessage", "An account with email '" + cleanEmail + "' is already registered. Please sign in or use a different email.");
            populateGuestForm(model, cleanName, cleanEmail, cleanPhone, cleanAddress);
            return "customer/customer-register";
        }

        try {
            Customer customer = new Customer(cleanName, cleanEmail, cleanPhone, cleanAddress);
            customerService.registerCustomer(customer);
            return "redirect:/login?registered=true&role=CUSTOMER";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            populateGuestForm(model, cleanName, cleanEmail, cleanPhone, cleanAddress);
            return "customer/customer-register";
        }
    }

    private void populateGuestForm(Model model, String name, String email, String phone, String address) {
        model.addAttribute("activeTab", "guest");
        model.addAttribute("guestFullName", name);
        model.addAttribute("guestEmail", email);
        model.addAttribute("guestPhone", phone);
        model.addAttribute("guestAddress", address);
    }

    @PostMapping({"/new/account", "/register/account"})
    public String registerAccount(@RequestParam(required = false) String fullName,
                                  @RequestParam(required = false) String email,
                                  @RequestParam(required = false) String phone,
                                  @RequestParam(required = false) String address,
                                  @RequestParam(defaultValue = "0") int loyaltyPoints,
                                  @RequestParam(defaultValue = "BRONZE") String membershipTier,
                                  @RequestParam(required = false) String preferredPaymentMethod,
                                  Model model) {
        String cleanName = (fullName != null) ? fullName.trim() : "";
        String cleanEmail = (email != null) ? email.trim() : "";
        String cleanPhone = (phone != null) ? phone.trim() : "";
        String cleanAddress = (address != null) ? address.trim() : "";

        if (cleanName.length() < 2) {
            model.addAttribute("errorMessage", "Full name is required (at least 2 characters).");
            populateRegForm(model, cleanName, cleanEmail, cleanPhone, cleanAddress, loyaltyPoints, membershipTier, preferredPaymentMethod);
            return "customer/customer-register";
        }
        if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            model.addAttribute("errorMessage", "Please provide a valid email address (e.g. name@domain.lk).");
            populateRegForm(model, cleanName, cleanEmail, cleanPhone, cleanAddress, loyaltyPoints, membershipTier, preferredPaymentMethod);
            return "customer/customer-register";
        }
        if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
            model.addAttribute("errorMessage", "Please provide a valid contact phone number (e.g. +94-77-1234567).");
            populateRegForm(model, cleanName, cleanEmail, cleanPhone, cleanAddress, loyaltyPoints, membershipTier, preferredPaymentMethod);
            return "customer/customer-register";
        }
        if (cleanAddress.isEmpty()) {
            model.addAttribute("errorMessage", "Delivery address is required for registered members.");
            populateRegForm(model, cleanName, cleanEmail, cleanPhone, cleanAddress, loyaltyPoints, membershipTier, preferredPaymentMethod);
            return "customer/customer-register";
        }
        if (loyaltyPoints < 0) {
            model.addAttribute("errorMessage", "Loyalty points cannot be negative.");
            populateRegForm(model, cleanName, cleanEmail, cleanPhone, cleanAddress, loyaltyPoints, membershipTier, preferredPaymentMethod);
            return "customer/customer-register";
        }
        if (customerService.findCustomerByEmail(cleanEmail).isPresent()) {
            model.addAttribute("errorMessage", "An account with email '" + cleanEmail + "' is already registered. Please sign in or use a different email.");
            populateRegForm(model, cleanName, cleanEmail, cleanPhone, cleanAddress, loyaltyPoints, membershipTier, preferredPaymentMethod);
            return "customer/customer-register";
        }

        try {
            RegisteredCustomer registered = new RegisteredCustomer(
                    cleanName, cleanEmail, cleanPhone, cleanAddress, loyaltyPoints, membershipTier, "password123", preferredPaymentMethod
            );
            customerService.registerCustomer(registered);
            return "redirect:/login?registered=true&role=CUSTOMER";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            populateRegForm(model, cleanName, cleanEmail, cleanPhone, cleanAddress, loyaltyPoints, membershipTier, preferredPaymentMethod);
            return "customer/customer-register";
        }
    }

    private void populateRegForm(Model model, String name, String email, String phone, String address,
                                 int points, String tier, String paymentMethod) {
        model.addAttribute("activeTab", "registered");
        model.addAttribute("regFullName", name);
        model.addAttribute("regEmail", email);
        model.addAttribute("regPhone", phone);
        model.addAttribute("regAddress", address);
        model.addAttribute("regLoyaltyPoints", points);
        model.addAttribute("regMembershipTier", tier);
        model.addAttribute("regPreferredPaymentMethod", paymentMethod);
    }

    @GetMapping("/profile")
    public String viewProfile(@RequestParam(value = "updated", required = false) String updated,
                              @RequestParam(value = "error", required = false) String error,
                              HttpSession session,
                              Model model) {
        Long customerId = (session != null) ? (Long) session.getAttribute("userId") : null;
        if (customerId == null) {
            return "redirect:/login";
        }
        Customer customer = customerService.getCustomerById(customerId);
        model.addAttribute("customer", customer);
        model.addAttribute("updated", updated != null);
        model.addAttribute("errorMessage", error);
        return "customer/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam(required = false) String fullName,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String address,
                                @RequestParam(required = false) String preferredPaymentMethod,
                                HttpSession session,
                                Model model) {
        Long customerId = (session != null) ? (Long) session.getAttribute("userId") : null;
        if (customerId == null) {
            return "redirect:/login";
        }

        String cleanName = (fullName != null) ? fullName.trim() : "";
        String cleanPhone = (phone != null) ? phone.trim() : "";
        String cleanAddress = (address != null) ? address.trim() : "";

        if (cleanName.length() < 2) {
            Customer customer = customerService.getCustomerById(customerId);
            model.addAttribute("customer", customer);
            model.addAttribute("errorMessage", "Full name must be at least 2 characters.");
            return "customer/profile";
        }
        if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
            Customer customer = customerService.getCustomerById(customerId);
            model.addAttribute("customer", customer);
            model.addAttribute("errorMessage", "Please provide a valid contact phone number.");
            return "customer/profile";
        }
        if (cleanAddress.isEmpty()) {
            Customer customer = customerService.getCustomerById(customerId);
            model.addAttribute("customer", customer);
            model.addAttribute("errorMessage", "Delivery address is required.");
            return "customer/profile";
        }

        Customer customer = customerService.getCustomerById(customerId);
        customer.setFullName(cleanName);
        customer.setPhone(cleanPhone);
        customer.setAddress(cleanAddress);
        if (customer instanceof RegisteredCustomer registered && preferredPaymentMethod != null) {
            registered.setPreferredPaymentMethod(preferredPaymentMethod);
        }
        customerService.updateCustomer(customer);
        session.setAttribute("userName", cleanName);
        return "redirect:/customers/profile?updated=true";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Customer customer = customerService.getCustomerById(id);
        model.addAttribute("customer", customer);
        return "customer/customer-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateCustomer(@PathVariable Long id,
                                 @RequestParam(required = false) String fullName,
                                 @RequestParam(required = false) String email,
                                 @RequestParam(required = false) String phone,
                                 @RequestParam(required = false) String address,
                                 @RequestParam(required = false) Integer loyaltyPoints,
                                 Model model) {
        Customer customer = customerService.getCustomerById(id);

        String cleanName = (fullName != null) ? fullName.trim() : "";
        String cleanEmail = (email != null) ? email.trim() : "";
        String cleanPhone = (phone != null) ? phone.trim() : "";

        if (cleanName.length() < 2) {
            model.addAttribute("customer", customer);
            model.addAttribute("errorMessage", "Full name is required (at least 2 characters).");
            return "customer/customer-edit";
        }
        if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            model.addAttribute("customer", customer);
            model.addAttribute("errorMessage", "Please provide a valid email address.");
            return "customer/customer-edit";
        }
        if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
            model.addAttribute("customer", customer);
            model.addAttribute("errorMessage", "Please provide a valid phone number.");
            return "customer/customer-edit";
        }

        Optional<Customer> existingWithEmail = customerService.findCustomerByEmail(cleanEmail);
        if (existingWithEmail.isPresent() && !existingWithEmail.get().getId().equals(id)) {
            model.addAttribute("customer", customer);
            model.addAttribute("errorMessage", "Email '" + cleanEmail + "' is already in use by another customer.");
            return "customer/customer-edit";
        }

        try {
            customer.setFullName(cleanName);
            customer.setEmail(cleanEmail);
            customer.setPhone(cleanPhone);
            customer.setAddress(address != null ? address.trim() : null);
            if (customer instanceof RegisteredCustomer registered && loyaltyPoints != null) {
                if (loyaltyPoints < 0) {
                    model.addAttribute("customer", customer);
                    model.addAttribute("errorMessage", "Loyalty points cannot be negative.");
                    return "customer/customer-edit";
                }
                registered.setLoyaltyPoints(loyaltyPoints);
            }
            customerService.updateCustomer(customer);
            return "redirect:/customers";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("customer", customer);
            model.addAttribute("errorMessage", ex.getMessage());
            return "customer/customer-edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return "redirect:/customers";
    }
}
