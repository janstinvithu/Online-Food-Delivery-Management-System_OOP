package com.fooddelivery.repository;

import com.fooddelivery.model.order.FoodOrder;
import java.util.List;
import java.util.Optional;

public interface FoodOrderRepository {

    Optional<FoodOrder> findById(Long id);

    Optional<FoodOrder> findByOrderNumber(String orderNumber);

    List<FoodOrder> findByOrderStatus(String orderStatus);

    List<FoodOrder> findAllWithDetails();

    Optional<FoodOrder> findByIdWithDetails(Long id);

    List<FoodOrder> findByCustomerIdWithDetails(Long customerId);

    <S extends FoodOrder> S save(S order);

    void archiveById(Long id);
}
