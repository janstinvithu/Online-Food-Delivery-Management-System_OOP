package com.fooddelivery.model.delivery;

import com.fooddelivery.common.util.PersistentIds;
import java.time.LocalDate;
import java.time.LocalTime;

public class CourierShift {

    private Long id;
    private Long courierId;
    private LocalDate shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;

    public Long getId() { return id; }
    public void assignPersistentId(Long persistentId) { this.id = PersistentIds.assign(this.id, persistentId); }
    public Long getCourierId() { return courierId; }
    public void setCourierId(Long courierId) { this.courierId = courierId; }
    public LocalDate getShiftDate() { return shiftDate; }
    public void setShiftDate(LocalDate shiftDate) { this.shiftDate = shiftDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}
