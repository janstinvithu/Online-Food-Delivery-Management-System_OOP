package com.fooddelivery.repository;

import com.fooddelivery.model.customer.SavedAddress;
import com.fooddelivery.model.delivery.CourierShift;
import com.fooddelivery.model.delivery.DeliveryCheckpoint;
import com.fooddelivery.model.delivery.DeliveryZone;
import com.fooddelivery.model.delivery.Refund;
import com.fooddelivery.model.order.OrderComplaint;
import com.fooddelivery.model.order.OrderReview;
import java.util.List;
import java.util.Optional;

public interface OperationsRepository {

    OrderComplaint saveComplaint(OrderComplaint complaint);

    List<OrderComplaint> findComplaintsByOrderId(Long orderId);

    Optional<OrderComplaint> findComplaintById(Long id);

    void deleteComplaintById(Long id);

    OrderReview saveReview(OrderReview review);

    Optional<OrderReview> findReviewByOrderId(Long orderId);

    Optional<OrderReview> findReviewById(Long id);

    void deleteReviewById(Long id);

    SavedAddress saveAddress(SavedAddress address);

    List<SavedAddress> findAddressesByCustomerId(Long customerId);

    Optional<SavedAddress> findAddressById(Long id);

    void deleteAddressById(Long id);

    DeliveryZone saveZone(DeliveryZone zone);

    List<DeliveryZone> findAllZones();

    Optional<DeliveryZone> findZoneById(Long id);

    boolean zoneReferencedByOrder(Long zoneId);

    void deleteZoneById(Long id);

    CourierShift saveShift(CourierShift shift);

    List<CourierShift> findShiftsByCourierId(Long courierId);

    Optional<CourierShift> findShiftById(Long id);

    void deleteShiftById(Long id);

    DeliveryCheckpoint saveCheckpoint(DeliveryCheckpoint checkpoint);

    List<DeliveryCheckpoint> findCheckpointsByDeliveryId(Long deliveryId);

    Optional<DeliveryCheckpoint> findCheckpointById(Long id);

    void deleteCheckpointById(Long id);

    Refund saveRefund(Refund refund);

    java.util.List<Refund> findRefundsByPaymentId(Long paymentId);

    java.util.Optional<Refund> findRefundById(Long id);

    boolean refundExistsForPayment(Long paymentId);

    void deleteRefundById(Long id);
}
