package com.fooddelivery.model.food;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "food_items")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "item_type", discriminatorType = DiscriminatorType.STRING)
public abstract class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Food name must not be blank")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be strictly positive")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "available", nullable = false)
    private boolean available = true;

    @Min(value = 1, message = "Preparation time must be at least 1 minute")
    @Column(name = "preparation_time_minutes", nullable = false)
    private int preparationTimeMinutes;

    protected FoodItem() {
        this.available = true;
    }

    protected FoodItem(String name, String description, BigDecimal price, int preparationTimeMinutes) {
        setName(name);
        setDescription(description);
        setPrice(price);
        setPreparationTimeMinutes(preparationTimeMinutes);
        this.available = true;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public final void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Food name cannot be null or empty");
        }
        this.name = name.trim();
    }

    public String getDescription() {
        return description;
    }

    public final void setDescription(String description) {
        this.description = (description != null) ? description.trim() : null;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public final void setPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be strictly positive (> 0.00)");
        }
        this.price = price;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getPreparationTimeMinutes() {
        return preparationTimeMinutes;
    }

    public final void setPreparationTimeMinutes(int preparationTimeMinutes) {
        if (preparationTimeMinutes <= 0) {
            throw new IllegalArgumentException("Preparation time must be positive (> 0)");
        }
        this.preparationTimeMinutes = preparationTimeMinutes;
    }

    @Transient
    public abstract String getFoodCategory();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodItem foodItem = (FoodItem) o;
        return id != null && Objects.equals(id, foodItem.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
