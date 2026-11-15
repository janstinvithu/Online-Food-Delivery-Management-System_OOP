package com.fooddelivery.model.order;

import com.fooddelivery.common.util.PersistentIds;
import com.fooddelivery.model.food.FoodItem;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

public class OrderItem {

    private Long id;

    @NotNull(message = "Order reference is required")
    private FoodOrder order;

    @NotNull(message = "Food item is required")
    private FoodItem foodItem;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be positive")
    private BigDecimal unitPrice;

    @NotNull(message = "Subtotal is required")
    private BigDecimal subtotal;

    public OrderItem() {
    }

    public OrderItem(FoodOrder order, FoodItem foodItem, int quantity) {
        if (foodItem == null) {
            throw new IllegalArgumentException("Food item cannot be null");
        }
        this.order = order;
        this.foodItem = foodItem;
        this.unitPrice = foodItem.getPrice();
        setQuantity(quantity);
    }

    public Long getId() {
        return id;
    }

    public void assignPersistentId(Long persistentId) {
        this.id = PersistentIds.assign(this.id, persistentId);
    }

    public FoodOrder getOrder() {
        return order;
    }

    public void setOrder(FoodOrder order) {
        this.order = order;
    }

    public FoodItem getFoodItem() {
        return foodItem;
    }

    public void setFoodItem(FoodItem foodItem) {
        if (foodItem == null) {
            throw new IllegalArgumentException("Food item cannot be null");
        }
        this.foodItem = foodItem;
        this.unitPrice = foodItem.getPrice();
        recalculateSubtotal();
    }

    public int getQuantity() {
        return quantity;
    }

    public final void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive (> 0)");
        }
        this.quantity = quantity;
        recalculateSubtotal();
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be strictly positive");
        }
        this.unitPrice = unitPrice;
        recalculateSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    private void recalculateSubtotal() {
        if (unitPrice != null && quantity > 0) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return id != null && Objects.equals(id, orderItem.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
