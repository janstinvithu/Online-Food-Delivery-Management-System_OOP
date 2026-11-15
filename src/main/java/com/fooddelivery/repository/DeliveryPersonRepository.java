package com.fooddelivery.repository;

import com.fooddelivery.model.delivery.DeliveryPerson;
import java.util.List;
import java.util.Optional;

public interface DeliveryPersonRepository {

    List<DeliveryPerson> findAll();

    Optional<DeliveryPerson> findById(Long id);

    List<DeliveryPerson> findByAvailableTrue();

    DeliveryPerson save(DeliveryPerson deliveryPerson);

    List<DeliveryPerson> saveAll(Iterable<DeliveryPerson> deliveryPersons);

    void deleteById(Long id);
}
