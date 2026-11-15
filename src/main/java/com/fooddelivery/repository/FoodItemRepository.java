package com.fooddelivery.repository;

import com.fooddelivery.model.food.Beverage;
import com.fooddelivery.model.food.FoodItem;
import com.fooddelivery.model.food.MainCourse;
import java.util.List;
import java.util.Optional;

public interface FoodItemRepository {

    List<FoodItem> findAll();

    Optional<FoodItem> findById(Long id);

    List<FoodItem> findByAvailableTrue();

    List<FoodItem> searchByName(String keyword);

    List<MainCourse> findAllMainCourses();

    List<Beverage> findAllBeverages();

    <S extends FoodItem> S save(S foodItem);

    <S extends FoodItem> List<S> saveAll(Iterable<S> foodItems);

    void delete(FoodItem foodItem);

    long count();
}
