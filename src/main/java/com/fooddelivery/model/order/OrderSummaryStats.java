package com.fooddelivery.model.order;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value object summarizing operational order metrics and revenue in Sri Lankan Rupees (LKR).
 */
public class OrderSummaryStats {

    private final long totalOrders;
    private final long activeOrders;
    private final long deliveredOrders;
    private final long cancelledOrders;
    private final BigDecimal totalRevenue;
    private final BigDecimal totalDiscounts;

    public OrderSummaryStats(long totalOrders, long activeOrders, long deliveredOrders,
                             long cancelledOrders, BigDecimal totalRevenue, BigDecimal totalDiscounts) {
        this.totalOrders = totalOrders;
        this.activeOrders = activeOrders;
        this.deliveredOrders = deliveredOrders;
        this.cancelledOrders = cancelledOrders;
        this.totalRevenue = (totalRevenue != null) ? totalRevenue : BigDecimal.ZERO;
        this.totalDiscounts = (totalDiscounts != null) ? totalDiscounts : BigDecimal.ZERO;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public long getActiveOrders() {
        return activeOrders;
    }

    public long getDeliveredOrders() {
        return deliveredOrders;
    }

    public long getCancelledOrders() {
        return cancelledOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public BigDecimal getTotalDiscounts() {
        return totalDiscounts;
    }

    public BigDecimal getAverageOrderValue() {
        if (deliveredOrders == 0) {
            return BigDecimal.ZERO;
        }
        return totalRevenue.divide(BigDecimal.valueOf(deliveredOrders), 2, RoundingMode.HALF_UP);
    }

    public double getCancellationRatePercentage() {
        if (totalOrders == 0) {
            return 0.0;
        }
        return ((double) cancelledOrders / (double) totalOrders) * 100.0;
    }
}

