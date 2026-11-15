package com.fooddelivery.service;

import com.fooddelivery.common.exception.ResourceNotFoundException;
import com.fooddelivery.model.customer.Customer;
import com.fooddelivery.model.customer.RegisteredCustomer;
import com.fooddelivery.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
    }

    public Optional<Customer> findCustomerByEmail(String email) {
        if (email == null) return Optional.empty();
        return customerRepository.findByEmailIgnoreCase(email.trim());
    }

    public List<Customer> searchCustomers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllCustomers();
        }
        return customerRepository.searchCustomers(query.trim());
    }

    public Customer registerCustomer(Customer customer) {
        if (customer.getId() == null && customerRepository.existsByEmailIgnoreCase(customer.getEmail())) {
            throw new IllegalArgumentException("Email is already registered: " + customer.getEmail());
        }
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Customer customer) {
        if (customer.getId() == null) {
            throw new IllegalArgumentException("Customer must have an ID to update");
        }
        return customerRepository.save(customer);
    }

    public Customer savePaymentMethod(Long customerId, String method) {
        RegisteredCustomer customer = requireRegistered(customerId);
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("Choose Cash or Card.");
        }
        String clean = method.trim().toUpperCase();
        if (!"CASH".equals(clean) && !"CARD".equals(clean)) {
            throw new IllegalArgumentException("Saved payment method must be CASH or CARD.");
        }
        customer.setPreferredPaymentMethod(clean);
        return customerRepository.save(customer);
    }

    public Customer clearPaymentMethod(Long customerId) {
        RegisteredCustomer customer = requireRegistered(customerId);
        customer.setPreferredPaymentMethod(null);
        return customerRepository.save(customer);
    }

    private RegisteredCustomer requireRegistered(Long customerId) {
        Customer customer = getCustomerById(customerId);
        if (!(customer instanceof RegisteredCustomer registered)) {
            throw new IllegalStateException("A saved payment method is available for registered customers.");
        }
        return registered;
    }

    public void deleteCustomer(Long id) {
        Customer customer = getCustomerById(id);
        customerRepository.delete(customer);
    }
}
