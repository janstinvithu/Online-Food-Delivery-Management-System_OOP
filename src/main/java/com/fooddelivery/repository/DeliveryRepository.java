package com.fooddelivery.repository;

import com.fooddelivery.model.delivery.Delivery;
import java.util.List;
import java.util.Optional;

public interface DeliveryRepository {

    List<Delivery> findAll();

    Optional<Delivery> findById(Long id);

    Optional<Delivery> findByTrackingNumber(String trackingNumber);

    List<Delivery> findByDeliveryStatus(String deliveryStatus);

    Delivery save(Delivery delivery);
}
