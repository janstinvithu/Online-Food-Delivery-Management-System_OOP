package com.fooddelivery.repository;

import com.fooddelivery.model.customer.Customer;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository {

    List<Customer> findAll();

    Optional<Customer> findById(Long id);

    Optional<Customer> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<Customer> searchCustomers(String query);

    <S extends Customer> S save(S customer);

    <S extends Customer> List<S> saveAll(Iterable<S> customers);

    void delete(Customer customer);

    long count();
}
