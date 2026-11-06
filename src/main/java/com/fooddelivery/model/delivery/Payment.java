package com.fooddelivery.model.delivery;

import com.fooddelivery.model.order.FoodOrder;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "payment_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Order reference is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private FoodOrder order;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be strictly positive")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotBlank(message = "Payment status is required")
    @Column(name = "payment_status", nullable = false, length = 30)
    private String paymentStatus; // PENDING, AUTHORIZED, COMPLETED, FAILED, REFUNDED

    @Column(name = "payment_timestamp", nullable = false)
    private LocalDateTime paymentTimestamp;

    @NotBlank(message = "Transaction reference is required")
    @Column(name = "transaction_reference", nullable = false, unique = true, length = 64)
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

    @Transient
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
