package com.fooddelivery.model.delivery;

import com.fooddelivery.model.order.FoodOrder;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "cash_payments")
@DiscriminatorValue("CASH")
@PrimaryKeyJoinColumn(name = "id")
public class CashPayment extends Payment {

    @NotNull(message = "Amount tendered is required")
    @DecimalMin(value = "0.00", message = "Amount tendered cannot be negative")
    @Column(name = "amount_tendered", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountTendered;

    @NotNull(message = "Change due is required")
    @DecimalMin(value = "0.00", message = "Change due cannot be negative")
    @Column(name = "change_due", nullable = false, precision = 10, scale = 2)
    private BigDecimal changeDue;

    public CashPayment() {
        super();
        this.amountTendered = BigDecimal.ZERO;
        this.changeDue = BigDecimal.ZERO;
    }

    public CashPayment(FoodOrder order, BigDecimal amount, BigDecimal amountTendered) {
        super(order, amount);
        setAmountTendered(amountTendered);
    }

    public BigDecimal getAmountTendered() {
        return amountTendered;
    }

    public final void setAmountTendered(BigDecimal amountTendered) {
        if (amountTendered == null || amountTendered.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount tendered must be non-negative");
        }
        if (getAmount() != null && amountTendered.compareTo(getAmount()) < 0) {
            throw new IllegalArgumentException("Amount tendered cannot be less than order total");
        }
        this.amountTendered = amountTendered;
        recalculateChange();
    }

    public BigDecimal getChangeDue() {
        return changeDue;
    }

    public final void setChangeDue(BigDecimal changeDue) {
        if (changeDue == null || changeDue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Change due must be non-negative");
        }
        this.changeDue = changeDue;
    }

    private void recalculateChange() {
        if (amountTendered != null && getAmount() != null) {
            this.changeDue = amountTendered.subtract(getAmount()).max(BigDecimal.ZERO);
        }
    }

    @Override
    @Transient
    public String getPaymentDetails() {
        return "Cash [Tendered: " + com.fooddelivery.common.util.CurrencyUtil.format(amountTendered) 
                + ", Change: " + com.fooddelivery.common.util.CurrencyUtil.format(changeDue) + "]";
    }
}
