package com.fooddelivery.model.delivery;

import com.fooddelivery.common.util.PersistentIds;
import com.fooddelivery.model.order.OnlineOrder;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Delivery {

    private Long id;

    @NotBlank(message = "Tracking number is required")
    private String trackingNumber;

    private String deliveryStatus; // PENDING_ASSIGNMENT, ASSIGNED, PICKED_UP, OUT_FOR_DELIVERY, DELIVERED, FAILED

    private LocalDateTime departureTime;

    private LocalDateTime deliveredTime;

    private DeliveryPerson deliveryPerson;

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

    public void assignPersistentId(Long persistentId) {
        this.id = PersistentIds.assign(this.id, persistentId);
    }

    /**
     * Reloads stored delivery state without the status side effects used during live updates.
     */
    public void restorePersistedState(String status, LocalDateTime departure, LocalDateTime delivered, DeliveryPerson person) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Delivery status cannot be blank");
        }
        this.deliveryStatus = status.trim().toUpperCase();
        this.departureTime = departure;
        this.deliveredTime = delivered;
        this.deliveryPerson = person;
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
        } else if ("DELIVERED".equals(this.deliveryStatus) && deliveredTime == null) {
            this.deliveredTime = LocalDateTime.now();
            if (this.deliveryPerson != null) {
                this.deliveryPerson.setAvailable(true);
            }
        }
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
