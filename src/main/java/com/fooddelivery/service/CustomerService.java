package com.fooddelivery.service;

import com.fooddelivery.common.exception.ResourceNotFoundException;
import com.fooddelivery.model.customer.Customer;
import com.fooddelivery.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
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

    @Transactional
    public Customer registerCustomer(Customer customer) {
        if (customer.getId() == null && customerRepository.existsByEmailIgnoreCase(customer.getEmail())) {
            throw new IllegalArgumentException("Email is already registered: " + customer.getEmail());
        }
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer updateCustomer(Customer customer) {
        if (customer.getId() == null) {
            throw new IllegalArgumentException("Customer must have an ID to update");
        }
        return customerRepository.save(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = getCustomerById(id);
        customerRepository.delete(customer);
    }
}
