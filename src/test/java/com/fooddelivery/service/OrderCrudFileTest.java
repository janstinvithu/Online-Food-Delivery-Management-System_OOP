package com.fooddelivery.service;

import com.fooddelivery.config.SampleDataSeeder;
import com.fooddelivery.model.customer.Customer;
import com.fooddelivery.model.food.FoodItem;
import com.fooddelivery.model.order.FoodOrder;
import com.fooddelivery.model.order.OnlineOrder;
import com.fooddelivery.model.order.OrderItem;
import com.fooddelivery.model.order.PickupOrder;
import com.fooddelivery.persistence.TestStore;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class OrderCrudFileTest {

    @TempDir
    Path tempDir;

    private TestStore store;
    private Long customerId;
    private Long foodId;

    @BeforeEach
    void setUp() {
        store = TestStore.open(tempDir);
        SampleDataSeeder.seedIfEmpty(store.foods, store.customers, store.couriers, store.orders, store.payments);
        customerId = store.customers.findByEmailIgnoreCase("sophia.desilva@gmail.com").orElseThrow().getId();
        foodId = store.foods.findAll().stream()
                .filter(item -> "Chicken Dum Biryani Special".equals(item.getName()))
                .map(FoodItem::getId)
                .findFirst()
                .orElseThrow();
    }

    @Test
    void onlineOrderCrudSet() {
        OnlineOrder created = store.orderService.createOnlineOrder(
                customerId, "No. 10, Church Street, Galle Fort", "Call on arrival",
                new BigDecimal("350.00"), Map.of(foodId, 1));
        assertNotNull(created.getId());
        assertNotNull(created.getDelivery());

        OnlineOrder read = store.orderService.getOnlineOrderById(created.getId());
        assertEquals(created.getOrderNumber(), read.getOrderNumber());

        String orderNumber = created.getOrderNumber();
        OnlineOrder updated = store.orderService.updateOnlineOrder(
                created.getId(), "No. 12, Lighthouse Street, Galle", "Leave at the gate", new BigDecimal("400.00"));
        assertEquals("No. 12, Lighthouse Street, Galle", updated.getDeliveryAddress());
        assertEquals(new BigDecimal("400.00"), updated.getDeliveryFee());
        assertEquals(orderNumber, updated.getOrderNumber());
        assertEquals(created.getOrderDateTime(), updated.getOrderDateTime());

        OnlineOrder cancelled = store.orderService.cancelOnlineOrder(created.getId());
        assertEquals("CANCELLED", cancelled.getOrderStatus());
        assertTrue(store.orderService.getOnlineOrderById(created.getId()).getOrderNumber().startsWith("ORD-"));
        assertThrows(IllegalStateException.class, () -> store.orderService.cancelOnlineOrder(created.getId()));
        assertThrows(IllegalStateException.class, () -> store.orderService.updateOnlineOrder(
                created.getId(), "No. 1, Fort", "Closed", new BigDecimal("350.00")));
    }

    @Test
    void pickupOrderCrudSet() {
        PickupOrder created = store.orderService.createPickupOrder(
                customerId, "06:00 PM - 06:20 PM", 20, 3, "+94-76-5551234", true, Map.of(foodId, 2));
        PickupOrder read = store.orderService.getPickupOrderById(created.getId());
        assertEquals(2, read.getOrderItems().get(0).getQuantity());

        PickupOrder updated = store.orderService.updatePickupOrder(
                created.getId(), "06:30 PM - 06:45 PM", 15, 4, "+94-76-5559999", false);
        assertEquals(4, updated.getCounterNumber());
        assertFalse(updated.isCurbsidePickup());

        assertEquals("CANCELLED", store.orderService.cancelPickupOrder(created.getId()).getOrderStatus());
    }

    @Test
    void orderItemCrudSet() {
        OnlineOrder created = store.orderService.createOnlineOrder(
                customerId, "No. 10, Church Street, Galle Fort", "Gate",
                new BigDecimal("350.00"), Map.of(foodId, 1));
        Long secondFoodId = store.foods.findAll().stream()
                .filter(item -> item.getName().contains("Faluda"))
                .map(FoodItem::getId)
                .findFirst()
                .orElseThrow();

        OrderItem added = store.orderService.addOrderItem(created.getId(), secondFoodId, 2);
        assertEquals(2, store.orderService.getOrderItems(created.getId()).size());

        OrderItem changed = store.orderService.updateOrderItemQuantity(created.getId(), added.getId(), 3);
        assertEquals(3, changed.getQuantity());

        store.orderService.removeOrderItem(created.getId(), added.getId());
        assertEquals(1, store.orderService.getOrderItems(created.getId()).size());
        Long remainingId = store.orderService.getOrderItems(created.getId()).get(0).getId();
        assertThrows(IllegalStateException.class,
                () -> store.orderService.removeOrderItem(created.getId(), remainingId));
    }

    @Test
    void adminOrderCrudSetArchivesOnlyInvalidOrders() {
        Customer guest = store.customers.findByEmailIgnoreCase("sophia.desilva@gmail.com").orElseThrow();
        FoodOrder created = store.orderService.createAdminOrder(
                "ONLINE", guest.getId(), "No. 10, Church Street, Galle Fort", "Desk",
                new BigDecimal("350.00"), null, 0, 0, null, false, Map.of(foodId, 1));
        assertTrue(store.orderService.getAllOrders().stream().anyMatch(order -> order.getId().equals(created.getId())));
        assertEquals(1, store.orderService.searchOrders(created.getOrderNumber(), "PENDING", "ONLINE").size());

        store.orderService.deleteInvalidOrder(created.getId());
        assertTrue(store.orders.findById(created.getId()).isEmpty());
        assertTrue(store.orderService.searchOrders(created.getOrderNumber(), null, null).isEmpty());

        FoodOrder paidPending = store.orders.findByOrderNumber("ORD-20261018-1004").orElseThrow();
        assertThrows(IllegalStateException.class, () -> store.orderService.deleteInvalidOrder(paidPending.getId()));
        assertEquals("PREPARING", store.orderService.updateOrderStatus(paidPending.getId(), "PREPARING").getOrderStatus());
        assertThrows(IllegalStateException.class, () -> store.orderService.updatePickupOrder(
                paidPending.getId(), "02:00 PM - 02:15 PM", 15, 1, "+94-77-4448899", false));
        assertThrows(IllegalArgumentException.class,
                () -> store.orderService.updateOrderStatus(paidPending.getId(), "DELIVERED"));

        assertThrows(IllegalStateException.class, () -> store.orderService.deleteInvalidOrder(
                store.orders.findByOrderNumber("ORD-20261018-1001").orElseThrow().getId()));
    }

    @Test
    void orderNoteCrudSet() {
        OnlineOrder created = store.orderService.createOnlineOrder(
                customerId, "No. 10, Church Street, Galle Fort", null,
                new BigDecimal("350.00"), Map.of(foodId, 1));

        store.orderService.createOrderNote(created.getId(), "Ring the bell");
        assertEquals("Ring the bell", store.orderService.getOrderNote(created.getId()));
        assertThrows(IllegalStateException.class,
                () -> store.orderService.createOrderNote(created.getId(), "Second note"));

        OnlineOrder updated = store.orderService.updateOrderNote(created.getId(), "Leave with security");
        assertEquals("Leave with security", updated.getSpecialDeliveryInstructions());

        OnlineOrder cleared = store.orderService.deleteOrderNote(created.getId());
        assertTrue(cleared.getSpecialDeliveryInstructions() == null || cleared.getSpecialDeliveryInstructions().isBlank());
        assertEquals("", store.orderService.getOrderNote(created.getId()));
        assertThrows(IllegalArgumentException.class, () -> store.orderService.createOrderNote(created.getId(), "  "));
    }
}
