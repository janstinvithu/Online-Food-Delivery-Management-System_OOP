package com.fooddelivery.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession createAdminSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userRole", "ADMIN");
        session.setAttribute("userName", "System Administrator");
        session.setAttribute("userEmail", "admin@foodflow.lk");
        return session;
    }

    private MockHttpSession createCustomerSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userRole", "CUSTOMER");
        session.setAttribute("userId", 1L);
        session.setAttribute("userName", "Eleanor Fernando");
        session.setAttribute("userEmail", "eleanor.fernando@vanguard.lk");
        return session;
    }

    @Test
    @DisplayName("Admin can view order logs with status filter and summary statistics")
    void testAdminListOrdersWithStatusFilter() throws Exception {
        mockMvc.perform(get("/orders")
                        .param("status", "PENDING")
                        .session(createAdminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("order/order-history"))
                .andExpect(model().attributeExists("orders", "stats", "currentStatus"))
                .andExpect(model().attribute("currentStatus", "PENDING"));
    }

    @Test
    @DisplayName("Admin can search order logs by keyword")
    void testAdminSearchOrders() throws Exception {
        mockMvc.perform(get("/orders")
                        .param("search", "Eleanor")
                        .session(createAdminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("order/order-history"))
                .andExpect(model().attributeExists("orders", "searchQuery"))
                .andExpect(model().attribute("searchQuery", "Eleanor"));
    }

    @Test
    @DisplayName("Customer accessing /orders is redirected to /orders/my-orders")
    void testCustomerRedirectToMyOrders() throws Exception {
        mockMvc.perform(get("/orders")
                        .session(createCustomerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/my-orders"));
    }

    @Test
    @DisplayName("Authenticated customer can view their personal orders")
    void testCustomerMyOrdersView() throws Exception {
        mockMvc.perform(get("/orders/my-orders")
                        .session(createCustomerSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("order/my-orders"))
                .andExpect(model().attributeExists("customer", "orders"));
    }

    @Test
    @DisplayName("Online order form validates missing customer")
    void testOnlineOrderMissingCustomerValidation() throws Exception {
        mockMvc.perform(post("/orders/new/online")
                        .session(createAdminSession())
                        .param("deliveryAddress", "123 Main St, Colombo")
                        .param("foodItemId", "1")
                        .param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/order-create"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("activeTab", "online"));
    }

    @Test
    @DisplayName("Online order form validates missing food item")
    void testOnlineOrderMissingFoodItemValidation() throws Exception {
        mockMvc.perform(post("/orders/new/online")
                        .session(createAdminSession())
                        .param("customerId", "1")
                        .param("deliveryAddress", "123 Main St, Colombo")
                        .param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/order-create"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("Pickup order form validates empty pickup time slot")
    void testPickupOrderMissingTimeSlotValidation() throws Exception {
        mockMvc.perform(post("/orders/new/pickup")
                        .session(createAdminSession())
                        .param("customerId", "1")
                        .param("foodItemId", "1")
                        .param("quantity", "1")
                        .param("pickupTimeSlot", "")
                        .param("pickupContactPhone", "+94-77-1234567"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/order-create"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("activeTab", "pickup"));
    }

    @Test
    @DisplayName("Successfully create online order and redirect")
    void testCreateOnlineOrderSuccess() throws Exception {
        mockMvc.perform(post("/orders/new/online")
                        .session(createAdminSession())
                        .param("customerId", "1")
                        .param("foodItemId", "1")
                        .param("quantity", "2")
                        .param("deliveryAddress", "77 Galle Face Terrace, Colombo 03")
                        .param("specialInstructions", "Ring the front bell")
                        .param("deliveryFee", "350.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders"));
    }

    @Test
    @DisplayName("Admin can view status transition page for an order")
    void testShowOrderStatusPage() throws Exception {
        mockMvc.perform(get("/orders/edit/1")
                        .session(createAdminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("order/order-status"))
                .andExpect(model().attributeExists("order"));
    }

    @Test
    @DisplayName("Admin can update order status")
    void testUpdateOrderStatus() throws Exception {
        mockMvc.perform(post("/orders/edit/1")
                        .session(createAdminSession())
                        .param("status", "CONFIRMED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/edit/1"));
    }
}
