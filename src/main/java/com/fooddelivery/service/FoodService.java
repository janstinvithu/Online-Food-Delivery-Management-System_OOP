package com.fooddelivery.service;

import com.fooddelivery.common.exception.ResourceNotFoundException;
import com.fooddelivery.model.food.FoodItem;
import com.fooddelivery.repository.FoodItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class FoodService {

    private final FoodItemRepository foodItemRepository;

    public FoodService(FoodItemRepository foodItemRepository) {
        this.foodItemRepository = foodItemRepository;
    }

    public List<FoodItem> getAllActiveFoodItems() {
        return foodItemRepository.findByAvailableTrue();
    }

    public List<FoodItem> getAllFoodItems() {
        return foodItemRepository.findAll();
    }

    public FoodItem getFoodItemById(Long id) {
        return foodItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FoodItem", "id", id));
    }

    public List<FoodItem> searchFoodItems(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllFoodItems();
        }
        return foodItemRepository.searchByName(keyword.trim());
    }

    @Transactional
    public FoodItem saveFoodItem(FoodItem foodItem) {
        return foodItemRepository.save(foodItem);
    }

    @Transactional
    public void deleteFoodItem(Long id) {
        FoodItem item = getFoodItemById(id);
        foodItemRepository.delete(item);
    }
}
