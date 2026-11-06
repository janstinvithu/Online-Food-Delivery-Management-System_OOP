package com.fooddelivery.model.delivery;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "delivery_persons")
public class DeliveryPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Courier name is required")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Phone number is required")
    @Column(name = "phone", nullable = false, unique = true, length = 25)
    private String phone;

    @NotBlank(message = "Vehicle type is required")
    @Column(name = "vehicle_type", nullable = false, length = 30)
    private String vehicleType;

    @NotBlank(message = "Vehicle plate number is required")
    @Column(name = "vehicle_plate_number", nullable = false, length = 30)
    private String vehiclePlateNumber;

    @Column(name = "is_available", nullable = false)
    private boolean available = true;

    @DecimalMin(value = "1.00", message = "Minimum rating is 1.00")
    @DecimalMax(value = "5.00", message = "Maximum rating is 5.00")
    @Column(name = "rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal rating = new BigDecimal("5.00");

    @OneToMany(mappedBy = "deliveryPerson", fetch = FetchType.LAZY)
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

    @Transient
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
