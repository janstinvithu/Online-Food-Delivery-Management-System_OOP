package com.fooddelivery.service;

import com.fooddelivery.common.exception.ResourceNotFoundException;
import com.fooddelivery.model.delivery.CardPayment;
import com.fooddelivery.model.delivery.Delivery;
import com.fooddelivery.model.delivery.DeliveryPerson;
import com.fooddelivery.model.delivery.Payment;
import com.fooddelivery.model.order.FoodOrder;
import com.fooddelivery.repository.DeliveryPersonRepository;
import com.fooddelivery.repository.DeliveryRepository;
import com.fooddelivery.repository.FoodOrderRepository;
import com.fooddelivery.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
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

    @Transactional
    public Delivery assignCourier(Long deliveryId, Long courierId) {
        Delivery delivery = getDeliveryById(deliveryId);
        DeliveryPerson courier = deliveryPersonRepository.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPerson", "id", courierId));

        delivery.assignCourier(courier);
        delivery.setDeliveryStatus("OUT_FOR_DELIVERY");
        return deliveryRepository.save(delivery);
    }

    @Transactional
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

    @Transactional
    public CardPayment processCardPayment(Long orderId, String cardLastFour, String cardType, String authCode, String gatewayTxId) {
        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("FoodOrder", "id", orderId));

        CardPayment payment = new CardPayment(order, order.getTotalAmount(), cardLastFour, cardType, authCode, gatewayTxId);
        payment.setPaymentStatus("AUTHORIZED");
        order.setOrderStatus("CONFIRMED");
        return paymentRepository.save(payment);
    }
}
