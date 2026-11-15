package com.fooddelivery.repository;

import com.fooddelivery.model.customer.Customer;
import com.fooddelivery.model.order.FoodOrder;
import com.fooddelivery.model.order.OnlineOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FoodOrderRepository foodOrderRepository;

    @Test
    void testSaveAndFindOnlineOrder() {
        Customer customer = new Customer("Kasun Jayawardena", "kasun.j@example.lk", "+94-77-9876543", "45 Galle Road, Bambalapitiya, Colombo 04");
        customer = entityManager.persistAndFlush(customer);

        OnlineOrder order = new OnlineOrder(customer, "45 Galle Road, Bambalapitiya, Colombo 04", "Call upon arrival", new BigDecimal("350.00"));
        order.setOrderNumber("ORD-TEST-0001");
        entityManager.persistAndFlush(order);

        Optional<FoodOrder> found = foodOrderRepository.findByOrderNumber("ORD-TEST-0001");
        assertTrue(found.isPresent());
        assertEquals("45 Galle Road, Bambalapitiya, Colombo 04", ((OnlineOrder) found.get()).getDeliveryAddress());
        assertEquals(new BigDecimal("350.00"), ((OnlineOrder) found.get()).getDeliveryFee());
    }

    @Test
    void testSearchAndFilterOrders() {
        Customer customer1 = entityManager.persistAndFlush(
                new Customer("Dilshan Madushanka", "dilshan@cricket.lk", "+94-77-5551234", "Colombo")
        );
        Customer customer2 = entityManager.persistAndFlush(
                new Customer("Chaminda Vaas", "vaas@cricket.lk", "+94-71-9998877", "Wattala")
        );

        OnlineOrder order1 = new OnlineOrder(customer1, "Colombo", "Gate 1", new BigDecimal("350.00"));
        order1.setOrderNumber("ORD-SL-1001");
        order1.setTotalAmount(new BigDecimal("2500.00"));
        order1.setOrderStatus("PENDING");
        entityManager.persistAndFlush(order1);

        long initialDeliveredCount = foodOrderRepository.countByOrderStatus("DELIVERED");

        OnlineOrder order2 = new OnlineOrder(customer2, "Wattala", "Gate 2", new BigDecimal("350.00"));
        order2.setOrderNumber("ORD-SL-1002");
        order2.setTotalAmount(new BigDecimal("4000.00"));
        order2.setOrderStatus("DELIVERED");
        entityManager.persistAndFlush(order2);

        // Filter by status
        var pendingOrders = foodOrderRepository.findByOrderStatusWithDetails("PENDING");
        assertTrue(pendingOrders.stream().anyMatch(o -> "ORD-SL-1001".equals(o.getOrderNumber())));

        // Search by customer name
        var searchResults = foodOrderRepository.searchOrdersWithDetails("Dilshan");
        assertEquals(1, searchResults.size());
        assertEquals("ORD-SL-1001", searchResults.get(0).getOrderNumber());

        // Search by order number keyword
        var numResults = foodOrderRepository.searchOrdersWithDetails("SL-1002");
        assertEquals(1, numResults.size());
        assertEquals("Chaminda Vaas", numResults.get(0).getCustomer().getFullName());

        // Count by status
        assertEquals(initialDeliveredCount + 1, foodOrderRepository.countByOrderStatus("DELIVERED"));
    }
}
