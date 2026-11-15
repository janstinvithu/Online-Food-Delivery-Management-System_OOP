package com.fooddelivery.service;

import com.fooddelivery.common.exception.ResourceNotFoundException;
import com.fooddelivery.model.delivery.CardPayment;
import com.fooddelivery.model.delivery.CashPayment;
import com.fooddelivery.model.delivery.Delivery;
import com.fooddelivery.model.delivery.DeliveryPerson;
import com.fooddelivery.model.delivery.Payment;
import com.fooddelivery.model.order.FoodOrder;
import com.fooddelivery.repository.DeliveryPersonRepository;
import com.fooddelivery.repository.DeliveryRepository;
import com.fooddelivery.repository.FoodOrderRepository;
import com.fooddelivery.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryPersonRepository deliveryPersonRepository;
    private final PaymentRepository paymentRepository;
    private final FoodOrderRepository foodOrderRepository;

    public DeliveryService(DeliveryRepository deliveryRepository,
                           DeliveryPersonRepository deliveryPersonRepository,
                           PaymentRepository paymentRepository,
                           FoodOrderRepository foodOrderRepository) {
        this.deliveryRepository = deliveryRepository;
        this.deliveryPersonRepository = deliveryPersonRepository;
        this.paymentRepository = paymentRepository;
        this.foodOrderRepository = foodOrderRepository;
    }

    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }

    public Delivery getDeliveryById(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "id", id));
    }

    public Delivery getDeliveryByTracking(String trackingNumber) {
        return deliveryRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "trackingNumber", trackingNumber));
    }

    public List<DeliveryPerson> getAllCouriers() {
        return deliveryPersonRepository.findAll();
    }

    public List<DeliveryPerson> getAvailableCouriers() {
        return deliveryPersonRepository.findByAvailableTrue();
    }

    public DeliveryPerson createCourier(String name, String phone, String vehicleType, String plateNumber, BigDecimal rating) {
        DeliveryPerson courier = new DeliveryPerson(name, phone, vehicleType, plateNumber);
        if (rating != null) {
            courier.setRating(rating);
        }
        return deliveryPersonRepository.save(courier);
    }

    public DeliveryPerson getCourierById(Long id) {
        return deliveryPersonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPerson", "id", id));
    }

    public DeliveryPerson updateCourier(Long id, String name, String phone, String vehicleType,
                                        String plateNumber, BigDecimal rating) {
        DeliveryPerson courier = getCourierById(id);
        courier.setName(name);
        courier.setPhone(phone);
        courier.setVehicleType(vehicleType);
        courier.setVehiclePlateNumber(plateNumber);
        if (rating != null) {
            courier.setRating(rating);
        }
        return deliveryPersonRepository.save(courier);
    }

    public void deleteCourier(Long id) {
        DeliveryPerson courier = getCourierById(id);
        boolean referenced = deliveryRepository.findAll().stream()
                .anyMatch(delivery -> delivery.getDeliveryPerson() != null
                        && id.equals(delivery.getDeliveryPerson().getId()));
        if (referenced || !courier.isAvailable()) {
            throw new IllegalStateException("This courier has a delivery record and cannot be deleted.");
        }
        deliveryPersonRepository.deleteById(id);
    }

    public Delivery assignCourier(Long deliveryId, Long courierId) {
        Delivery delivery = getDeliveryById(deliveryId);
        DeliveryPerson courier = deliveryPersonRepository.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPerson", "id", courierId));

        delivery.assignCourier(courier);
        delivery.setDeliveryStatus("OUT_FOR_DELIVERY");
        return deliveryRepository.save(delivery);
    }

    public Delivery updateDeliveryStatus(Long deliveryId, String status) {
        Delivery delivery = getDeliveryById(deliveryId);
        delivery.setDeliveryStatus(status);
        return deliveryRepository.save(delivery);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment for Order", "orderId", orderId));
    }

    public CardPayment processCardPayment(Long orderId, String cardLastFour, String cardType, String authCode, String gatewayTxId) {
        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("FoodOrder", "id", orderId));

        CardPayment payment = new CardPayment(order, order.getTotalAmount(), cardLastFour, cardType, authCode, gatewayTxId);
        payment.setPaymentStatus("AUTHORIZED");
        order.setOrderStatus("CONFIRMED");
        return paymentRepository.save(payment);
    }

    public Payment updatePayment(Long orderId, String status, BigDecimal amountTendered, String authorizationCode) {
        Payment payment = getPaymentByOrderId(orderId);
        if ("REFUNDED".equals(payment.getPaymentStatus())) {
            throw new IllegalStateException("A refunded payment cannot be changed.");
        }
        if (status != null && !status.isBlank()) {
            String next = status.trim().toUpperCase();
            if (!Set.of("PENDING", "AUTHORIZED", "COMPLETED", "FAILED", "REFUNDED").contains(next)) {
                throw new IllegalArgumentException("Payment status must be PENDING, AUTHORIZED, COMPLETED, FAILED, or REFUNDED.");
            }
            payment.setPaymentStatus(next);
        }
        if (payment instanceof CashPayment cash && amountTendered != null) {
            cash.setAmountTendered(amountTendered);
        }
        if (payment instanceof CardPayment card && authorizationCode != null && !authorizationCode.isBlank()) {
            card.setAuthorizationCode(authorizationCode.trim());
        }
        return paymentRepository.save(payment);
    }

    public void deletePayment(Long orderId) {
        Payment payment = getPaymentByOrderId(orderId);
        if (!"PENDING".equals(payment.getPaymentStatus()) && !"FAILED".equals(payment.getPaymentStatus())) {
            throw new IllegalStateException("Only a pending or failed payment can be deleted.");
        }
        paymentRepository.deleteByOrderId(orderId);
    }
}
