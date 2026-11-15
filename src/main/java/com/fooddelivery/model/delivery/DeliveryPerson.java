package com.fooddelivery.model.delivery;

import com.fooddelivery.common.util.PersistentIds;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DeliveryPerson {

    private Long id;

    @NotBlank(message = "Courier name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "Vehicle type is required")
    private String vehicleType;

    @NotBlank(message = "Vehicle plate number is required")
    private String vehiclePlateNumber;

    private boolean available = true;

    @DecimalMin(value = "1.00", message = "Minimum rating is 1.00")
    @DecimalMax(value = "5.00", message = "Maximum rating is 5.00")
    private BigDecimal rating = new BigDecimal("5.00");

    private final List<Delivery> deliveries = new ArrayList<>();

    public DeliveryPerson() {
        this.available = true;
    }

    public DeliveryPerson(String name, String phone, String vehicleType, String vehiclePlateNumber) {
        this();
        setName(name);
        setPhone(phone);
        setVehicleType(vehicleType);
        setVehiclePlateNumber(vehiclePlateNumber);
    }

    public Long getId() {
        return id;
    }

    public void assignPersistentId(Long persistentId) {
        this.id = PersistentIds.assign(this.id, persistentId);
    }

    public void restoreDelivery(Delivery delivery) {
        if (delivery != null && !deliveries.contains(delivery)) {
            deliveries.add(delivery);
        }
    }

    public String getName() {
        return name;
    }

    public final void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Courier name cannot be blank");
        }
        this.name = name.trim();
    }

    public String getPhone() {
        return phone;
    }

    public final void setPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be blank");
        }
        this.phone = phone.trim();
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public final void setVehicleType(String vehicleType) {
        if (vehicleType == null || vehicleType.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle type cannot be blank");
        }
        this.vehicleType = vehicleType.trim();
    }

    public String getVehiclePlateNumber() {
        return vehiclePlateNumber;
    }

    public final void setVehiclePlateNumber(String vehiclePlateNumber) {
        if (vehiclePlateNumber == null || vehiclePlateNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle plate number cannot be blank");
        }
        this.vehiclePlateNumber = vehiclePlateNumber.trim();
    }

    public String getVehicleDetails() {
        return vehicleType + " (" + vehiclePlateNumber + ")";
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        if (rating == null || rating.compareTo(new BigDecimal("1.00")) < 0 || rating.compareTo(new BigDecimal("5.00")) > 0) {
            throw new IllegalArgumentException("Rating must be between 1.00 and 5.00");
        }
        this.rating = rating;
    }

    public List<Delivery> getDeliveries() {
        return Collections.unmodifiableList(deliveries);
    }

    public void addDelivery(Delivery delivery) {
        if (delivery == null) {
            throw new IllegalArgumentException("Delivery cannot be null");
        }
        if (!deliveries.contains(delivery)) {
            deliveries.add(delivery);
            delivery.setDeliveryPerson(this);
            this.available = false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryPerson that = (DeliveryPerson) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
