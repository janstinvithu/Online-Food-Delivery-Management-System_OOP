package com.fooddelivery.service;

import com.fooddelivery.model.customer.RegisteredCustomer;
import com.fooddelivery.model.food.MainCourse;
import com.fooddelivery.model.order.OnlineOrder;
import com.fooddelivery.repository.CustomerRepository;
import com.fooddelivery.repository.FoodItemRepository;
import com.fooddelivery.repository.FoodOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class OrderServiceTest {

    @Mock
    private FoodOrderRepository foodOrderRepository;

    @Mock
    private FoodItemRepository foodItemRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private OrderService orderService;

    private RegisteredCustomer platinumCustomer;
    private MainCourse kottu;

    @BeforeEach
    public void setUp() {
        platinumCustomer = new RegisteredCustomer(
                "Eleanor Fernando",
                "eleanor@vanguard.lk",
                "+94-77-1234567",
                "No. 42, Ward Place, Colombo 07",
                1250,
                "PLATINUM",
                "hash_1234",
                "CARD"
        );

        kottu = new MainCourse(
                "Jaffna Spiced Mutton Kottu Roti",
                "Flaky godamba roti shredded with tender mutton and spicy Jaffna curry",
                new BigDecimal("2200.00"),
                20,
                "Northern Sri Lankan",
                false,
                "EXTRA_HOT",
                "Large Portion with Gravy"
        );
    }

    @Test
    void testDynamicDiscountForPlatinumMember() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(platinumCustomer));
        when(foodItemRepository.findById(10L)).thenReturn(Optional.of(kottu));
        when(foodOrderRepository.save(any(OnlineOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OnlineOrder order = orderService.createOnlineOrder(
                1L,
                "No. 42, Ward Place, Colombo 07",
                "Ring buzzer upon arrival",
                new BigDecimal("350.00"),
                Map.of(10L, 1)
        );

        assertNotNull(order);
        // Subtotal: 2200.00
        assertEquals(new BigDecimal("2200.00"), order.getSubtotalAmount());
        // Platinum discount: 15% of 2200.00 = 330.00
        assertEquals(new BigDecimal("330.00"), order.getDiscountAmount());
        // Net: 2200.00 - 330.00 + 350.00 (delivery fee) = 2220.00
        assertEquals(new BigDecimal("2220.00"), order.getTotalAmount());
    }
}
