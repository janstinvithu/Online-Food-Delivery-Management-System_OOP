package com.fooddelivery.model.order;

import com.fooddelivery.common.util.OrderIdGenerator;
import com.fooddelivery.common.util.PersistentIds;
import com.fooddelivery.model.customer.Customer;
import com.fooddelivery.model.customer.RegisteredCustomer;
import com.fooddelivery.model.delivery.Payment;
import com.fooddelivery.model.food.FoodItem;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class FoodOrder {

    private Long id;

    @NotBlank(message = "Order number is required")
    private String orderNumber;

    @NotBlank(message = "Order status is required")
    private String orderStatus; // PENDING, PREPARING, READY, DELIVERED, CANCELLED

    private LocalDateTime orderDateTime;

    @NotNull(message = "Subtotal amount is required")
    @DecimalMin(value = "0.00", message = "Subtotal cannot be negative")
    private BigDecimal subtotalAmount = BigDecimal.ZERO;

    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.00", message = "Total amount cannot be negative")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @NotNull(message = "Customer reference is required")
    private Customer customer;

    private final List<OrderItem> orderItems = new ArrayList<>();

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

    public void assignPersistentId(Long persistentId) {
        this.id = PersistentIds.assign(this.id, persistentId);
    }

    /**
     * Reattaches a stored line without the availability check used for new orders.
     */
    public void restoreOrderItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Order item cannot be null");
        }
        if (!orderItems.contains(item)) {
            orderItems.add(item);
            item.setOrder(this);
        }
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

    public void setOrderStatus(String orderStatus) {
        if (orderStatus == null || orderStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Order status cannot be blank");
        }
        String upper = orderStatus.trim().toUpperCase();
        if ("CANCELLED".equals(this.orderStatus) || "DELIVERED".equals(this.orderStatus)) {
            throw new IllegalStateException("Order is in a terminal state: " + this.orderStatus);
        }
        this.orderStatus = upper;
    }

    public void cancelOrder() {
        if ("DELIVERED".equals(this.orderStatus)) {
            throw new IllegalStateException("Cannot cancel an order that has already been delivered.");
        }
        this.orderStatus = "CANCELLED";
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
