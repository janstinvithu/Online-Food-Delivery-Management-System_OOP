package com.fooddelivery.controller;

import com.fooddelivery.model.customer.Customer;
import com.fooddelivery.model.customer.RegisteredCustomer;
import com.fooddelivery.model.delivery.CardPayment;
import com.fooddelivery.model.delivery.CashPayment;
import com.fooddelivery.model.food.Beverage;
import com.fooddelivery.model.food.MainCourse;
import com.fooddelivery.model.order.OnlineOrder;
import com.fooddelivery.model.order.PickupOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthAndSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Public route /login is accessible unauthenticated")
    void testPublicLoginRouteAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @DisplayName("Public route /customers/new is accessible unauthenticated")
    void testPublicRegisterRouteAccessible() throws Exception {
        mockMvc.perform(get("/customers/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/customer-register"));
    }

    @Test
    @DisplayName("Unauthenticated request to protected route redirects to /login")
    void testUnauthenticatedAccessRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/foods"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/orders/my-orders"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("Admin authentication succeeds and sets session attributes")
    void testAdminLoginSuccess() throws Exception {
        mockMvc.perform(post("/login")
                        .param("role", "ADMIN")
                        .param("email", "admin@foodflow.lk")
                        .param("password", "admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("Customer authentication succeeds and redirects to /orders/my-orders")
    void testCustomerLoginSuccess() throws Exception {
        mockMvc.perform(post("/login")
                        .param("role", "CUSTOMER")
                        .param("email", "eleanor.fernando@vanguard.lk")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/my-orders"));
    }

    @Test
    @DisplayName("Customer attempting to access Admin Scope is intercepted and redirected to /orders/my-orders")
    void testCustomerBlockedFromAdminScope() throws Exception {
        MockHttpSession customerSession = new MockHttpSession();
        customerSession.setAttribute("userRole", "CUSTOMER");
        customerSession.setAttribute("userId", 1L);
        customerSession.setAttribute("userName", "Eleanor Fernando");

        // Customer blocked from /foods
        mockMvc.perform(get("/foods").session(customerSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/my-orders"));

        // Customer blocked from /deliveries
        mockMvc.perform(get("/deliveries").session(customerSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/my-orders"));

        // Customer blocked from /customers directory
        mockMvc.perform(get("/customers").session(customerSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/my-orders"));
    }

    @Test
    @DisplayName("Customer can access Customer Scope routes")
    void testCustomerCanAccessCustomerScope() throws Exception {
        MockHttpSession customerSession = new MockHttpSession();
        customerSession.setAttribute("userRole", "CUSTOMER");
        customerSession.setAttribute("userId", 1L);
        customerSession.setAttribute("userName", "Eleanor Fernando");

        mockMvc.perform(get("/orders/my-orders").session(customerSession))
                .andExpect(status().isOk())
                .andExpect(view().name("order/my-orders"));

        mockMvc.perform(get("/orders/new").session(customerSession))
                .andExpect(status().isOk())
                .andExpect(view().name("order/order-create"));

        mockMvc.perform(get("/customers/profile").session(customerSession))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/profile"));
    }

    @Test
    @DisplayName("Admin can access Admin Scope routes")
    void testAdminCanAccessAdminScope() throws Exception {
        MockHttpSession adminSession = new MockHttpSession();
        adminSession.setAttribute("userRole", "ADMIN");
        adminSession.setAttribute("userName", "System Administrator");

        mockMvc.perform(get("/").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        mockMvc.perform(get("/foods").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(view().name("food/food-list"));

        mockMvc.perform(get("/deliveries").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(view().name("delivery/delivery-tracking"));

        mockMvc.perform(get("/customers").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/customer-list"));
    }

    @Test
    @DisplayName("Component 02: RegisteredCustomer dynamic 10% discount method verification")
    void testRegisteredCustomerDiscountCalculations() {
        RegisteredCustomer goldMember = new RegisteredCustomer(
                "Marcus Perera", "marcus@kandytech.lk", "+94-71-9876543",
                "Peradeniya Road, Kandy", 650, "GOLD", "hash", "CASH"
        );

        // Gold tier has exactly 10% discount rate (0.10)
        assertEquals(0.10, goldMember.getDiscountRate(), 0.001);

        BigDecimal foodTotal = new BigDecimal("2500.00");
        BigDecimal discount = goldMember.calculateDiscount(foodTotal);
        // 10% of 2500.00 = 250.00
        assertEquals(new BigDecimal("250.00"), discount);

        BigDecimal discountedTotal = goldMember.calculateDiscountedTotal(foodTotal);
        // 2500.00 - 250.00 = 2250.00
        assertEquals(new BigDecimal("2250.00"), discountedTotal);
    }

    @Test
    @DisplayName("Component 01: FoodItem polymorphic hierarchy (MainCourse & Beverage)")
    void testFoodItemHierarchy() {
        MainCourse biryani = new MainCourse(
                "Chicken Biryani", "Dum rice with spice", new BigDecimal("1850.00"), 25,
                "Sri Lankan Dum", false, "MEDIUM", "Clay Pot"
        );
        assertEquals("MEDIUM", biryani.getSpiceLevel());
        assertEquals("Clay Pot", biryani.getServingPortion());

        Beverage ceylonTea = new Beverage(
                "Ceylon Milk Tea", "High grown pekoe", new BigDecimal("450.00"), 5,
                "Pot (350ml)", 350, false, 65, "LOW"
        );
        assertEquals(350, ceylonTea.getVolumeMl());
        assertEquals("Pot (350ml)", ceylonTea.getVolumeSize());
    }

    @Test
    @DisplayName("Component 03: FoodOrder polymorphic calculations (OnlineOrder deliveryFee vs PickupOrder)")
    void testOrderHierarchyCalculations() {
        Customer guest = new Customer("Guest Customer", "guest@test.lk", "+94-77-0000000", "Colombo 03");
        MainCourse food = new MainCourse("Rice", "Desc", new BigDecimal("1000.00"), 10, "Local", true, "MILD", "Regular");

        OnlineOrder onlineOrder = new OnlineOrder(guest, "Colombo 03", "Gate 1", new BigDecimal("350.00"));
        onlineOrder.addOrderItem(food, 2); // 2000.00 subtotal + 350.00 delivery fee = 2350.00
        assertEquals(new BigDecimal("2000.00"), onlineOrder.getSubtotalAmount());
        assertEquals(new BigDecimal("350.00"), onlineOrder.getDeliveryFee());
        assertEquals(new BigDecimal("2350.00"), onlineOrder.getTotalAmount());

        PickupOrder pickupOrder = new PickupOrder(guest, "12:30 PM - 01:00 PM", 15, 2, "+94-77-0000000", true);
        pickupOrder.addOrderItem(food, 1); // 1000.00 subtotal, no delivery fee
        assertEquals(new BigDecimal("1000.00"), pickupOrder.getSubtotalAmount());
        assertEquals(new BigDecimal("1000.00"), pickupOrder.getTotalAmount());
        assertEquals("12:30 PM - 01:00 PM", pickupOrder.getPickupTimeSlot());
    }

    @Test
    @DisplayName("Component 04: Payment polymorphic calculation & validation (Cash tendered vs Card 4 digits)")
    void testPaymentHierarchyAndValidation() {
        Customer guest = new Customer("Guest", "g@test.lk", "+94-77-1111111", "Colombo 05");
        PickupOrder order = new PickupOrder(guest, "02:00 PM", 10, 1, "+94-77-1111111", false);
        order.setTotalAmount(new BigDecimal("1500.00"));

        CashPayment cash = new CashPayment(order, order.getTotalAmount(), new BigDecimal("2000.00"));
        // Tendered 2000.00 for 1500.00 -> Change due is 500.00
        assertEquals(new BigDecimal("500.00"), cash.getChangeDue());

        CardPayment card = new CardPayment(order, order.getTotalAmount(), "4242", "VISA", "AUTH-9912", "gw_9912");
        assertEquals("4242", card.getCardLastFour());
        assertEquals("VISA", card.getCardType());

        // Validate 4 digits constraint
        assertThrows(IllegalArgumentException.class, () -> {
            card.setCardLastFour("123"); // Not 4 digits
        });
    }
}
