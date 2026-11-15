package com.fooddelivery.service;

import com.fooddelivery.config.SampleDataSeeder;
import com.fooddelivery.model.delivery.DeliveryPerson;
import com.fooddelivery.persistence.TestStore;
import java.math.BigDecimal;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeliveryCourierCrudTest {

    @TempDir
    Path tempDir;

    private TestStore store;
    private DeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        store = TestStore.open(tempDir);
        SampleDataSeeder.seedIfEmpty(store.foods, store.customers, store.couriers, store.orders, store.payments);
        deliveryService = new DeliveryService(store.deliveries, store.couriers, store.payments, store.orders);
    }

    @Test
    void courierCrudSet() {
        DeliveryPerson created = deliveryService.createCourier(
                "Amal Silva", "+94-71-2223344", "Motorcycle", "WP CA-2201", new BigDecimal("4.80"));
        DeliveryPerson read = deliveryService.getCourierById(created.getId());
        assertEquals("Amal Silva", read.getName());

        DeliveryPerson updated = deliveryService.updateCourier(
                created.getId(), "Amal Silva", "+94-71-2223344", "Scooter", "WP CA-2201", new BigDecimal("4.90"));
        assertEquals("Scooter", updated.getVehicleType());

        deliveryService.deleteCourier(created.getId());
        assertTrue(store.couriers.findById(created.getId()).isEmpty());

        Long busyId = store.couriers.findAll().stream()
                .filter(courier -> !courier.isAvailable())
                .map(DeliveryPerson::getId)
                .findFirst()
                .orElseThrow();
        assertThrows(IllegalStateException.class, () -> deliveryService.deleteCourier(busyId));
    }
}
