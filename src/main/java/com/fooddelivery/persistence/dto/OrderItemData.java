package com.fooddelivery.persistence.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderItemData {

    private Long id;
    private Long foodItemId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFoodItemId() { return foodItemId; }
    public void setFoodItemId(Long foodItemId) { this.foodItemId = foodItemId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
