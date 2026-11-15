package com.fooddelivery.model.order;

import com.fooddelivery.model.customer.Customer;
import com.fooddelivery.model.customer.RegisteredCustomer;
import com.fooddelivery.model.food.MainCourse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class FoodOrderDomainTest {

    private Customer regularCustomer;
    private RegisteredCustomer goldCustomer;
    private MainCourse kottu;
    private MainCourse biryani;

    @BeforeEach
    void setUp() {
        regularCustomer = new Customer("Kasun Silva", "kasun@example.lk", "+94-77-1112233", "Colombo 03");
        goldCustomer = new RegisteredCustomer(
                "Nimal Perera",
                "nimal@example.lk",
                "+94-71-4445566",
                "Kandy Road, Kelaniya",
                500,
                "GOLD",
                "hash123",
                "CASH"
        );

        kottu = new MainCourse(
                "Chicken Kottu",
                "Sri Lankan roti chopped with spicy chicken",
                new BigDecimal("1500.00"),
                15,
                "Sri Lankan",
                false,
                "HOT",
                "Regular"
        );

        biryani = new MainCourse(
                "Mutton Dum Biryani",
                "Fragrant basmati rice cooked with spiced mutton",
                new BigDecimal("2000.00"),
                25,
                "Mughlai / Sri Lankan",
                false,
                "MEDIUM",
                "Full Portion"
        );
    }

    @Test
    @DisplayName("Should initialize order with PENDING status and default values")
    void testInitialOrderState() {
        OnlineOrder order = new OnlineOrder(regularCustomer, "No 10, Marine Drive, Colombo", "Call on gate", new BigDecimal("350.00"));

        assertNotNull(order.getOrderNumber());
        assertEquals("PENDING", order.getOrderStatus());
        assertEquals(OrderStatus.PENDING, order.getStatusEnum());
        assertTrue(order.isCancellable());
        assertFalse(order.isTerminal());
        assertTrue(order.isOnlineOrder());
        assertFalse(order.isPickupOrder());
        assertEquals(0, order.getTotalItemCount());
    }

    @Test
    @DisplayName("Should calculate totals correctly and apply Gold member discount")
    void testOrderCalculationsWithGoldDiscount() {
        OnlineOrder order = new OnlineOrder(goldCustomer, "Kelaniya", "Leave at porch", new BigDecimal("350.00"));

        order.addOrderItem(kottu, 2); // 1500 * 2 = 3000
        order.addOrderItem(biryani, 1); // 2000 * 1 = 2000
        // Subtotal = 5000.00
        assertEquals(new BigDecimal("5000.00"), order.getSubtotalAmount());
        assertEquals(3, order.getTotalItemCount());

        // Gold tier gives 10% discount: 500.00
        assertEquals(new BigDecimal("500.00"), order.getDiscountAmount());

        // Net total = (5000 - 500) + 350 (delivery fee) = 4850.00
        assertEquals(new BigDecimal("4850.00"), order.getTotalAmount());
    }

    @Test
    @DisplayName("Should validate order status transitions and enforce terminal state")
    void testOrderStatusTransitions() {
        OnlineOrder order = new OnlineOrder(regularCustomer, "Colombo", null, new BigDecimal("300.00"));

        // Valid forward progression
        assertTrue(order.canTransitionTo(OrderStatus.CONFIRMED));
        assertTrue(order.canTransitionTo(OrderStatus.PREPARING));
        order.setOrderStatus(OrderStatus.CONFIRMED);
        assertEquals(OrderStatus.CONFIRMED, order.getStatusEnum());

        order.setOrderStatus(OrderStatus.PREPARING);
        assertEquals(OrderStatus.PREPARING, order.getStatusEnum());

        order.setOrderStatus(OrderStatus.READY);
        assertEquals(OrderStatus.READY, order.getStatusEnum());

        order.setOrderStatus(OrderStatus.OUT_FOR_DELIVERY);
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, order.getStatusEnum());

        order.setOrderStatus(OrderStatus.DELIVERED);
        assertEquals(OrderStatus.DELIVERED, order.getStatusEnum());
        assertTrue(order.isTerminal());
        assertFalse(order.isCancellable());

        // Cannot modify terminal state
        assertThrows(IllegalStateException.class, () -> order.setOrderStatus("PENDING"));
        assertThrows(IllegalStateException.class, order::cancelOrder);
    }

    @Test
    @DisplayName("Should allow cancelling active order and prevent double cancel")
    void testOrderCancellation() {
        PickupOrder pickupOrder = new PickupOrder(regularCustomer, "12:30 PM", 15, 2, "+94-77-1112233", false);

        assertTrue(pickupOrder.isCancellable());
        pickupOrder.cancelOrder();

        assertEquals(OrderStatus.CANCELLED, pickupOrder.getStatusEnum());
        assertTrue(pickupOrder.isTerminal());
        assertFalse(pickupOrder.isCancellable());

        // Second cancellation attempt throws
        assertThrows(IllegalStateException.class, pickupOrder::cancelOrder);
    }

    @Test
    @DisplayName("OrderStatus enum should identify valid transitions and terminal flags")
    void testOrderStatusEnumRules() {
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.CONFIRMED));
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.PENDING.canTransitionTo(OrderStatus.DELIVERED));

        assertTrue(OrderStatus.DELIVERED.isTerminal());
        assertTrue(OrderStatus.CANCELLED.isTerminal());
        assertFalse(OrderStatus.PREPARING.isTerminal());

        assertEquals(OrderStatus.READY, OrderStatus.fromString("ready"));
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.fromString("OUT FOR DELIVERY"));
        assertThrows(IllegalArgumentException.class, () -> OrderStatus.fromString("INVALID_STATUS"));
    }
}

