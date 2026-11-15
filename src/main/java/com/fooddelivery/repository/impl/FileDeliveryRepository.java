package com.fooddelivery.repository.impl;

import com.fooddelivery.model.delivery.Delivery;
import com.fooddelivery.persistence.AggregateWriter;
import com.fooddelivery.persistence.DataStoreManager;
import com.fooddelivery.persistence.DomainMapper;
import com.fooddelivery.repository.DeliveryRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class FileDeliveryRepository implements DeliveryRepository {

    private final DataStoreManager store;
    private final DomainMapper mapper;
    private final AggregateWriter writer;

    public FileDeliveryRepository(DataStoreManager store, DomainMapper mapper, AggregateWriter writer) {
        this.store = store;
        this.mapper = mapper;
        this.writer = writer;
    }

    @Override
    public List<Delivery> findAll() {
        return deliveries();
    }

    @Override
    public Optional<Delivery> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return deliveries().stream().filter(delivery -> id.equals(delivery.getId())).findFirst();
    }

    @Override
    public Optional<Delivery> findByTrackingNumber(String trackingNumber) {
        if (trackingNumber == null) {
            return Optional.empty();
        }
        return deliveries().stream()
                .filter(delivery -> trackingNumber.equals(delivery.getTrackingNumber()))
                .findFirst();
    }

    @Override
    public List<Delivery> findByDeliveryStatus(String deliveryStatus) {
        if (deliveryStatus == null) {
            return List.of();
        }
        return deliveries().stream()
                .filter(delivery -> deliveryStatus.equalsIgnoreCase(delivery.getDeliveryStatus()))
                .toList();
    }

    @Override
    public Delivery save(Delivery delivery) {
        return store.update(data -> {
            writer.writeDelivery(data, delivery, true);
            return delivery;
        });
    }

    private List<Delivery> deliveries() {
        return store.read(data -> mapper.toGraph(data).getDeliveries());
    }
}
