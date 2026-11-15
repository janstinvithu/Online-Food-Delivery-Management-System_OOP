package com.fooddelivery.persistence.dto;

public class SequencesData {

    private long customerId;
    private long foodItemId;
    private long orderId;
    private long orderItemId;
    private long deliveryId;
    private long deliveryPersonId;
    private long paymentId;
    private long complaintId;
    private long reviewId;
    private long savedAddressId;
    private long deliveryZoneId;
    private long courierShiftId;
    private long checkpointId;
    private long refundId;

    public long nextCustomerId() {
        return ++customerId;
    }

    public long nextFoodItemId() {
        return ++foodItemId;
    }

    public long nextOrderId() {
        return ++orderId;
    }

    public long nextOrderItemId() {
        return ++orderItemId;
    }

    public long nextDeliveryId() {
        return ++deliveryId;
    }

    public long nextDeliveryPersonId() {
        return ++deliveryPersonId;
    }

    public long nextPaymentId() {
        return ++paymentId;
    }

    public long nextComplaintId() {
        return ++complaintId;
    }

    public long nextReviewId() {
        return ++reviewId;
    }

    public long nextSavedAddressId() {
        return ++savedAddressId;
    }

    public long nextDeliveryZoneId() {
        return ++deliveryZoneId;
    }

    public long nextCourierShiftId() {
        return ++courierShiftId;
    }

    public long nextCheckpointId() {
        return ++checkpointId;
    }

    public long nextRefundId() {
        return ++refundId;
    }

    public SequencesData copy() {
        SequencesData copy = new SequencesData();
        copy.raiseTo(this);
        return copy;
    }

    public void raiseTo(SequencesData other) {
        if (other == null) {
            return;
        }
        customerId = Math.max(customerId, other.customerId);
        foodItemId = Math.max(foodItemId, other.foodItemId);
        orderId = Math.max(orderId, other.orderId);
        orderItemId = Math.max(orderItemId, other.orderItemId);
        deliveryId = Math.max(deliveryId, other.deliveryId);
        deliveryPersonId = Math.max(deliveryPersonId, other.deliveryPersonId);
        paymentId = Math.max(paymentId, other.paymentId);
        complaintId = Math.max(complaintId, other.complaintId);
        reviewId = Math.max(reviewId, other.reviewId);
        savedAddressId = Math.max(savedAddressId, other.savedAddressId);
        deliveryZoneId = Math.max(deliveryZoneId, other.deliveryZoneId);
        courierShiftId = Math.max(courierShiftId, other.courierShiftId);
        checkpointId = Math.max(checkpointId, other.checkpointId);
        refundId = Math.max(refundId, other.refundId);
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public long getFoodItemId() {
        return foodItemId;
    }

    public void setFoodItemId(long foodItemId) {
        this.foodItemId = foodItemId;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public long getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(long deliveryId) {
        this.deliveryId = deliveryId;
    }

    public long getDeliveryPersonId() {
        return deliveryPersonId;
    }

    public void setDeliveryPersonId(long deliveryPersonId) {
        this.deliveryPersonId = deliveryPersonId;
    }

    public long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(long paymentId) {
        this.paymentId = paymentId;
    }

    public long getComplaintId() { return complaintId; }
    public void setComplaintId(long complaintId) { this.complaintId = complaintId; }
    public long getReviewId() { return reviewId; }
    public void setReviewId(long reviewId) { this.reviewId = reviewId; }
    public long getSavedAddressId() { return savedAddressId; }
    public void setSavedAddressId(long savedAddressId) { this.savedAddressId = savedAddressId; }
    public long getDeliveryZoneId() { return deliveryZoneId; }
    public void setDeliveryZoneId(long deliveryZoneId) { this.deliveryZoneId = deliveryZoneId; }
    public long getCourierShiftId() { return courierShiftId; }
    public void setCourierShiftId(long courierShiftId) { this.courierShiftId = courierShiftId; }
    public long getCheckpointId() { return checkpointId; }
    public void setCheckpointId(long checkpointId) { this.checkpointId = checkpointId; }
    public long getRefundId() { return refundId; }
    public void setRefundId(long refundId) { this.refundId = refundId; }
}
