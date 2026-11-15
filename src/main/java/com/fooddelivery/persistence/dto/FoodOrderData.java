package com.fooddelivery.persistence.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FoodOrderData {

    private Long id;
    private String type;
    private String orderNumber;
    private String orderStatus;
    private LocalDateTime orderDateTime;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private Long customerId;
    private List<OrderItemData> items = new ArrayList<>();
    private String deliveryAddress;
    private String specialDeliveryInstructions;
    private LocalDateTime estimatedDeliveryTime;
    private BigDecimal deliveryFee;
    private Long deliveryId;
    private Long deliveryZoneId;
    private String pickupTimeSlot;
    private Integer pickupWindowMinutes;
    private Integer counterNumber;
    private String pickupContactPhone;
    private Boolean curbsidePickup;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public LocalDateTime getOrderDateTime() { return orderDateTime; }
    public void setOrderDateTime(LocalDateTime orderDateTime) { this.orderDateTime = orderDateTime; }
    public BigDecimal getSubtotalAmount() { return subtotalAmount; }
    public void setSubtotalAmount(BigDecimal subtotalAmount) { this.subtotalAmount = subtotalAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public List<OrderItemData> getItems() { return items; }
    public void setItems(List<OrderItemData> items) { this.items = items; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getSpecialDeliveryInstructions() { return specialDeliveryInstructions; }
    public void setSpecialDeliveryInstructions(String specialDeliveryInstructions) { this.specialDeliveryInstructions = specialDeliveryInstructions; }
    public LocalDateTime getEstimatedDeliveryTime() { return estimatedDeliveryTime; }
    public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) { this.estimatedDeliveryTime = estimatedDeliveryTime; }
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }
    public Long getDeliveryId() { return deliveryId; }
    public void setDeliveryId(Long deliveryId) { this.deliveryId = deliveryId; }
    public Long getDeliveryZoneId() { return deliveryZoneId; }
    public void setDeliveryZoneId(Long deliveryZoneId) { this.deliveryZoneId = deliveryZoneId; }
    public String getPickupTimeSlot() { return pickupTimeSlot; }
    public void setPickupTimeSlot(String pickupTimeSlot) { this.pickupTimeSlot = pickupTimeSlot; }
    public Integer getPickupWindowMinutes() { return pickupWindowMinutes; }
    public void setPickupWindowMinutes(Integer pickupWindowMinutes) { this.pickupWindowMinutes = pickupWindowMinutes; }
    public Integer getCounterNumber() { return counterNumber; }
    public void setCounterNumber(Integer counterNumber) { this.counterNumber = counterNumber; }
    public String getPickupContactPhone() { return pickupContactPhone; }
    public void setPickupContactPhone(String pickupContactPhone) { this.pickupContactPhone = pickupContactPhone; }
    public Boolean getCurbsidePickup() { return curbsidePickup; }
    public void setCurbsidePickup(Boolean curbsidePickup) { this.curbsidePickup = curbsidePickup; }
}
