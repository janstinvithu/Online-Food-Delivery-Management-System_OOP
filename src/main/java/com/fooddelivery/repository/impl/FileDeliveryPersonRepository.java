package com.fooddelivery.repository.impl;

import com.fooddelivery.model.delivery.DeliveryPerson;
import com.fooddelivery.persistence.AggregateWriter;
import com.fooddelivery.persistence.DataStoreManager;
import com.fooddelivery.persistence.DomainMapper;
import com.fooddelivery.repository.DeliveryPersonRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class FileDeliveryPersonRepository implements DeliveryPersonRepository {

    private final DataStoreManager store;
    private final DomainMapper mapper;
    private final AggregateWriter writer;

    public FileDeliveryPersonRepository(DataStoreManager store, DomainMapper mapper, AggregateWriter writer) {
        this.store = store;
        this.mapper = mapper;
        this.writer = writer;
    }

    @Override
    public List<DeliveryPerson> findAll() {
        return people();
    }

    @Override
    public Optional<DeliveryPerson> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return people().stream().filter(person -> id.equals(person.getId())).findFirst();
    }

    @Override
    public List<DeliveryPerson> findByAvailableTrue() {
        return people().stream().filter(DeliveryPerson::isAvailable).toList();
    }

    @Override
    public DeliveryPerson save(DeliveryPerson deliveryPerson) {
        return store.update(data -> {
            writer.writeDeliveryPerson(data, deliveryPerson);
            return deliveryPerson;
        });
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Courier id is required");
        }
        store.update(data -> {
            boolean removed = data.getDeliveryPersons().removeIf(person -> id.equals(person.getId()));
            if (!removed) {
                throw new com.fooddelivery.common.exception.ResourceNotFoundException("DeliveryPerson", "id", id);
            }
            return null;
        });
    }

    @Override
    public List<DeliveryPerson> saveAll(Iterable<DeliveryPerson> deliveryPersons) {
        return store.update(data -> {
            List<DeliveryPerson> saved = new ArrayList<>();
            for (DeliveryPerson person : deliveryPersons) {
                writer.writeDeliveryPerson(data, person);
                saved.add(person);
            }
            return saved;
        });
    }

    private List<DeliveryPerson> people() {
        return store.read(data -> mapper.toGraph(data).getDeliveryPersons());
    }
}
