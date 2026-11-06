package com.fooddelivery.model.order;

import com.fooddelivery.model.customer.Customer;
import com.fooddelivery.model.delivery.Delivery;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "online_orders")
@DiscriminatorValue("ONLINE")
@PrimaryKeyJoinColumn(name = "id")
public class OnlineOrder extends FoodOrder {

    @NotBlank(message = "Delivery address is required")
    @Column(name = "delivery_address", nullable = false, length = 255)
    private String deliveryAddress;

    @Column(name = "special_delivery_instructions", length = 255)
    private String specialDeliveryInstructions;

    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    @NotNull(message = "Delivery fee is required")
    @DecimalMin(value = "0.00", message = "Delivery fee cannot be negative")
    @Column(name = "delivery_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee = new BigDecimal("350.00"); // Standard LKR delivery fee

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

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
    @Transient
    public String getFulfillmentType() {
        return "Online Delivery to: " + deliveryAddress;
    }
}
