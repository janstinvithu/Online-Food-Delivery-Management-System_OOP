package com.fooddelivery.service;

import com.fooddelivery.config.SampleDataSeeder;
import com.fooddelivery.model.delivery.DeliveryZone;
import com.fooddelivery.model.order.FoodOrder;
import com.fooddelivery.model.order.OrderComplaint;
import com.fooddelivery.persistence.TestStore;
import com.fooddelivery.repository.impl.FileOperationsRepository;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationsCrudTest {

    @TempDir
    Path tempDir;

    private TestStore store;
    private OperationsService operations;

    @BeforeEach
    void setUp() {
        store = TestStore.open(tempDir);
        SampleDataSeeder.seedIfEmpty(store.foods, store.customers, store.couriers, store.orders, store.payments);
        operations = new OperationsService(
                new FileOperationsRepository(store.manager),
                store.orders,
                store.customers,
                store.deliveries,
                store.couriers,
                store.payments);
    }

    @Test
    void complaintReviewAndAddressCrud() {
        FoodOrder delivered = store.orders.findByOrderNumber("ORD-20261018-1003").orElseThrow();
        Long customerId = delivered.getCustomer().getId();

        OrderComplaint complaint = operations.createComplaint(delivered.getId(), customerId, "Food arrived cold");
        assertEquals(1, operations.getComplaints(delivered.getId()).size());
        OrderComplaint updated = operations.updateComplaint(complaint.getId(), customerId, "Food arrived cold and late", "RESOLVED");
        assertEquals("RESOLVED", updated.getStatus());
        assertThrows(IllegalStateException.class, () -> operations.deleteComplaint(complaint.getId(), customerId));
        operations.updateComplaint(complaint.getId(), customerId, "Food arrived cold and late", "OPEN");
        operations.deleteComplaint(complaint.getId(), customerId);
        assertTrue(operations.getComplaints(delivered.getId()).isEmpty());

        var review = operations.createReview(delivered.getId(), customerId, 4, "Tasty biryani");
        assertEquals("Tasty biryani", operations.getReview(delivered.getId()).orElseThrow().getComment());
        operations.updateReview(review.getId(), customerId, 5, "Excellent biryani");
        assertEquals(5, operations.getReview(delivered.getId()).orElseThrow().getRating());
        operations.deleteReview(review.getId(), customerId);
        assertTrue(operations.getReview(delivered.getId()).isEmpty());

        FoodOrder pending = store.orders.findByOrderNumber("ORD-20261018-1004").orElseThrow();
        assertThrows(IllegalStateException.class,
                () -> operations.createComplaint(pending.getId(), pending.getCustomer().getId(), "Too soon"));
        var pendingReview = operations.createReview(pending.getId(), pending.getCustomer().getId(), 5, "Looking forward to it");
        operations.replyToReview(pendingReview.getId(), "Thank you. We will prepare it with care.");
        assertEquals("Thank you. We will prepare it with care.", operations.getReview(pending.getId()).orElseThrow().getAdminReply());
        operations.updateReview(pendingReview.getId(), null, 4, "Updated by the restaurant");
        assertEquals("Thank you. We will prepare it with care.", operations.getReview(pending.getId()).orElseThrow().getAdminReply());
        operations.deleteReview(pendingReview.getId(), null);
        assertTrue(operations.getReview(pending.getId()).isEmpty());

        var address = operations.createAddress(customerId, "Home", "No. 12, Galle Road, Colombo 03");
        assertEquals(1, operations.getAddresses(customerId).size());
        operations.updateAddress(address.getId(), customerId, "Office", "No. 8, York Street, Colombo 01");
        assertEquals("Office", operations.getAddresses(customerId).get(0).getLabel());
        operations.deleteAddress(address.getId(), customerId);
        assertTrue(operations.getAddresses(customerId).isEmpty());
    }

    @Test
    void zoneShiftAndCheckpointCrud() {
        DeliveryZone zone = operations.createZone("Negombo", new BigDecimal("400.00"));
        assertEquals("Negombo", operations.getZone(zone.getId()).getAreaName());
        operations.updateZone(zone.getId(), "Negombo Beach", new BigDecimal("420.00"));
        assertEquals(new BigDecimal("420.00"), operations.getZone(zone.getId()).getFee());

        FoodOrder online = store.orders.findByOrderNumber("ORD-20261018-1001").orElseThrow();
        operations.attachZone(online.getId(), zone.getId());
        assertThrows(IllegalStateException.class, () -> operations.deleteZone(zone.getId()));

        DeliveryZone spare = operations.createZone("Kandy", new BigDecimal("500.00"));
        operations.deleteZone(spare.getId());
        assertTrue(operations.getZones().stream().noneMatch(item -> "Kandy".equals(item.getAreaName())));

        Long courierId = store.couriers.findAll().get(0).getId();
        var future = operations.createShift(courierId, LocalDate.now().plusDays(2), LocalTime.of(9, 0), LocalTime.of(13, 0));
        operations.updateShift(future.getId(), LocalDate.now().plusDays(3), LocalTime.of(10, 0), LocalTime.of(14, 0));
        assertEquals(LocalTime.of(10, 0), operations.getShifts(courierId).get(0).getStartTime());
        operations.deleteShift(future.getId());
        assertTrue(operations.getShifts(courierId).isEmpty());

        var started = operations.createShift(courierId, LocalDate.now().minusDays(1), LocalTime.of(9, 0), LocalTime.of(13, 0));
        assertThrows(IllegalStateException.class, () -> operations.deleteShift(started.getId()));

        Long deliveryId = store.deliveries.findAll().get(0).getId();
        var checkpoint = operations.createCheckpoint(deliveryId, "PICKED_UP", "Left the kitchen");
        operations.updateCheckpoint(checkpoint.getId(), "ON_THE_WAY", "On Galle Road", LocalDateTime.now().minusMinutes(5));
        assertEquals("ON_THE_WAY", operations.getCheckpoints(deliveryId).get(0).getStep());
        operations.deleteCheckpoint(checkpoint.getId());
        assertTrue(operations.getCheckpoints(deliveryId).isEmpty());
    }
}
