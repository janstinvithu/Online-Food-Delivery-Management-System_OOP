package com.fooddelivery.service;

import com.fooddelivery.common.exception.ResourceNotFoundException;
import com.fooddelivery.model.food.Beverage;
import com.fooddelivery.model.food.FoodItem;
import com.fooddelivery.model.food.MainCourse;
import com.fooddelivery.repository.FoodItemRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
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

    public List<MainCourse> getMainCourses() {
        return foodItemRepository.findAllMainCourses();
    }

    public List<Beverage> getBeverages() {
        return foodItemRepository.findAllBeverages();
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

    public FoodItem saveFoodItem(FoodItem foodItem) {
        return foodItemRepository.save(foodItem);
    }

    public FoodItem toggleAvailability(Long id) {
        FoodItem item = getFoodItemById(id);
        item.toggleAvailability();
        return foodItemRepository.save(item);
    }

    public List<FoodItem> importFoodItems(List<FoodItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return foodItemRepository.saveAll(items);
    }

    public FoodItem updateFoodItem(Long id, String name, String description, BigDecimal price,
                                   int prepTime, boolean available,
                                   String cuisineType, Boolean vegetarian, String spiceLevel, String servingPortion,
                                   String volumeSize, Integer volumeMl, Boolean cold, Integer caffeineContentMg, String sugarLevel) {
        FoodItem item = getFoodItemById(id);
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setPreparationTimeMinutes(prepTime);
        item.setAvailable(available);

        if (item instanceof MainCourse mc) {
            if (cuisineType != null && !cuisineType.trim().isEmpty()) {
                mc.setCuisineType(cuisineType);
            }
            if (vegetarian != null) {
                mc.setVegetarian(vegetarian);
            }
            if (spiceLevel != null && !spiceLevel.trim().isEmpty()) {
                mc.setSpiceLevel(spiceLevel);
            }
            if (servingPortion != null && !servingPortion.trim().isEmpty()) {
                mc.setServingPortion(servingPortion);
            }
        } else if (item instanceof Beverage bev) {
            if (volumeSize != null && !volumeSize.trim().isEmpty()) {
                bev.setVolumeSize(volumeSize);
            }
            if (volumeMl != null && volumeMl > 0) {
                bev.setVolumeMl(volumeMl);
            }
            if (cold != null) {
                bev.setCold(cold);
            }
            if (caffeineContentMg != null && caffeineContentMg >= 0) {
                bev.setCaffeineContentMg(caffeineContentMg);
            }
            if (sugarLevel != null && !sugarLevel.trim().isEmpty()) {
                bev.setSugarLevel(sugarLevel);
            }
        }

        return foodItemRepository.save(item);
    }

    public void deleteFoodItem(Long id) {
        FoodItem item = getFoodItemById(id);
        foodItemRepository.delete(item);
    }
}
