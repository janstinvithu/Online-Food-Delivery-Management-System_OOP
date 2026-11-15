package com.fooddelivery.service;

import com.fooddelivery.common.exception.ResourceNotFoundException;
import com.fooddelivery.model.customer.SavedAddress;
import com.fooddelivery.model.delivery.CourierShift;
import com.fooddelivery.model.delivery.DeliveryCheckpoint;
import com.fooddelivery.model.delivery.DeliveryZone;
import com.fooddelivery.model.delivery.Payment;
import com.fooddelivery.model.delivery.Refund;
import com.fooddelivery.model.order.FoodOrder;
import com.fooddelivery.model.order.OnlineOrder;
import com.fooddelivery.model.order.OrderComplaint;
import com.fooddelivery.model.order.OrderReview;
import com.fooddelivery.repository.CustomerRepository;
import com.fooddelivery.repository.DeliveryPersonRepository;
import com.fooddelivery.repository.DeliveryRepository;
import com.fooddelivery.repository.FoodOrderRepository;
import com.fooddelivery.repository.OperationsRepository;
import com.fooddelivery.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class OperationsService {

    private static final Set<String> CHECKPOINT_STEPS = Set.of("PICKED_UP", "ON_THE_WAY", "NEARBY");

    private final OperationsRepository records;
    private final FoodOrderRepository orders;
    private final CustomerRepository customers;
    private final DeliveryRepository deliveries;
    private final DeliveryPersonRepository couriers;
    private final PaymentRepository payments;

    public OperationsService(OperationsRepository records,
                             FoodOrderRepository orders,
                             CustomerRepository customers,
                             DeliveryRepository deliveries,
                             DeliveryPersonRepository couriers,
                             PaymentRepository payments) {
        this.records = records;
        this.orders = orders;
        this.customers = customers;
        this.deliveries = deliveries;
        this.couriers = couriers;
        this.payments = payments;
    }

    public void seedZonesIfEmpty() {
        if (!records.findAllZones().isEmpty()) {
            return;
        }
        createZone("Colombo 07", new BigDecimal("350.00"));
        createZone("Galle Fort", new BigDecimal("450.00"));
        createZone("Jaffna Town", new BigDecimal("550.00"));
    }

    public OrderComplaint createComplaint(Long orderId, Long customerId, String message) {
        FoodOrder order = requireOrder(orderId);
        requireCustomer(customerId);
        requireOwner(order, customerId);
        if (!"DELIVERED".equals(order.getOrderStatus()) && !"CANCELLED".equals(order.getOrderStatus())) {
            throw new IllegalStateException("A complaint can be added after the order is delivered or cancelled.");
        }
        OrderComplaint complaint = new OrderComplaint();
        complaint.setOrderId(orderId);
        complaint.setCustomerId(customerId);
        complaint.setMessage(clean(message, "Complaint"));
        complaint.setStatus("OPEN");
        complaint.setCreatedAt(LocalDateTime.now());
        return records.saveComplaint(complaint);
    }

    public List<OrderComplaint> getComplaints(Long orderId) {
        return records.findComplaintsByOrderId(orderId);
    }

    public OrderComplaint updateComplaint(Long id, Long customerId, String message, String status) {
        OrderComplaint complaint = requireComplaint(id);
        requireActor(complaint.getCustomerId(), customerId);
        complaint.setMessage(clean(message, "Complaint"));
        String next = status == null ? complaint.getStatus() : status.trim().toUpperCase();
        if (!"OPEN".equals(next) && !"RESOLVED".equals(next)) {
            throw new IllegalArgumentException("Complaint status must be OPEN or RESOLVED.");
        }
        complaint.setStatus(next);
        return records.saveComplaint(complaint);
    }

    public void deleteComplaint(Long id, Long customerId) {
        OrderComplaint complaint = requireComplaint(id);
        requireActor(complaint.getCustomerId(), customerId);
        if (!"OPEN".equals(complaint.getStatus())) {
            throw new IllegalStateException("Only an open complaint can be deleted.");
        }
        records.deleteComplaintById(id);
    }

    public OrderReview createReview(Long orderId, Long customerId, int rating, String comment) {
        FoodOrder order = requireOrder(orderId);
        requireCustomer(customerId);
        requireOwner(order, customerId);
        if (records.findReviewByOrderId(orderId).isPresent()) {
            throw new IllegalStateException("This order already has a review. Update it instead.");
        }
        OrderReview review = new OrderReview();
        review.setOrderId(orderId);
        review.setCustomerId(customerId);
        review.setRating(validRating(rating));
        review.setComment(clean(comment, "Review"));
        review.setCreatedAt(LocalDateTime.now());
        return records.saveReview(review);
    }

    public Optional<OrderReview> getReview(Long orderId) {
        return records.findReviewByOrderId(orderId);
    }

    public OrderReview updateReview(Long id, Long customerId, int rating, String comment) {
        OrderReview review = requireReview(id);
        requireActor(review.getCustomerId(), customerId);
        review.setRating(validRating(rating));
        review.setComment(clean(comment, "Review"));
        return records.saveReview(review);
    }

    public void deleteReview(Long id, Long customerId) {
        OrderReview review = requireReview(id);
        requireActor(review.getCustomerId(), customerId);
        records.deleteReviewById(id);
    }

    public OrderReview replyToReview(Long id, String reply) {
        OrderReview review = requireReview(id);
        review.setAdminReply(clean(reply, "Reply"));
        review.setRepliedAt(LocalDateTime.now());
        return records.saveReview(review);
    }

    public SavedAddress createAddress(Long customerId, String label, String addressLine) {
        requireCustomer(customerId);
        SavedAddress address = new SavedAddress();
        address.setCustomerId(customerId);
        address.setLabel(clean(label, "Address label"));
        address.setAddressLine(cleanAddress(addressLine));
        return records.saveAddress(address);
    }

    public List<SavedAddress> getAddresses(Long customerId) {
        return records.findAddressesByCustomerId(customerId);
    }

    public SavedAddress updateAddress(Long id, Long customerId, String label, String addressLine) {
        SavedAddress address = requireAddress(id);
        requireActor(address.getCustomerId(), customerId);
        address.setLabel(clean(label, "Address label"));
        address.setAddressLine(cleanAddress(addressLine));
        return records.saveAddress(address);
    }

    public void deleteAddress(Long id, Long customerId) {
        SavedAddress address = requireAddress(id);
        requireActor(address.getCustomerId(), customerId);
        records.deleteAddressById(id);
    }

    public DeliveryZone createZone(String areaName, BigDecimal fee) {
        DeliveryZone zone = new DeliveryZone();
        zone.setAreaName(clean(areaName, "Area name"));
        zone.setFee(validFee(fee));
        assertUniqueZoneName(zone.getAreaName(), null);
        return records.saveZone(zone);
    }

    public List<DeliveryZone> getZones() {
        return records.findAllZones();
    }

    public DeliveryZone getZone(Long id) {
        return records.findZoneById(id).orElseThrow(() -> new ResourceNotFoundException("DeliveryZone", "id", id));
    }

    public DeliveryZone updateZone(Long id, String areaName, BigDecimal fee) {
        DeliveryZone zone = getZone(id);
        zone.setAreaName(clean(areaName, "Area name"));
        zone.setFee(validFee(fee));
        assertUniqueZoneName(zone.getAreaName(), id);
        return records.saveZone(zone);
    }

    public void deleteZone(Long id) {
        getZone(id);
        if (records.zoneReferencedByOrder(id)) {
            throw new IllegalStateException("This delivery zone is used by an order and cannot be deleted.");
        }
        records.deleteZoneById(id);
    }

    public void attachZone(Long orderId, Long zoneId) {
        DeliveryZone zone = getZone(zoneId);
        FoodOrder order = requireOrder(orderId);
        if (!(order instanceof OnlineOrder online)) {
            throw new IllegalStateException("A delivery zone applies to an online order.");
        }
        online.setDeliveryZoneId(zone.getId());
        online.setDeliveryFee(zone.getFee());
        orders.save(online);
    }

    public CourierShift createShift(Long courierId, LocalDate date, LocalTime start, LocalTime end) {
        requireCourier(courierId);
        CourierShift shift = new CourierShift();
        shift.setCourierId(courierId);
        applyShift(shift, date, start, end);
        return records.saveShift(shift);
    }

    public List<CourierShift> getShifts(Long courierId) {
        return records.findShiftsByCourierId(courierId);
    }

    public CourierShift updateShift(Long id, LocalDate date, LocalTime start, LocalTime end) {
        CourierShift shift = requireShift(id);
        applyShift(shift, date, start, end);
        return records.saveShift(shift);
    }

    public void deleteShift(Long id) {
        CourierShift shift = requireShift(id);
        if (!shift.getShiftDate().atTime(shift.getStartTime()).isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("A shift that has already started cannot be deleted.");
        }
        records.deleteShiftById(id);
    }

    public DeliveryCheckpoint createCheckpoint(Long deliveryId, String step, String note) {
        requireDelivery(deliveryId);
        DeliveryCheckpoint checkpoint = new DeliveryCheckpoint();
        checkpoint.setDeliveryId(deliveryId);
        checkpoint.setStep(validStep(step));
        checkpoint.setNote(optionalNote(note));
        checkpoint.setRecordedAt(LocalDateTime.now());
        return records.saveCheckpoint(checkpoint);
    }

    public List<DeliveryCheckpoint> getCheckpoints(Long deliveryId) {
        return records.findCheckpointsByDeliveryId(deliveryId);
    }

    public DeliveryCheckpoint updateCheckpoint(Long id, String step, String note, LocalDateTime recordedAt) {
        DeliveryCheckpoint checkpoint = requireCheckpoint(id);
        checkpoint.setStep(validStep(step));
        checkpoint.setNote(optionalNote(note));
        if (recordedAt == null) {
            throw new IllegalArgumentException("Checkpoint time is required.");
        }
        checkpoint.setRecordedAt(recordedAt);
        return records.saveCheckpoint(checkpoint);
    }

    public void deleteCheckpoint(Long id) {
        requireCheckpoint(id);
        records.deleteCheckpointById(id);
    }

    public Refund createRefund(Long paymentId, BigDecimal amount, String reason) {
        Payment payment = requirePayment(paymentId);
        if (!"COMPLETED".equals(payment.getPaymentStatus())) {
            throw new IllegalStateException("A refund can be started for a completed payment.");
        }
        if (records.refundExistsForPayment(paymentId)) {
            throw new IllegalStateException("This payment already has a refund. Update it instead.");
        }
        Refund refund = new Refund();
        refund.setPaymentId(paymentId);
        refund.setOrderId(payment.getOrder().getId());
        refund.setAmount(validRefundAmount(amount == null ? payment.getAmount() : amount, payment.getAmount()));
        refund.setReason(clean(reason, "Refund reason"));
        refund.setStatus("REQUESTED");
        refund.setCreatedAt(LocalDateTime.now());
        return records.saveRefund(refund);
    }

    public List<Refund> getRefunds(Long paymentId) {
        return records.findRefundsByPaymentId(paymentId);
    }

    public boolean hasRefund(Long paymentId) {
        return records.refundExistsForPayment(paymentId);
    }

    public Refund updateRefund(Long id, BigDecimal amount, String reason, String status) {
        Refund refund = requireRefund(id);
        Payment payment = requirePayment(refund.getPaymentId());
        if ("REFUNDED".equals(payment.getPaymentStatus()) && !"PAID".equalsIgnoreCase(status)) {
            throw new IllegalStateException("A paid refund cannot be reopened.");
        }
        String next = status == null ? refund.getStatus() : status.trim().toUpperCase();
        if (!"REQUESTED".equals(next) && !"PAID".equals(next)) {
            throw new IllegalArgumentException("Refund status must be REQUESTED or PAID.");
        }
        refund.setAmount(validRefundAmount(amount == null ? refund.getAmount() : amount, payment.getAmount()));
        refund.setReason(clean(reason, "Refund reason"));
        refund.setStatus(next);
        Refund saved = records.saveRefund(refund);
        if ("PAID".equals(next) && !"REFUNDED".equals(payment.getPaymentStatus())) {
            payment.setPaymentStatus("REFUNDED");
            payments.save(payment);
        }
        return saved;
    }

    public void deleteRefund(Long id) {
        Refund refund = requireRefund(id);
        if (!"REQUESTED".equals(refund.getStatus())) {
            throw new IllegalStateException("Only a requested refund can be deleted.");
        }
        records.deleteRefundById(id);
    }

    private Payment requirePayment(Long paymentId) {
        return payments.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));
    }

    private Refund requireRefund(Long id) {
        return records.findRefundById(id).orElseThrow(() -> new ResourceNotFoundException("Refund", "id", id));
    }

    private BigDecimal validRefundAmount(BigDecimal amount, BigDecimal paymentAmount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Refund amount must be greater than zero.");
        }
        if (paymentAmount != null && amount.compareTo(paymentAmount) > 0) {
            throw new IllegalArgumentException("Refund amount cannot be more than the payment.");
        }
        return amount;
    }

    private void applyShift(CourierShift shift, LocalDate date, LocalTime start, LocalTime end) {
        if (date == null || start == null || end == null) {
            throw new IllegalArgumentException("Shift date, start time, and end time are required.");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Shift end time must be after the start time.");
        }
        shift.setShiftDate(date);
        shift.setStartTime(start);
        shift.setEndTime(end);
    }

    private void assertUniqueZoneName(String name, Long currentId) {
        boolean taken = records.findAllZones().stream()
                .anyMatch(zone -> zone.getAreaName().equalsIgnoreCase(name)
                        && (currentId == null || !currentId.equals(zone.getId())));
        if (taken) {
            throw new IllegalArgumentException("A delivery zone with that area name already exists.");
        }
    }

    private FoodOrder requireOrder(Long orderId) {
        return orders.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("FoodOrder", "id", orderId));
    }

    private void requireCustomer(Long customerId) {
        customers.findById(customerId).orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
    }

    private void requireCourier(Long courierId) {
        couriers.findById(courierId).orElseThrow(() -> new ResourceNotFoundException("DeliveryPerson", "id", courierId));
    }

    private void requireDelivery(Long deliveryId) {
        deliveries.findById(deliveryId).orElseThrow(() -> new ResourceNotFoundException("Delivery", "id", deliveryId));
    }

    private OrderComplaint requireComplaint(Long id) {
        return records.findComplaintById(id).orElseThrow(() -> new ResourceNotFoundException("OrderComplaint", "id", id));
    }

    private OrderReview requireReview(Long id) {
        return records.findReviewById(id).orElseThrow(() -> new ResourceNotFoundException("OrderReview", "id", id));
    }

    private SavedAddress requireAddress(Long id) {
        return records.findAddressById(id).orElseThrow(() -> new ResourceNotFoundException("SavedAddress", "id", id));
    }

    private CourierShift requireShift(Long id) {
        return records.findShiftById(id).orElseThrow(() -> new ResourceNotFoundException("CourierShift", "id", id));
    }

    private DeliveryCheckpoint requireCheckpoint(Long id) {
        return records.findCheckpointById(id).orElseThrow(() -> new ResourceNotFoundException("DeliveryCheckpoint", "id", id));
    }

    private void requireOwner(FoodOrder order, Long customerId) {
        if (order.getCustomer() == null || !customerId.equals(order.getCustomer().getId())) {
            throw new IllegalStateException("You can only change records for your own order.");
        }
    }

    private void requireActor(Long ownerId, Long actorId) {
        if (actorId != null && !actorId.equals(ownerId)) {
            throw new IllegalStateException("You can only change your own record.");
        }
    }

    private int validRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be from 1 to 5.");
        }
        return rating;
    }

    private BigDecimal validFee(BigDecimal fee) {
        if (fee == null || fee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Delivery fee cannot be negative.");
        }
        return fee;
    }

    private String validStep(String step) {
        String cleanStep = step == null ? "" : step.trim().toUpperCase();
        if (!CHECKPOINT_STEPS.contains(cleanStep)) {
            throw new IllegalArgumentException("Checkpoint step must be PICKED_UP, ON_THE_WAY, or NEARBY.");
        }
        return cleanStep;
    }

    private String optionalNote(String note) {
        if (note == null || note.isBlank()) {
            return null;
        }
        return note.trim();
    }

    private String clean(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        return value.trim();
    }

    private String cleanAddress(String addressLine) {
        String value = clean(addressLine, "Address");
        if (value.length() < 5) {
            throw new IllegalArgumentException("Address must be at least 5 characters.");
        }
        return value;
    }
}
