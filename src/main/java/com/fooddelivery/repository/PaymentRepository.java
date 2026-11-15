package com.fooddelivery.repository;

import com.fooddelivery.model.delivery.Payment;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    List<Payment> findAll();

    Optional<Payment> findById(Long id);

    Optional<Payment> findByTransactionReference(String transactionReference);

    Optional<Payment> findByOrderId(Long orderId);

    <S extends Payment> S save(S payment);

    void deleteByOrderId(Long orderId);
}
