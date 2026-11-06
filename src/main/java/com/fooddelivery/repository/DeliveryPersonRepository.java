package com.fooddelivery.repository;

import com.fooddelivery.model.delivery.DeliveryPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeliveryPersonRepository extends JpaRepository<DeliveryPerson, Long> {

    List<DeliveryPerson> findByAvailableTrue();
}
