package com.fooddelivery.service;

import com.fooddelivery.model.customer.RegisteredCustomer;
import com.fooddelivery.model.food.MainCourse;
import com.fooddelivery.model.order.FoodOrder;
import com.fooddelivery.model.order.OnlineOrder;
import com.fooddelivery.model.order.OrderSummaryStats;
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

    @Test
    void testAddOrderItemRecalculatesOrderTotals() {
        OnlineOrder order = new OnlineOrder(platinumCustomer, "Colombo", null, new BigDecimal("350.00"));
        order.addOrderItem(kottu, 1);
        when(foodOrderRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(order));
        when(foodOrderRepository.save(any(FoodOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MainCourse faluda = new MainCourse(
                "Faluda",
                "Cold sweet beverage with rose syrup",
                new BigDecimal("500.00"),
                5,
                "Beverage",
                true,
                "MILD",
                "Regular"
        );
        when(foodItemRepository.findById(20L)).thenReturn(Optional.of(faluda));

        FoodOrder updated = orderService.addOrderItem(5L, 20L, 2);

        // Subtotal: 2200 + (500 * 2) = 3200.00
        assertEquals(new BigDecimal("3200.00"), updated.getSubtotalAmount());
        // Platinum discount: 15% of 3200.00 = 480.00
        assertEquals(new BigDecimal("480.00"), updated.getDiscountAmount());
        // Net: 3200 - 480 + 350 = 3070.00
        assertEquals(new BigDecimal("3070.00"), updated.getTotalAmount());
    }

    @Test
    void testCannotAddItemsToDeliveredOrder() {
        OnlineOrder order = new OnlineOrder(platinumCustomer, "Colombo", null, new BigDecimal("350.00"));
        order.addOrderItem(kottu, 1);
        order.setOrderStatus("DELIVERED");
        when(foodOrderRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.addOrderItem(5L, 10L, 1));
    }

    @Test
    void testCannotRemoveLastRemainingItem() {
        OnlineOrder order = new OnlineOrder(platinumCustomer, "Colombo", null, new BigDecimal("350.00"));
        order.addOrderItem(kottu, 1);
        when(foodOrderRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.removeOrderItem(5L, 1L));
    }

    @Test
    void testUpdateOnlineDeliveryDetails() {
        OnlineOrder order = new OnlineOrder(platinumCustomer, "Original Address", "No notes", new BigDecimal("350.00"));
        when(foodOrderRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(order));
        when(foodOrderRepository.save(any(FoodOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FoodOrder result = orderService.updateOnlineDeliveryDetails(5L, "New Colombo Address", "Call before arrival");
        assertTrue(result instanceof OnlineOrder);
        assertEquals("New Colombo Address", ((OnlineOrder) result).getDeliveryAddress());
        assertEquals("Call before arrival", ((OnlineOrder) result).getSpecialDeliveryInstructions());
    }

    @Test
    void testGetOrderSummaryStats() {
        when(foodOrderRepository.count()).thenReturn(10L);
        when(foodOrderRepository.findActiveOrdersWithDetails()).thenReturn(java.util.List.of());
        when(foodOrderRepository.countByOrderStatus("DELIVERED")).thenReturn(8L);
        when(foodOrderRepository.countByOrderStatus("CANCELLED")).thenReturn(2L);
        when(foodOrderRepository.calculateDeliveredRevenue()).thenReturn(new BigDecimal("25000.00"));
        when(foodOrderRepository.calculateTotalDiscountsGiven()).thenReturn(new BigDecimal("1500.00"));

        OrderSummaryStats stats = orderService.getOrderSummaryStats();

        assertEquals(10L, stats.getTotalOrders());
        assertEquals(0L, stats.getActiveOrders());
        assertEquals(8L, stats.getDeliveredOrders());
        assertEquals(2L, stats.getCancelledOrders());
        assertEquals(new BigDecimal("25000.00"), stats.getTotalRevenue());
        assertEquals(new BigDecimal("1500.00"), stats.getTotalDiscounts());
        assertEquals(new BigDecimal("3125.00"), stats.getAverageOrderValue());
        assertEquals(20.0, stats.getCancellationRatePercentage());
    }
}
