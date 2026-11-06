package com.fooddelivery.repository;

import com.fooddelivery.model.food.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    List<FoodItem> findByAvailableTrue();

    @Query("SELECT f FROM FoodItem f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<FoodItem> searchByName(@Param("keyword") String keyword);

    @Query("SELECT f FROM FoodItem f WHERE TYPE(f) = com.fooddelivery.model.food.MainCourse")
    List<FoodItem> findAllMainCourses();

    @Query("SELECT f FROM FoodItem f WHERE TYPE(f) = com.fooddelivery.model.food.Beverage")
    List<FoodItem> findAllBeverages();
}
