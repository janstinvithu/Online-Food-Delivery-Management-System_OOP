package com.fooddelivery.repository.impl;

import com.fooddelivery.model.customer.Customer;
import com.fooddelivery.persistence.AggregateWriter;
import com.fooddelivery.persistence.ApplicationData;
import com.fooddelivery.persistence.DataStoreManager;
import com.fooddelivery.persistence.DomainMapper;
import com.fooddelivery.persistence.dto.FoodOrderData;
import com.fooddelivery.repository.CustomerRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class FileCustomerRepository implements CustomerRepository {

    private final DataStoreManager store;
    private final DomainMapper mapper;
    private final AggregateWriter writer;

    public FileCustomerRepository(DataStoreManager store, DomainMapper mapper, AggregateWriter writer) {
        this.store = store;
        this.mapper = mapper;
        this.writer = writer;
    }

    @Override
    public List<Customer> findAll() {
        return graphCustomers();
    }

    @Override
    public Optional<Customer> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return graphCustomers().stream().filter(customer -> id.equals(customer.getId())).findFirst();
    }

    @Override
    public Optional<Customer> findByEmailIgnoreCase(String email) {
        if (email == null) {
            return Optional.empty();
        }
        String wanted = email.trim().toLowerCase(Locale.ROOT);
        return graphCustomers().stream()
                .filter(customer -> customer.getEmail() != null && customer.getEmail().equalsIgnoreCase(wanted))
                .findFirst();
    }

    @Override
    public boolean existsByEmailIgnoreCase(String email) {
        if (email == null) {
            return false;
        }
        String wanted = email.trim().toLowerCase(Locale.ROOT);
        return store.read(data -> data.getCustomers().stream()
                .anyMatch(customer -> customer.getEmail() != null && customer.getEmail().equalsIgnoreCase(wanted)));
    }

    @Override
    public List<Customer> searchCustomers(String query) {
        String wanted = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return graphCustomers().stream()
                .filter(customer -> customer.getFullName().toLowerCase(Locale.ROOT).contains(wanted)
                        || customer.getEmail().toLowerCase(Locale.ROOT).contains(wanted))
                .toList();
    }

    @Override
    public <S extends Customer> S save(S customer) {
        return store.update(data -> {
            writer.writeCustomer(data, customer);
            return customer;
        });
    }

    @Override
    public <S extends Customer> List<S> saveAll(Iterable<S> customers) {
        return store.update(data -> {
            List<S> saved = new ArrayList<>();
            for (S customer : customers) {
                writer.writeCustomer(data, customer);
                saved.add(customer);
            }
            return saved;
        });
    }

    @Override
    public void delete(Customer customer) {
        store.update(data -> {
            boolean referenced = referencesCustomer(data.getOrders(), customer.getId())
                    || referencesCustomer(data.getArchivedOrders(), customer.getId());
            if (referenced) {
                throw new IllegalStateException("Customer cannot be deleted while orders still reference this account");
            }
            data.getCustomers().removeIf(existing -> customer.getId().equals(existing.getId()));
            return null;
        });
    }

    @Override
    public long count() {
        return store.read(data -> data.getCustomers().size());
    }

    private List<Customer> graphCustomers() {
        return store.read(data -> mapper.toGraph(data).getCustomers());
    }

    private boolean referencesCustomer(List<FoodOrderData> orders, Long customerId) {
        for (FoodOrderData order : orders) {
            if (customerId.equals(order.getCustomerId())) {
                return true;
            }
        }
        return false;
    }
}
