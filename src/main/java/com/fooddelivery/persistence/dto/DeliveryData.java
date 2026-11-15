package com.fooddelivery.persistence.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryData {

    private Long id;
    private String trackingNumber;
    private String deliveryStatus;
    private LocalDateTime departureTime;
    private LocalDateTime deliveredTime;
    private Long deliveryPersonId;
    private Long onlineOrderId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
    public LocalDateTime getDeliveredTime() { return deliveredTime; }
    public void setDeliveredTime(LocalDateTime deliveredTime) { this.deliveredTime = deliveredTime; }
    public Long getDeliveryPersonId() { return deliveryPersonId; }
    public void setDeliveryPersonId(Long deliveryPersonId) { this.deliveryPersonId = deliveryPersonId; }
    public Long getOnlineOrderId() { return onlineOrderId; }
    public void setOnlineOrderId(Long onlineOrderId) { this.onlineOrderId = onlineOrderId; }
}
