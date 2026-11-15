package com.fooddelivery.model.order;

import com.fooddelivery.model.customer.Customer;
import com.fooddelivery.model.delivery.Delivery;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OnlineOrder extends FoodOrder {

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    private String specialDeliveryInstructions;

    private LocalDateTime estimatedDeliveryTime;

    @NotNull(message = "Delivery fee is required")
    @DecimalMin(value = "0.00", message = "Delivery fee cannot be negative")
    private BigDecimal deliveryFee = new BigDecimal("350.00"); // Standard LKR delivery fee

    private Delivery delivery;

    private Long deliveryZoneId;

    public OnlineOrder() {
        super();
        this.deliveryFee = new BigDecimal("350.00");
        this.estimatedDeliveryTime = LocalDateTime.now().plusMinutes(45);
    }

    public OnlineOrder(Customer customer, String deliveryAddress, String specialDeliveryInstructions, BigDecimal deliveryFee) {
        super(customer);
        setDeliveryAddress(deliveryAddress);
        setSpecialDeliveryInstructions(specialDeliveryInstructions);
        setDeliveryFee(deliveryFee);
        this.estimatedDeliveryTime = LocalDateTime.now().plusMinutes(45);
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public final void setDeliveryAddress(String deliveryAddress) {
        if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Delivery address cannot be blank");
        }
        this.deliveryAddress = deliveryAddress.trim();
    }

    public String getSpecialDeliveryInstructions() {
        return specialDeliveryInstructions;
    }

    public final void setSpecialDeliveryInstructions(String specialDeliveryInstructions) {
        this.specialDeliveryInstructions = (specialDeliveryInstructions != null) ? specialDeliveryInstructions.trim() : null;
    }

    public LocalDateTime getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public final void setDeliveryFee(BigDecimal deliveryFee) {
        if (deliveryFee == null || deliveryFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Delivery fee must be non-negative");
        }
        this.deliveryFee = deliveryFee;
        recalculateTotal();
    }

    public Long getDeliveryZoneId() {
        return deliveryZoneId;
    }

    public void setDeliveryZoneId(Long deliveryZoneId) {
        this.deliveryZoneId = deliveryZoneId;
    }

    public Delivery getDelivery() {
        return delivery;
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        if (delivery != null && delivery.getOnlineOrder() != this) {
            delivery.setOnlineOrder(this);
        }
    }

    @Override
    public void recalculateTotal() {
        super.recalculateTotal();
        if (deliveryFee != null && getTotalAmount() != null) {
            setTotalAmount(getTotalAmount().add(deliveryFee));
        }
    }

    @Override
    public String getFulfillmentType() {
        return "Online Delivery to: " + deliveryAddress;
    }
}
