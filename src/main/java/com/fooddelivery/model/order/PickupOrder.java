package com.fooddelivery.model.order;

import com.fooddelivery.model.customer.Customer;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class PickupOrder extends FoodOrder {

    @NotBlank(message = "Pickup time slot is required")
    private String pickupTimeSlot; // e.g. "12:30 PM - 12:45 PM"

    @Min(value = 1, message = "Pickup window must be at least 1 minute")
    private int pickupWindowMinutes;

    @Min(value = 1, message = "Counter number must be positive")
    private int counterNumber;

    @NotBlank(message = "Pickup contact phone is required")
    private String pickupContactPhone;

    private boolean curbsidePickup;

    public PickupOrder() {
        super();
        this.pickupTimeSlot = "ASAP (15-20 mins)";
        this.pickupWindowMinutes = 20;
        this.counterNumber = 1;
    }

    public PickupOrder(Customer customer, String pickupTimeSlot, int pickupWindowMinutes,
                       int counterNumber, String pickupContactPhone, boolean curbsidePickup) {
        super(customer);
        setPickupTimeSlot(pickupTimeSlot);
        setPickupWindowMinutes(pickupWindowMinutes);
        setCounterNumber(counterNumber);
        setPickupContactPhone(pickupContactPhone);
        setCurbsidePickup(curbsidePickup);
    }

    public String getPickupTimeSlot() {
        return pickupTimeSlot;
    }

    public final void setPickupTimeSlot(String pickupTimeSlot) {
        if (pickupTimeSlot == null || pickupTimeSlot.trim().isEmpty()) {
            throw new IllegalArgumentException("Pickup time slot cannot be blank");
        }
        this.pickupTimeSlot = pickupTimeSlot.trim();
    }

    public int getPickupWindowMinutes() {
        return pickupWindowMinutes;
    }

    public final void setPickupWindowMinutes(int pickupWindowMinutes) {
        if (pickupWindowMinutes <= 0) {
            throw new IllegalArgumentException("Pickup window must be positive");
        }
        this.pickupWindowMinutes = pickupWindowMinutes;
    }

    public int getCounterNumber() {
        return counterNumber;
    }

    public final void setCounterNumber(int counterNumber) {
        if (counterNumber <= 0) {
            throw new IllegalArgumentException("Counter number must be positive");
        }
        this.counterNumber = counterNumber;
    }

    public String getPickupContactPhone() {
        return pickupContactPhone;
    }

    public final void setPickupContactPhone(String pickupContactPhone) {
        if (pickupContactPhone == null || pickupContactPhone.trim().isEmpty()) {
            throw new IllegalArgumentException("Pickup contact phone cannot be blank");
        }
        this.pickupContactPhone = pickupContactPhone.trim();
    }

    public boolean isCurbsidePickup() {
        return curbsidePickup;
    }

    public final void setCurbsidePickup(boolean curbsidePickup) {
        this.curbsidePickup = curbsidePickup;
    }

    @Override
    public String getFulfillmentType() {
        return (curbsidePickup ? "Curbside Pickup" : "Counter #" + counterNumber) + " (Slot: " + pickupTimeSlot + ")";
    }
}
