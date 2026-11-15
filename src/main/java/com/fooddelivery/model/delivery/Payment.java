package com.fooddelivery.model.delivery;

import com.fooddelivery.common.util.PersistentIds;
import com.fooddelivery.model.order.FoodOrder;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public abstract class Payment {

    private Long id;

    @NotNull(message = "Order reference is required")
    private FoodOrder order;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be strictly positive")
    private BigDecimal amount;

    @NotBlank(message = "Payment status is required")
    private String paymentStatus; // PENDING, AUTHORIZED, COMPLETED, FAILED, REFUNDED

    private LocalDateTime paymentTimestamp;

    @NotBlank(message = "Transaction reference is required")
    private String transactionReference;

    protected Payment() {
        this.paymentTimestamp = LocalDateTime.now();
        this.paymentStatus = "PENDING";
        this.transactionReference = "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    protected Payment(FoodOrder order, BigDecimal amount) {
        this();
        setOrder(order);
        setAmount(amount);
    }

    public Long getId() {
        return id;
    }

    public void assignPersistentId(Long persistentId) {
        this.id = PersistentIds.assign(this.id, persistentId);
    }

    public FoodOrder getOrder() {
        return order;
    }

    public final void setOrder(FoodOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        this.order = order;
        if (order.getPayment() != this) {
            order.setPayment(this);
        }
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public final void setAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be strictly positive (> 0.00)");
        }
        this.amount = amount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        if (paymentStatus == null || paymentStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment status cannot be blank");
        }
        this.paymentStatus = paymentStatus.trim().toUpperCase();
    }

    public LocalDateTime getPaymentTimestamp() {
        return paymentTimestamp;
    }

    public void setPaymentTimestamp(LocalDateTime paymentTimestamp) {
        this.paymentTimestamp = paymentTimestamp;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        if (transactionReference == null || transactionReference.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction reference cannot be blank");
        }
        this.transactionReference = transactionReference.trim();
    }

    public abstract String getPaymentDetails();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return id != null && Objects.equals(id, payment.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
