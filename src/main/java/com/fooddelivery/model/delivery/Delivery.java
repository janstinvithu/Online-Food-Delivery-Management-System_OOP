package com.fooddelivery.model.delivery;

import com.fooddelivery.model.order.OnlineOrder;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "deliveries")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tracking number is required")
    @Column(name = "tracking_number", nullable = false, unique = true, length = 36)
    private String trackingNumber;

    @Column(name = "delivery_status", nullable = false, length = 30)
    private String deliveryStatus; // PENDING_ASSIGNMENT, ASSIGNED, PICKED_UP, OUT_FOR_DELIVERY, DELIVERED, FAILED

    @Column(name = "departure_time")
    private LocalDateTime departureTime;

    @Column(name = "delivered_time")
    private LocalDateTime deliveredTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "delivery_person_id")
    private DeliveryPerson deliveryPerson;

    @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY)
    private OnlineOrder onlineOrder;

    public Delivery() {
        this.trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.deliveryStatus = "PENDING_ASSIGNMENT";
    }

    public Delivery(DeliveryPerson deliveryPerson) {
        this();
        if (deliveryPerson != null) {
            assignCourier(deliveryPerson);
        }
    }

    public Long getId() {
        return id;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        if (trackingNumber == null || trackingNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Tracking number cannot be blank");
        }
        this.trackingNumber = trackingNumber.trim();
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        if (deliveryStatus == null || deliveryStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Delivery status cannot be blank");
        }
        this.deliveryStatus = deliveryStatus.trim().toUpperCase();
        if ("OUT_FOR_DELIVERY".equals(this.deliveryStatus) && departureTime == null) {
            this.departureTime = LocalDateTime.now();
        } else if ("DELIVERED".equals(this.deliveryStatus)) {
            if (this.deliveredTime == null) {
                this.deliveredTime = LocalDateTime.now();
            }
            if (this.deliveryPerson != null) {
                this.deliveryPerson.setAvailable(true);
            }
        } else if ("CANCELLED".equals(this.deliveryStatus)) {
            if (this.deliveryPerson != null) {
                this.deliveryPerson.setAvailable(true);
            }
        }
    }

    public void markDelivered() {
        setDeliveryStatus("DELIVERED");
    }

    public void cancelDelivery() {
        setDeliveryStatus("CANCELLED");
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalDateTime getDeliveredTime() {
        return deliveredTime;
    }

    public void setDeliveredTime(LocalDateTime deliveredTime) {
        this.deliveredTime = deliveredTime;
    }

    public DeliveryPerson getDeliveryPerson() {
        return deliveryPerson;
    }

    public void setDeliveryPerson(DeliveryPerson deliveryPerson) {
        this.deliveryPerson = deliveryPerson;
    }

    public final void assignCourier(DeliveryPerson courier) {
        if (courier == null) {
            throw new IllegalArgumentException("Courier cannot be null");
        }
        this.deliveryPerson = courier;
        this.deliveryStatus = "ASSIGNED";
        courier.setAvailable(false);
    }

    public OnlineOrder getOnlineOrder() {
        return onlineOrder;
    }

    public void setOnlineOrder(OnlineOrder onlineOrder) {
        this.onlineOrder = onlineOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Delivery delivery = (Delivery) o;
        return id != null && Objects.equals(id, delivery.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
