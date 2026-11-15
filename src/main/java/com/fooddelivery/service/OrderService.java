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
import com.fooddelivery.model.order.OrderItem;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private static final Map<String, List<String>> NEXT_STATUSES = new LinkedHashMap<>();

    static {
        NEXT_STATUSES.put("PENDING", List.of("CONFIRMED", "PREPARING"));
        NEXT_STATUSES.put("CONFIRMED", List.of("PREPARING"));
        NEXT_STATUSES.put("PREPARING", List.of("READY"));
        NEXT_STATUSES.put("READY", List.of("OUT_FOR_DELIVERY", "DELIVERED"));
        NEXT_STATUSES.put("OUT_FOR_DELIVERY", List.of("DELIVERED"));
        NEXT_STATUSES.put("DELIVERED", List.of());
        NEXT_STATUSES.put("CANCELLED", List.of());
    }

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

    public List<FoodOrder> searchOrders(String orderNumber, String status, String fulfillmentType) {
        String number = orderNumber == null ? "" : orderNumber.trim().toLowerCase();
        String statusFilter = status == null ? "" : status.trim().toUpperCase();
        String type = fulfillmentType == null ? "" : fulfillmentType.trim().toUpperCase();
        return getAllOrders().stream()
                .filter(order -> number.isEmpty() || (order.getOrderNumber() != null
                        && order.getOrderNumber().toLowerCase().contains(number)))
                .filter(order -> statusFilter.isEmpty() || statusFilter.equals(order.getOrderStatus()))
                .filter(order -> matchesFulfillment(order, type))
                .toList();
    }

    public List<String> statusChoices(FoodOrder order) {
        List<String> choices = new ArrayList<>();
        if (order.getOrderStatus() != null) {
            choices.add(order.getOrderStatus());
        }
        for (String next : NEXT_STATUSES.getOrDefault(order.getOrderStatus(), List.of())) {
            if (!choices.contains(next)) {
                choices.add(next);
            }
        }
        return choices;
    }

    public boolean canCustomerEdit(FoodOrder order) {
        return order != null && "PENDING".equals(order.getOrderStatus());
    }

    public boolean canCancel(FoodOrder order) {
        return order != null
                && !"DELIVERED".equals(order.getOrderStatus())
                && !"CANCELLED".equals(order.getOrderStatus());
    }

    public boolean canArchive(FoodOrder order) {
        return order != null
                && "PENDING".equals(order.getOrderStatus())
                && order.getPayment() == null;
    }

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

    public FoodOrder updateOrderStatus(Long orderId, String newStatus) {
        FoodOrder order = getOrderById(orderId);
        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException("Order status cannot be blank");
        }
        String next = newStatus.trim().toUpperCase();
        if (next.equals(order.getOrderStatus())) {
            return order;
        }
        List<String> allowed = NEXT_STATUSES.getOrDefault(order.getOrderStatus(), List.of());
        if (!allowed.contains(next)) {
            throw new IllegalArgumentException(
                    "Cannot change an order from " + order.getOrderStatus() + " to " + next + ".");
        }
        order.setOrderStatus(next);
        return foodOrderRepository.save(order);
    }

    public FoodOrder cancelOrder(Long orderId) {
        FoodOrder order = getOrderById(orderId);
        if ("CANCELLED".equals(order.getOrderStatus())) {
            throw new IllegalStateException("This order is already cancelled.");
        }
        order.cancelOrder();
        return foodOrderRepository.save(order);
    }

    public Payment attachCashPayment(Long orderId, BigDecimal amountTendered) {
        FoodOrder order = getOrderById(orderId);
        CashPayment cash = new CashPayment(order, order.getTotalAmount(), amountTendered);
        cash.setPaymentStatus("COMPLETED");
        if ("PENDING".equals(order.getOrderStatus())) {
            order.setOrderStatus("PREPARING");
        }
        return paymentRepository.save(cash);
    }

    public OnlineOrder getOnlineOrderById(Long id) {
        FoodOrder order = getOrderById(id);
        if (!(order instanceof OnlineOrder onlineOrder)) {
            throw new IllegalArgumentException("Order " + id + " is not an online order");
        }
        return onlineOrder;
    }

    public OnlineOrder updateOnlineOrder(Long id, String deliveryAddress, String instructions, BigDecimal deliveryFee) {
        OnlineOrder order = getOnlineOrderById(id);
        requireEditable(order);
        if (deliveryAddress != null) {
            order.setDeliveryAddress(deliveryAddress);
        }
        if (instructions != null) {
            order.setSpecialDeliveryInstructions(instructions);
        }
        if (deliveryFee != null) {
            order.setDeliveryFee(deliveryFee);
        }
        return foodOrderRepository.save(order);
    }

    public OnlineOrder cancelOnlineOrder(Long id) {
        getOnlineOrderById(id);
        return (OnlineOrder) cancelOrder(id);
    }

    public OnlineOrder createOrderNote(Long orderId, String note) {
        OnlineOrder order = getOnlineOrderById(orderId);
        requireEditable(order);
        if (order.getSpecialDeliveryInstructions() != null && !order.getSpecialDeliveryInstructions().isBlank()) {
            throw new IllegalStateException("This order already has a note. Update it instead.");
        }
        order.setSpecialDeliveryInstructions(cleanNote(note));
        return foodOrderRepository.save(order);
    }

    public String getOrderNote(Long orderId) {
        String note = getOnlineOrderById(orderId).getSpecialDeliveryInstructions();
        return note == null ? "" : note;
    }

    public OnlineOrder updateOrderNote(Long orderId, String note) {
        OnlineOrder order = getOnlineOrderById(orderId);
        requireEditable(order);
        order.setSpecialDeliveryInstructions(cleanNote(note));
        return foodOrderRepository.save(order);
    }

    public OnlineOrder deleteOrderNote(Long orderId) {
        OnlineOrder order = getOnlineOrderById(orderId);
        requireEditable(order);
        order.setSpecialDeliveryInstructions(null);
        return foodOrderRepository.save(order);
    }

    private String cleanNote(String note) {
        if (note == null || note.isBlank()) {
            throw new IllegalArgumentException("Order note cannot be blank.");
        }
        return note.trim();
    }

    public PickupOrder getPickupOrderById(Long id) {
        FoodOrder order = getOrderById(id);
        if (!(order instanceof PickupOrder pickupOrder)) {
            throw new IllegalArgumentException("Order " + id + " is not a pickup order");
        }
        return pickupOrder;
    }

    public PickupOrder updatePickupOrder(Long id, String pickupTimeSlot, int pickupWindowMinutes,
                                         int counterNumber, String contactPhone, boolean curbside) {
        PickupOrder order = getPickupOrderById(id);
        requireEditable(order);
        order.setPickupTimeSlot(pickupTimeSlot);
        order.setPickupWindowMinutes(pickupWindowMinutes);
        order.setCounterNumber(counterNumber);
        order.setPickupContactPhone(contactPhone);
        order.setCurbsidePickup(curbside);
        return foodOrderRepository.save(order);
    }

    public PickupOrder cancelPickupOrder(Long id) {
        getPickupOrderById(id);
        return (PickupOrder) cancelOrder(id);
    }

    public OrderItem addOrderItem(Long orderId, Long foodItemId, int quantity) {
        FoodOrder order = getOrderById(orderId);
        requireEditable(order);
        FoodItem foodItem = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new ResourceNotFoundException("FoodItem", "id", foodItemId));
        order.addOrderItem(foodItem, quantity);
        foodOrderRepository.save(order);
        List<OrderItem> items = order.getOrderItems();
        return items.get(items.size() - 1);
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        return getOrderById(orderId).getOrderItems();
    }

    public OrderItem updateOrderItemQuantity(Long orderId, Long orderItemId, int quantity) {
        FoodOrder order = getOrderById(orderId);
        requireEditable(order);
        OrderItem item = findItem(order, orderItemId);
        item.setQuantity(quantity);
        order.recalculateTotal();
        foodOrderRepository.save(order);
        return item;
    }

    public void removeOrderItem(Long orderId, Long orderItemId) {
        FoodOrder order = getOrderById(orderId);
        requireEditable(order);
        OrderItem item = findItem(order, orderItemId);
        if (order.getOrderItems().size() == 1) {
            throw new IllegalStateException(
                    "An order must keep at least one item. Cancel the order if you no longer want it.");
        }
        order.removeOrderItem(item);
        foodOrderRepository.save(order);
    }

    public FoodOrder createAdminOrder(String fulfillmentType, Long customerId, String deliveryAddress,
                                      String deliveryInstructions, BigDecimal deliveryFee, String pickupTimeSlot,
                                      int pickupWindowMinutes, int counterNumber, String contactPhone,
                                      boolean curbside, Map<Long, Integer> items) {
        if ("PICKUP".equalsIgnoreCase(fulfillmentType)) {
            return createPickupOrder(customerId, pickupTimeSlot, pickupWindowMinutes, counterNumber, contactPhone, curbside, items);
        }
        return createOnlineOrder(customerId, deliveryAddress, deliveryInstructions, deliveryFee, items);
    }

    public void deleteInvalidOrder(Long orderId) {
        FoodOrder order = getOrderById(orderId);
        if (!"PENDING".equals(order.getOrderStatus())) {
            throw new IllegalStateException("Only an unsubmitted pending order can be archived.");
        }
        if (order.getPayment() != null) {
            throw new IllegalStateException("Orders with payment records stay in history and cannot be archived.");
        }
        foodOrderRepository.archiveById(orderId);
    }

    private void requireEditable(FoodOrder order) {
        if (!canCustomerEdit(order)) {
            throw new IllegalStateException(
                    "This order can only be changed before preparation begins. Current status: " + order.getOrderStatus());
        }
    }

    private boolean matchesFulfillment(FoodOrder order, String type) {
        if (type == null || type.isEmpty()) {
            return true;
        }
        if ("ONLINE".equals(type)) {
            return order instanceof OnlineOrder;
        }
        if ("PICKUP".equals(type)) {
            return order instanceof PickupOrder;
        }
        return false;
    }

    private OrderItem findItem(FoodOrder order, Long orderItemId) {
        for (OrderItem item : order.getOrderItems()) {
            if (orderItemId.equals(item.getId())) {
                return item;
            }
        }
        throw new ResourceNotFoundException("OrderItem", "id", orderItemId);
    }
}
