package com.fooddelivery.model.order;

import com.fooddelivery.common.util.OrderIdGenerator;
import com.fooddelivery.model.customer.Customer;
import com.fooddelivery.model.customer.RegisteredCustomer;
import com.fooddelivery.model.delivery.Payment;
import com.fooddelivery.model.food.FoodItem;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "food_orders")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "order_type", discriminatorType = DiscriminatorType.STRING)
public abstract class FoodOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Order number is required")
    @Column(name = "order_number", nullable = false, unique = true, length = 36)
    private String orderNumber;

    @NotBlank(message = "Order status is required")
    @Column(name = "order_status", nullable = false, length = 30)
    private String orderStatus; // PENDING, PREPARING, READY, DELIVERED, CANCELLED

    @Column(name = "order_date_time", nullable = false)
    private LocalDateTime orderDateTime;

    @NotNull(message = "Subtotal amount is required")
    @DecimalMin(value = "0.00", message = "Subtotal cannot be negative")
    @Column(name = "subtotal_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotalAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.00", message = "Total amount cannot be negative")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @NotNull(message = "Customer reference is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Payment payment;

    public FoodOrder() {
        this.orderNumber = OrderIdGenerator.generateOrderNumber();
        this.orderStatus = "PENDING";
        this.orderDateTime = LocalDateTime.now();
        this.subtotalAmount = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
    }

    public FoodOrder(Customer customer) {
        this();
        setCustomer(customer);
    }

    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Order number cannot be blank");
        }
        this.orderNumber = orderNumber.trim();
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public OrderStatus getStatusEnum() {
        return OrderStatus.fromString(this.orderStatus);
    }

    public void setOrderStatus(String orderStatus) {
        if (orderStatus == null || orderStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Order status cannot be blank");
        }
        OrderStatus newStatus = OrderStatus.fromString(orderStatus);
        if (newStatus.name().equalsIgnoreCase(this.orderStatus)) {
            return;
        }
        if (isTerminal()) {
            throw new IllegalStateException("Order is in a terminal state (" + this.orderStatus + ") and cannot be modified.");
        }
        if (newStatus == OrderStatus.OUT_FOR_DELIVERY && isPickupOrder()) {
            throw new IllegalArgumentException("Pickup orders cannot be set to OUT_FOR_DELIVERY. Customers collect orders directly at the counter.");
        }
        this.orderStatus = newStatus.name();
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        if (orderStatus == null) {
            throw new IllegalArgumentException("Order status cannot be null");
        }
        setOrderStatus(orderStatus.name());
    }

    public boolean isTerminal() {
        return "DELIVERED".equalsIgnoreCase(this.orderStatus) || "CANCELLED".equalsIgnoreCase(this.orderStatus);
    }

    public boolean isCancellable() {
        return !isTerminal();
    }

    public boolean canTransitionTo(OrderStatus target) {
        OrderStatus current = getStatusEnum();
        return current != null && current.canTransitionTo(target);
    }

    public boolean canTransitionTo(String targetStatus) {
        try {
            return canTransitionTo(OrderStatus.fromString(targetStatus));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void cancelOrder() {
        if ("DELIVERED".equalsIgnoreCase(this.orderStatus)) {
            throw new IllegalStateException("Cannot cancel an order that has already been delivered.");
        }
        if ("CANCELLED".equalsIgnoreCase(this.orderStatus)) {
            throw new IllegalStateException("Order is already cancelled.");
        }
        this.orderStatus = OrderStatus.CANCELLED.name();
    }

    public int getTotalItemCount() {
        int count = 0;
        for (OrderItem item : orderItems) {
            count += item.getQuantity();
        }
        return count;
    }

    @Transient
    public boolean isOnlineOrder() {
        return "ONLINE".equalsIgnoreCase(getFulfillmentType()) || (this instanceof OnlineOrder);
    }

    @Transient
    public boolean isPickupOrder() {
        return (this instanceof PickupOrder);
    }

    public LocalDateTime getOrderDateTime() {
        return orderDateTime;
    }

    public void setOrderDateTime(LocalDateTime orderDateTime) {
        this.orderDateTime = orderDateTime;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public void setSubtotalAmount(BigDecimal subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total amount must be non-negative");
        }
        this.totalAmount = totalAmount;
    }

    public Customer getCustomer() {
        return customer;
    }

    public final void setCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        this.customer = customer;
    }

    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }

    public void addOrderItem(FoodItem foodItem, int quantity) {
        if (foodItem == null) {
            throw new IllegalArgumentException("Food item cannot be null");
        }
        if (!foodItem.isAvailable()) {
            throw new IllegalStateException("Food item is currently not available: " + foodItem.getName());
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive (> 0)");
        }

        // If item already exists in this order, merge quantities
        for (OrderItem existing : orderItems) {
            if (existing.getFoodItem() != null && foodItem.getId() != null
                    && foodItem.getId().equals(existing.getFoodItem().getId())) {
                existing.setQuantity(existing.getQuantity() + quantity);
                recalculateTotal();
                return;
            }
        }

        OrderItem item = new OrderItem(this, foodItem, quantity);
        this.orderItems.add(item);
        recalculateTotal();
    }

    public void removeOrderItem(OrderItem item) {
        if (item != null && this.orderItems.remove(item)) {
            item.setOrder(null);
            recalculateTotal();
        }
    }

    public boolean removeOrderItemById(Long orderItemId) {
        if (orderItemId == null) {
            return false;
        }
        for (int i = 0; i < orderItems.size(); i++) {
            OrderItem current = orderItems.get(i);
            if (orderItemId.equals(current.getId())) {
                current.setOrder(null);
                orderItems.remove(i);
                recalculateTotal();
                return true;
            }
        }
        return false;
    }

    public boolean updateItemQuantity(Long orderItemId, int newQuantity) {
        if (orderItemId == null || newQuantity <= 0) {
            throw new IllegalArgumentException("Invalid order item id or non-positive quantity");
        }
        for (OrderItem current : orderItems) {
            if (orderItemId.equals(current.getId())) {
                current.setQuantity(newQuantity);
                recalculateTotal();
                return true;
            }
        }
        return false;
    }

    public void recalculateTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            sum = sum.add(item.getSubtotal());
        }
        this.subtotalAmount = sum;

        if (customer instanceof RegisteredCustomer registered) {
            this.discountAmount = registered.calculateDiscount(this.subtotalAmount);
        } else {
            this.discountAmount = BigDecimal.ZERO;
        }

        this.totalAmount = this.subtotalAmount.subtract(this.discountAmount).max(BigDecimal.ZERO);
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    @Transient
    public abstract String getFulfillmentType();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodOrder foodOrder = (FoodOrder) o;
        return id != null && Objects.equals(id, foodOrder.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
