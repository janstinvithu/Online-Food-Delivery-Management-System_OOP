package com.fooddelivery.repository.impl;

import com.fooddelivery.common.exception.ResourceNotFoundException;
import com.fooddelivery.model.delivery.Payment;
import com.fooddelivery.persistence.AggregateWriter;
import com.fooddelivery.persistence.DataStoreManager;
import com.fooddelivery.persistence.DomainMapper;
import com.fooddelivery.repository.PaymentRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class FilePaymentRepository implements PaymentRepository {

    private final DataStoreManager store;
    private final DomainMapper mapper;
    private final AggregateWriter writer;

    public FilePaymentRepository(DataStoreManager store, DomainMapper mapper, AggregateWriter writer) {
        this.store = store;
        this.mapper = mapper;
        this.writer = writer;
    }

    @Override
    public List<Payment> findAll() {
        return payments();
    }

    @Override
    public Optional<Payment> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return payments().stream().filter(payment -> id.equals(payment.getId())).findFirst();
    }

    @Override
    public Optional<Payment> findByTransactionReference(String transactionReference) {
        if (transactionReference == null) {
            return Optional.empty();
        }
        return payments().stream()
                .filter(payment -> transactionReference.equals(payment.getTransactionReference()))
                .findFirst();
    }

    @Override
    public Optional<Payment> findByOrderId(Long orderId) {
        if (orderId == null) {
            return Optional.empty();
        }
        return payments().stream()
                .filter(payment -> payment.getOrder() != null && orderId.equals(payment.getOrder().getId()))
                .findFirst();
    }

    @Override
    public <S extends Payment> S save(S payment) {
        return store.update(data -> {
            writer.writePayment(data, payment, true);
            return payment;
        });
    }

    @Override
    public void deleteByOrderId(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order id is required");
        }
        store.update(data -> {
            boolean removed = data.getPayments().removeIf(payment -> orderId.equals(payment.getOrderId()));
            if (!removed) {
                throw new ResourceNotFoundException("Payment for Order", "orderId", orderId);
            }
            return null;
        });
    }

    private List<Payment> payments() {
        return store.read(data -> mapper.toGraph(data).getPayments());
    }
}
