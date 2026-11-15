package com.fooddelivery.controller;

import com.fooddelivery.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class OrderPagesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderService orderService;

    @Test
    @DisplayName("Customer sees and updates only their own pending order")
    void customerOwnsPendingOrderDetails() throws Exception {
        mockMvc.perform(get("/orders/my-orders/4").session(customer(4L, "David Jayawardena")))
                .andExpect(status().isOk())
                .andExpect(view().name("order/order-details"))
                .andExpect(content().string(containsString("ORD-20261018-1004")))
                .andExpect(content().string(containsString("Save pickup details")));

        mockMvc.perform(get("/orders/my-orders/4").session(customer(1L, "Eleanor Fernando")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/my-orders"));

        mockMvc.perform(get("/orders/my-orders/1").session(customer(1L, "Eleanor Fernando")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("before preparation")))
                .andExpect(content().string(not(containsString("Save delivery details"))));

        mockMvc.perform(post("/orders/my-orders/4/details")
                        .session(customer(4L, "David Jayawardena"))
                        .param("pickupTimeSlot", "03:00 PM - 03:15 PM")
                        .param("pickupWindowMinutes", "15")
                        .param("counterNumber", "2")
                        .param("pickupContactPhone", "+94-77-4448899"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/my-orders/4"))
                .andExpect(flash().attributeExists("successMessage"));

        assertEquals("ORD-20261018-1004", orderService.getPickupOrderById(4L).getOrderNumber());
        assertEquals("03:00 PM - 03:15 PM", orderService.getPickupOrderById(4L).getPickupTimeSlot());
    }

    @Test
    @DisplayName("A customer id in the form does not place the order for someone else")
    void customerIdFromBrowserIsIgnored() throws Exception {
        MvcResult result = mockMvc.perform(post("/orders/new/online")
                        .session(customer(1L, "Eleanor Fernando"))
                        .param("customerId", "2")
                        .param("foodItemId", "1")
                        .param("quantity", "1")
                        .param("deliveryAddress", "No. 42, Ward Place, Colombo 07")
                        .param("deliveryFee", "350.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/payments/checkout/*"))
                .andReturn();

        String redirected = result.getResponse().getRedirectedUrl();
        Long createdId = Long.valueOf(redirected.substring(redirected.lastIndexOf('/') + 1));
        assertEquals(1L, orderService.getOrderById(createdId).getCustomer().getId());
    }

    @Test
    @DisplayName("Admin search and illegal status change stay on the order pages")
    void adminSearchAndStatusValidation() throws Exception {
        mockMvc.perform(get("/orders")
                        .session(admin())
                        .param("orderNumber", "1001")
                        .param("status", "PREPARING")
                        .param("type", "ONLINE"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/order-history"))
                .andExpect(content().string(containsString("ORD-20261018-1001")))
                .andExpect(content().string(not(containsString("ORD-20261018-1003"))));

        mockMvc.perform(post("/orders/edit/4")
                        .session(admin())
                        .param("status", "DELIVERED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/edit/4"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    private MockHttpSession customer(Long id, String name) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userRole", "CUSTOMER");
        session.setAttribute("userId", id);
        session.setAttribute("userName", name);
        return session;
    }

    private MockHttpSession admin() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userRole", "ADMIN");
        session.setAttribute("userName", "System Administrator");
        return session;
    }
}
