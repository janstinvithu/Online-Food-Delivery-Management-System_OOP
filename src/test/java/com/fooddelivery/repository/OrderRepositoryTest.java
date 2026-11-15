package com.fooddelivery.repository;

import com.fooddelivery.config.SampleDataSeeder;
import com.fooddelivery.model.order.FoodOrder;
import com.fooddelivery.model.order.OnlineOrder;
import com.fooddelivery.persistence.TestStore;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class OrderRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void testSaveAndFindOnlineOrder() {
        TestStore store = TestStore.open(tempDir);
        SampleDataSeeder.seedIfEmpty(store.foods, store.customers, store.couriers, store.orders, store.payments);

        Optional<FoodOrder> found = store.orders.findByOrderNumber("ORD-20261018-1001");
        assertTrue(found.isPresent());
        assertInstanceOf(OnlineOrder.class, found.get());
        OnlineOrder online = (OnlineOrder) found.get();
        assertEquals("No. 42, Ward Place, Colombo 07", online.getDeliveryAddress());
        assertEquals("Eleanor Fernando", online.getCustomer().getFullName());
        assertEquals(2, online.getOrderItems().size());
        assertNotNull(online.getDelivery());
        assertEquals("TRK-COL-2026-0001", online.getDelivery().getTrackingNumber());
        assertNotNull(online.getPayment());
        assertEquals("VISA", ((com.fooddelivery.model.delivery.CardPayment) online.getPayment()).getCardType());
    }
}
