package com.fooddelivery.model.food;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Entity
@Table(name = "main_courses")
@DiscriminatorValue("MAIN_COURSE")
@PrimaryKeyJoinColumn(name = "id")
public class MainCourse extends FoodItem {

    @NotBlank(message = "Cuisine type is required")
    @Column(name = "cuisine_type", nullable = false, length = 50)
    private String cuisineType;

    @Column(name = "is_vegetarian", nullable = false)
    private boolean vegetarian;

    @NotBlank(message = "Spice level is required")
    @Column(name = "spice_level", nullable = false, length = 20)
    private String spiceLevel; // MILD, MEDIUM, HOT, EXTRA_HOT

    @NotBlank(message = "Serving portion is required")
    @Column(name = "serving_portion", nullable = false, length = 100)
    private String servingPortion;

    public MainCourse() {
        super();
        this.spiceLevel = "MILD";
        this.cuisineType = "General";
        this.servingPortion = "1 Person";
    }

    public MainCourse(String name, String description, BigDecimal price, int preparationTimeMinutes,
                      String cuisineType, boolean vegetarian, String spiceLevel, String servingPortion) {
        super(name, description, price, preparationTimeMinutes);
        setCuisineType(cuisineType);
        setVegetarian(vegetarian);
        setSpiceLevel(spiceLevel);
        setServingPortion(servingPortion);
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public final void setCuisineType(String cuisineType) {
        if (cuisineType == null || cuisineType.trim().isEmpty()) {
            throw new IllegalArgumentException("Cuisine type cannot be blank");
        }
        this.cuisineType = cuisineType.trim();
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public final void setVegetarian(boolean vegetarian) {
        this.vegetarian = vegetarian;
    }

    public String getSpiceLevel() {
        return spiceLevel;
    }

    public final void setSpiceLevel(String spiceLevel) {
        if (spiceLevel == null || spiceLevel.trim().isEmpty()) {
            throw new IllegalArgumentException("Spice level cannot be blank");
        }
        String upper = spiceLevel.trim().toUpperCase();
        if (!upper.matches("MILD|MEDIUM|HOT|EXTRA_HOT")) {
            throw new IllegalArgumentException("Spice level must be MILD, MEDIUM, HOT, or EXTRA_HOT");
        }
        this.spiceLevel = upper;
    }

    public String getServingPortion() {
        return servingPortion;
    }

    public final void setServingPortion(String servingPortion) {
        if (servingPortion == null || servingPortion.trim().isEmpty()) {
            throw new IllegalArgumentException("Serving portion cannot be blank");
        }
        this.servingPortion = servingPortion.trim();
    }

    @Override
    @Transient
    public String getFoodCategory() {
        return "Main Course (" + cuisineType + ")";
    }
}
