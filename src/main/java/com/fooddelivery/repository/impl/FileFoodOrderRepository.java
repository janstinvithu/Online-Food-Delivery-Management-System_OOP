package com.fooddelivery.repository.impl;

import com.fooddelivery.common.exception.ResourceNotFoundException;
import com.fooddelivery.model.order.FoodOrder;
import com.fooddelivery.persistence.AggregateWriter;
import com.fooddelivery.persistence.DataStoreManager;
import com.fooddelivery.persistence.DomainMapper;
import com.fooddelivery.persistence.dto.FoodOrderData;
import com.fooddelivery.repository.FoodOrderRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class FileFoodOrderRepository implements FoodOrderRepository {

    private final DataStoreManager store;
    private final DomainMapper mapper;
    private final AggregateWriter writer;

    public FileFoodOrderRepository(DataStoreManager store, DomainMapper mapper, AggregateWriter writer) {
        this.store = store;
        this.mapper = mapper;
        this.writer = writer;
    }

    @Override
    public Optional<FoodOrder> findById(Long id) {
        return findByIdWithDetails(id);
    }

    @Override
    public Optional<FoodOrder> findByOrderNumber(String orderNumber) {
        if (orderNumber == null) {
            return Optional.empty();
        }
        return orders().stream()
                .filter(order -> orderNumber.equals(order.getOrderNumber()))
                .findFirst();
    }

    @Override
    public List<FoodOrder> findByOrderStatus(String orderStatus) {
        if (orderStatus == null) {
            return List.of();
        }
        return recent(orders().stream()
                .filter(order -> orderStatus.equalsIgnoreCase(order.getOrderStatus()))
                .toList());
    }

    @Override
    public List<FoodOrder> findAllWithDetails() {
        return recent(orders());
    }

    @Override
    public Optional<FoodOrder> findByIdWithDetails(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return orders().stream().filter(order -> id.equals(order.getId())).findFirst();
    }

    @Override
    public List<FoodOrder> findByCustomerIdWithDetails(Long customerId) {
        if (customerId == null) {
            return List.of();
        }
        return recent(orders().stream()
                .filter(order -> order.getCustomer() != null && customerId.equals(order.getCustomer().getId()))
                .toList());
    }

    @Override
    public <S extends FoodOrder> S save(S order) {
        return store.update(data -> {
            writer.writeOrder(data, order, true);
            return order;
        });
    }

    @Override
    public void archiveById(Long id) {
        store.update(data -> {
            FoodOrderData order = data.getOrders().stream()
                    .filter(existing -> id.equals(existing.getId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("FoodOrder", "id", id));
            data.getOrders().remove(order);
            if (order.getDeliveryId() != null) {
                data.getDeliveries().removeIf(delivery -> order.getDeliveryId().equals(delivery.getId()));
                order.setDeliveryId(null);
            }
            data.getArchivedOrders().add(order);
            return null;
        });
    }

    private List<FoodOrder> orders() {
        return store.read(data -> mapper.toGraph(data).getOrders());
    }

    private List<FoodOrder> recent(List<FoodOrder> orders) {
        return orders.stream()
                .sorted(Comparator.comparing(FoodOrder::getOrderDateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }
}
