package com.fooddelivery.repository.impl;

import com.fooddelivery.model.food.Beverage;
import com.fooddelivery.model.food.FoodItem;
import com.fooddelivery.model.food.MainCourse;
import com.fooddelivery.persistence.AggregateWriter;
import com.fooddelivery.persistence.DataStoreManager;
import com.fooddelivery.persistence.DomainMapper;
import com.fooddelivery.persistence.dto.FoodOrderData;
import com.fooddelivery.persistence.dto.OrderItemData;
import com.fooddelivery.repository.FoodItemRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class FileFoodItemRepository implements FoodItemRepository {

    private final DataStoreManager store;
    private final DomainMapper mapper;
    private final AggregateWriter writer;

    public FileFoodItemRepository(DataStoreManager store, DomainMapper mapper, AggregateWriter writer) {
        this.store = store;
        this.mapper = mapper;
        this.writer = writer;
    }

    @Override
    public List<FoodItem> findAll() {
        return foods();
    }

    @Override
    public Optional<FoodItem> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return foods().stream().filter(item -> id.equals(item.getId())).findFirst();
    }

    @Override
    public List<FoodItem> findByAvailableTrue() {
        return foods().stream().filter(FoodItem::isAvailable).toList();
    }

    @Override
    public List<FoodItem> searchByName(String keyword) {
        String wanted = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return foods().stream()
                .filter(item -> item.getName().toLowerCase(Locale.ROOT).contains(wanted))
                .toList();
    }

    @Override
    public List<MainCourse> findAllMainCourses() {
        return foods().stream()
                .filter(MainCourse.class::isInstance)
                .map(MainCourse.class::cast)
                .toList();
    }

    @Override
    public List<Beverage> findAllBeverages() {
        return foods().stream()
                .filter(Beverage.class::isInstance)
                .map(Beverage.class::cast)
                .toList();
    }

    @Override
    public <S extends FoodItem> S save(S foodItem) {
        return store.update(data -> {
            writer.writeFoodItem(data, foodItem);
            return foodItem;
        });
    }

    @Override
    public <S extends FoodItem> List<S> saveAll(Iterable<S> foodItems) {
        return store.update(data -> {
            List<S> saved = new ArrayList<>();
            for (S item : foodItems) {
                writer.writeFoodItem(data, item);
                saved.add(item);
            }
            return saved;
        });
    }

    @Override
    public void delete(FoodItem foodItem) {
        store.update(data -> {
            if (referenced(data.getOrders(), foodItem.getId()) || referenced(data.getArchivedOrders(), foodItem.getId())) {
                throw new IllegalStateException("Food item cannot be deleted because an order still references it");
            }
            data.getFoodItems().removeIf(existing -> foodItem.getId().equals(existing.getId()));
            return null;
        });
    }

    @Override
    public long count() {
        return store.read(data -> (long) data.getFoodItems().size());
    }

    private List<FoodItem> foods() {
        return store.read(data -> mapper.toGraph(data).getFoodItems());
    }

    private boolean referenced(List<FoodOrderData> orders, Long foodId) {
        for (FoodOrderData order : orders) {
            if (order.getItems() == null) {
                continue;
            }
            for (OrderItemData item : order.getItems()) {
                if (foodId.equals(item.getFoodItemId())) {
                    return true;
                }
            }
        }
        return false;
    }
}
