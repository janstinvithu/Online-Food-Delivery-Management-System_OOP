package com.fooddelivery.persistence.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryPersonData {

    private Long id;
    private String name;
    private String phone;
    private String vehicleType;
    private String vehiclePlateNumber;
    private boolean available = true;
    private BigDecimal rating;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public String getVehiclePlateNumber() { return vehiclePlateNumber; }
    public void setVehiclePlateNumber(String vehiclePlateNumber) { this.vehiclePlateNumber = vehiclePlateNumber; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }
}
