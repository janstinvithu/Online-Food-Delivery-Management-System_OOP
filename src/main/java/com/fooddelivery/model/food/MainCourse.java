package com.fooddelivery.model.food;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class MainCourse extends FoodItem {

    @NotBlank(message = "Cuisine type is required")
    private String cuisineType;

    private boolean vegetarian;

    @NotBlank(message = "Spice level is required")
    private String spiceLevel; // MILD, MEDIUM, HOT, EXTRA_HOT

    @NotBlank(message = "Serving portion is required")
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

    public void setCuisineType(String cuisineType) {
        if (cuisineType == null || cuisineType.trim().isEmpty()) {
            throw new IllegalArgumentException("Cuisine type cannot be blank");
        }
        this.cuisineType = cuisineType.trim();
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public void setVegetarian(boolean vegetarian) {
        this.vegetarian = vegetarian;
    }

    public String getSpiceLevel() {
        return spiceLevel;
    }

    public void setSpiceLevel(String spiceLevel) {
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

    public void setServingPortion(String servingPortion) {
        if (servingPortion == null || servingPortion.trim().isEmpty()) {
            throw new IllegalArgumentException("Serving portion cannot be blank");
        }
        this.servingPortion = servingPortion.trim();
    }

    @Override
    public String getFoodCategory() {
        return "Main Course (" + cuisineType + ")";
    }
}
