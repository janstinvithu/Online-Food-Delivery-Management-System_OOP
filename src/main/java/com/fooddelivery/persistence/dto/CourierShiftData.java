package com.fooddelivery.persistence.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.LocalTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CourierShiftData {
    private Long id;
    private Long courierId;
    private LocalDate shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCourierId() { return courierId; }
    public void setCourierId(Long courierId) { this.courierId = courierId; }
    public LocalDate getShiftDate() { return shiftDate; }
    public void setShiftDate(LocalDate shiftDate) { this.shiftDate = shiftDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}
