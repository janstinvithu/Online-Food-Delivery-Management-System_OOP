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
}
