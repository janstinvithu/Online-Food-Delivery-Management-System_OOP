package com.fooddelivery.service;

import com.fooddelivery.common.exception.ResourceNotFoundException;
import com.fooddelivery.model.customer.Customer;
import com.fooddelivery.model.customer.RegisteredCustomer;
import com.fooddelivery.model.delivery.CashPayment;
import com.fooddelivery.model.delivery.Delivery;
import com.fooddelivery.model.delivery.Payment;
import com.fooddelivery.model.food.FoodItem;
import com.fooddelivery.model.order.FoodOrder;
import com.fooddelivery.model.order.OnlineOrder;
import com.fooddelivery.model.order.OrderSummaryStats;
import com.fooddelivery.model.order.PickupOrder;
import com.fooddelivery.repository.CustomerRepository;
import com.fooddelivery.repository.FoodItemRepository;
import com.fooddelivery.repository.FoodOrderRepository;
import com.fooddelivery.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    public List<FoodOrder> getOrdersByStatus(String status) {
        if (status == null || status.trim().isEmpty() || "ALL".equalsIgnoreCase(status.trim())) {
            return getAllOrders();
        }
        return foodOrderRepository.findByOrderStatusWithDetails(status.trim());
    }

    public List<FoodOrder> searchOrders(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllOrders();
        }
        return foodOrderRepository.searchOrdersWithDetails(keyword.trim());
    }

    public List<FoodOrder> getOrders(String status, String keyword) {
        boolean hasStatus = status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim());
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        if (hasStatus && hasKeyword) {
            return foodOrderRepository.searchOrdersByStatusWithDetails(status.trim(), keyword.trim());
        } else if (hasStatus) {
            return foodOrderRepository.findByOrderStatusWithDetails(status.trim());
        } else if (hasKeyword) {
            return foodOrderRepository.searchOrdersWithDetails(keyword.trim());
        } else {
            return foodOrderRepository.findAllWithDetails();
        }
    }

    public List<FoodOrder> getActiveOrders() {
        return foodOrderRepository.findActiveOrdersWithDetails();
    }

    public List<FoodOrder> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            return getAllOrders();
        }
        return foodOrderRepository.findOrdersByDateRangeWithDetails(startDate, endDate);
    }

    public OrderSummaryStats getOrderSummaryStats() {
        long total = foodOrderRepository.count();
        long active = foodOrderRepository.findActiveOrdersWithDetails().size();
        long delivered = foodOrderRepository.countByOrderStatus("DELIVERED");
        long cancelled = foodOrderRepository.countByOrderStatus("CANCELLED");
        BigDecimal revenue = foodOrderRepository.calculateDeliveredRevenue();
        BigDecimal discounts = foodOrderRepository.calculateTotalDiscountsGiven();
        return new OrderSummaryStats(total, active, delivered, cancelled, revenue, discounts);
    }

    public long countOrdersByStatus(String status) {
        if (status == null) return 0;
        return foodOrderRepository.countByOrderStatus(status.trim().toUpperCase());
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
        if ("DELIVERED".equalsIgnoreCase(newStatus)) {
            if (order instanceof OnlineOrder onlineOrder && onlineOrder.getDelivery() != null) {
                onlineOrder.getDelivery().markDelivered();
            }
            if (order.getCustomer() instanceof RegisteredCustomer registered) {
                int pointsEarned = order.getTotalAmount().divideToIntegralValue(new BigDecimal("100")).intValue();
                if (pointsEarned > 0) {
                    registered.addLoyaltyPoints(pointsEarned);
                    customerRepository.save(registered);
                }
            }
        }
        return foodOrderRepository.save(order);
    }

    @Transactional
    public FoodOrder cancelOrder(Long orderId) {
        FoodOrder order = getOrderById(orderId);
        order.cancelOrder();
        return foodOrderRepository.save(order);
    }

    @Transactional
    public FoodOrder addOrderItem(Long orderId, Long foodItemId, int quantity) {
        FoodOrder order = getOrderById(orderId);
        if (order.isTerminal()) {
            throw new IllegalStateException("Cannot add items to an order in terminal state (" + order.getOrderStatus() + ")");
        }
        FoodItem foodItem = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new ResourceNotFoundException("FoodItem", "id", foodItemId));
        order.addOrderItem(foodItem, quantity);
        return foodOrderRepository.save(order);
    }

    @Transactional
    public FoodOrder updateOrderItemQuantity(Long orderId, Long orderItemId, int newQuantity) {
        FoodOrder order = getOrderById(orderId);
        if (order.isTerminal()) {
            throw new IllegalStateException("Cannot modify items on an order in terminal state (" + order.getOrderStatus() + ")");
        }
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        boolean updated = order.updateItemQuantity(orderItemId, newQuantity);
        if (!updated) {
            throw new ResourceNotFoundException("OrderItem", "id", orderItemId);
        }
        return foodOrderRepository.save(order);
    }

    @Transactional
    public FoodOrder removeOrderItem(Long orderId, Long orderItemId) {
        FoodOrder order = getOrderById(orderId);
        if (order.isTerminal()) {
            throw new IllegalStateException("Cannot modify items on an order in terminal state (" + order.getOrderStatus() + ")");
        }
        if (order.getOrderItems().size() <= 1) {
            throw new IllegalStateException("Cannot remove the last item from an order. Cancel the order instead.");
        }
        boolean removed = order.removeOrderItemById(orderItemId);
        if (!removed) {
            throw new ResourceNotFoundException("OrderItem", "id", orderItemId);
        }
        return foodOrderRepository.save(order);
    }

    @Transactional
    public FoodOrder updateOnlineDeliveryDetails(Long orderId, String address, String instructions) {
        FoodOrder order = getOrderById(orderId);
        if (!(order instanceof OnlineOrder onlineOrder)) {
            throw new IllegalArgumentException("Order " + orderId + " is not an online delivery order.");
        }
        if (order.isTerminal()) {
            throw new IllegalStateException("Cannot update delivery address on completed or cancelled order.");
        }
        onlineOrder.setDeliveryAddress(address);
        onlineOrder.setSpecialDeliveryInstructions(instructions);
        return foodOrderRepository.save(onlineOrder);
    }

    @Transactional
    public Payment attachCashPayment(Long orderId, BigDecimal amountTendered) {
        FoodOrder order = getOrderById(orderId);
        if (order.isTerminal()) {
            throw new IllegalStateException("Cannot attach payment to an order in terminal state (" + order.getOrderStatus() + ")");
        }
        CashPayment cash = new CashPayment(order, order.getTotalAmount(), amountTendered);
        cash.setPaymentStatus("COMPLETED");
        if ("PENDING".equals(order.getOrderStatus())) {
            order.setOrderStatus("PREPARING");
        }
        return paymentRepository.save(cash);
    }
}
