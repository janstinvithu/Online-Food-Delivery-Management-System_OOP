package com.fooddelivery.model.order;

import java.util.Set;

/**
 * Lifecycle states of a food order in the delivery system.
 * Encapsulates valid transitions, display labels, and state properties.
 */
public enum OrderStatus {
    PENDING("Pending", "status-PENDING"),
    CONFIRMED("Confirmed", "status-CONFIRMED"),
    PREPARING("Preparing", "status-PREPARING"),
    READY("Ready", "status-READY"),
    OUT_FOR_DELIVERY("Out for Delivery", "status-OUT_FOR_DELIVERY"),
    DELIVERED("Delivered", "status-DELIVERED"),
    CANCELLED("Cancelled", "status-CANCELLED");

    private final String displayName;
    private final String badgeClass;

    OrderStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED;
    }

    public boolean isCancellable() {
        return !isTerminal();
    }

    /**
     * Determines whether transitioning from this status to target is legally allowed.
     */
    public boolean canTransitionTo(OrderStatus target) {
        if (target == null || this == target) {
            return false;
        }
        if (this.isTerminal()) {
            return false;
        }
        if (target == CANCELLED) {
            return isCancellable();
        }
        return switch (this) {
            case PENDING -> target == CONFIRMED || target == PREPARING;
            case CONFIRMED -> target == PREPARING || target == READY;
            case PREPARING -> target == READY;
            case READY -> target == OUT_FOR_DELIVERY || target == DELIVERED;
            case OUT_FOR_DELIVERY -> target == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    public static OrderStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String clean = value.trim().toUpperCase().replace(" ", "_");
        for (OrderStatus status : values()) {
            if (status.name().equals(clean)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status: " + value);
    }
}
