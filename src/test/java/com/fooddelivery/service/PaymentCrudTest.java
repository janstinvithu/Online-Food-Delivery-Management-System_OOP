package com.fooddelivery.service;

import com.fooddelivery.config.SampleDataSeeder;
import com.fooddelivery.model.customer.RegisteredCustomer;
import com.fooddelivery.model.delivery.CardPayment;
import com.fooddelivery.model.delivery.Payment;
import com.fooddelivery.model.order.FoodOrder;
import com.fooddelivery.persistence.TestStore;
import com.fooddelivery.repository.impl.FileOperationsRepository;
import java.math.BigDecimal;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentCrudTest {

    @TempDir
    Path tempDir;

    private TestStore store;
    private DeliveryService deliveryService;
    private OperationsService operations;
    private CustomerService customers;

    @BeforeEach
    void setUp() {
        store = TestStore.open(tempDir);
        SampleDataSeeder.seedIfEmpty(store.foods, store.customers, store.couriers, store.orders, store.payments);
        deliveryService = new DeliveryService(store.deliveries, store.couriers, store.payments, store.orders);
        operations = new OperationsService(
                new FileOperationsRepository(store.manager),
                store.orders, store.customers, store.deliveries, store.couriers, store.payments);
        customers = new CustomerService(store.customers);
    }

    @Test
    void paymentUpdateDeleteRefundAndSavedMethod() {
        FoodOrder paid = store.orders.findByOrderNumber("ORD-20261018-1001").orElseThrow();
        Payment updated = deliveryService.updatePayment(paid.getId(), "COMPLETED", null, "AUTH-UPDATED");
        assertEquals("COMPLETED", updated.getPaymentStatus());
        assertEquals("AUTH-UPDATED", ((CardPayment) updated).getAuthorizationCode());

        var refund = operations.createRefund(updated.getId(), new BigDecimal("100.00"), "Missing item");
        assertEquals("REQUESTED", operations.getRefunds(updated.getId()).get(0).getStatus());
        operations.updateRefund(refund.getId(), new BigDecimal("100.00"), "Missing item", "PAID");
        assertEquals("REFUNDED", deliveryService.getPaymentByOrderId(paid.getId()).getPaymentStatus());
        assertThrows(IllegalStateException.class, () -> operations.deleteRefund(refund.getId()));
        assertThrows(IllegalStateException.class, () -> deliveryService.deletePayment(paid.getId()));

        FoodOrder cashOrder = store.orders.findByOrderNumber("ORD-20261018-1002").orElseThrow();
        deliveryService.updatePayment(cashOrder.getId(), "FAILED", null, null);
        deliveryService.deletePayment(cashOrder.getId());
        assertTrue(store.payments.findByOrderId(cashOrder.getId()).isEmpty());

        RegisteredCustomer eleanor = (RegisteredCustomer) store.customers.findByEmailIgnoreCase("eleanor.fernando@vanguard.lk").orElseThrow();
        customers.savePaymentMethod(eleanor.getId(), "CASH");
        assertEquals("CASH", ((RegisteredCustomer) customers.getCustomerById(eleanor.getId())).getPreferredPaymentMethod());
        customers.clearPaymentMethod(eleanor.getId());
        assertTrue(((RegisteredCustomer) customers.getCustomerById(eleanor.getId())).getPreferredPaymentMethod() == null);
    }
}
