package com.fooddelivery.service;

import com.fooddelivery.common.exception.ResourceNotFoundException;
import com.fooddelivery.model.customer.Customer;
import com.fooddelivery.model.delivery.CashPayment;
import com.fooddelivery.model.delivery.Delivery;
import com.fooddelivery.model.delivery.Payment;
import com.fooddelivery.model.food.FoodItem;
import com.fooddelivery.model.order.FoodOrder;
import com.fooddelivery.model.order.OnlineOrder;
import com.fooddelivery.model.order.PickupOrder;
import com.fooddelivery.repository.CustomerRepository;
import com.fooddelivery.repository.FoodItemRepository;
import com.fooddelivery.repository.FoodOrderRepository;
import com.fooddelivery.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class OrderService {

    private final FoodOrderRepository foodOrderRepository;
    private final FoodItemRepository foodItemRepository;
    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;

    public OrderService(FoodOrderRepository foodOrderRepository,
                        FoodItemRepository foodItemRepository,
                        CustomerRepository customerRepository,
                        PaymentRepository paymentRepository) {
        this.foodOrderRepository = foodOrderRepository;
        this.foodItemRepository = foodItemRepository;
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
    }

    public List<FoodOrder> getAllOrders() {
        return foodOrderRepository.findAllWithDetails();
    }

    public FoodOrder getOrderById(Long id) {
        return foodOrderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("FoodOrder", "id", id));
    }

    public FoodOrder getOrderByNumber(String orderNumber) {
        return foodOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("FoodOrder", "orderNumber", orderNumber));
    }

    public List<FoodOrder> getOrdersByCustomerId(Long customerId) {
        if (customerId == null) {
            return List.of();
        }
        return foodOrderRepository.findByCustomerIdWithDetails(customerId);
    }

    @Transactional
    public OnlineOrder createOnlineOrder(Long customerId, String address, String instructions,
                                         BigDecimal deliveryFee, Map<Long, Integer> itemsWithQuantities) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        OnlineOrder order = new OnlineOrder(customer, address, instructions, deliveryFee);
        attachItems(order, itemsWithQuantities);

        Delivery delivery = new Delivery();
        order.setDelivery(delivery);

        return foodOrderRepository.save(order);
    }

    @Transactional
    public PickupOrder createPickupOrder(Long customerId, String timeSlot, int pickupMinutes,
                                         int counterNumber, String contactPhone, boolean curbside,
                                         Map<Long, Integer> itemsWithQuantities) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        PickupOrder order = new PickupOrder(customer, timeSlot, pickupMinutes, counterNumber, contactPhone, curbside);
        attachItems(order, itemsWithQuantities);

        return foodOrderRepository.save(order);
    }

    private void attachItems(FoodOrder order, Map<Long, Integer> itemsWithQuantities) {
        if (itemsWithQuantities == null || itemsWithQuantities.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        for (Map.Entry<Long, Integer> entry : itemsWithQuantities.entrySet()) {
            FoodItem foodItem = foodItemRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("FoodItem", "id", entry.getKey()));
            order.addOrderItem(foodItem, entry.getValue());
        }
    }

    @Transactional
    public FoodOrder updateOrderStatus(Long orderId, String newStatus) {
        FoodOrder order = getOrderById(orderId);
        order.setOrderStatus(newStatus);
        return foodOrderRepository.save(order);
    }

    @Transactional
    public FoodOrder cancelOrder(Long orderId) {
        FoodOrder order = getOrderById(orderId);
        order.cancelOrder();
        return foodOrderRepository.save(order);
    }

    @Transactional
    public Payment attachCashPayment(Long orderId, BigDecimal amountTendered) {
        FoodOrder order = getOrderById(orderId);
        CashPayment cash = new CashPayment(order, order.getTotalAmount(), amountTendered);
        cash.setPaymentStatus("COMPLETED");
        if ("PENDING".equals(order.getOrderStatus())) {
            order.setOrderStatus("PREPARING");
        }
        return paymentRepository.save(cash);
    }
}
