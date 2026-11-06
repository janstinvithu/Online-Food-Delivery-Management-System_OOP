package com.fooddelivery.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FormValidationIntegrationTest {

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
    @DisplayName("Main Course form rejects blank dish name and zero price with validation errors")
    void testMainCourseValidationFailure() throws Exception {
        mockMvc.perform(post("/foods/new/main-course")
                        .session(createAdminSession())
                        .param("name", "")
                        .param("price", "0.00")
                        .param("preparationTimeMinutes", "0")
                        .param("cuisineType", "")
                        .param("spiceLevel", "MILD")
                        .param("servingPortion", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("food/food-add"))
                .andExpect(model().attributeHasFieldErrors("mainCourse", "name", "price", "preparationTimeMinutes", "cuisineType"));
    }

    @Test
    @DisplayName("Beverage form rejects invalid volume and negative price with validation errors")
    void testBeverageValidationFailure() throws Exception {
        mockMvc.perform(post("/foods/new/beverage")
                        .session(createAdminSession())
                        .param("name", "")
                        .param("price", "-100.00")
                        .param("preparationTimeMinutes", "0")
                        .param("volumeSize", "")
                        .param("volumeMl", "0")
                        .param("sugarLevel", "ZERO"))
                .andExpect(status().isOk())
                .andExpect(view().name("food/food-add"))
                .andExpect(model().attribute("activeTab", "beverage"))
                .andExpect(model().attributeHasFieldErrors("beverage", "name", "price", "preparationTimeMinutes", "volumeMl"));
    }

    @Test
    @DisplayName("Food update form rejects blank name and non-positive price without 500 error")
    void testFoodUpdateValidationFailure() throws Exception {
        mockMvc.perform(post("/foods/edit/1")
                        .session(createAdminSession())
                        .param("name", "")
                        .param("price", "-50.00")
                        .param("preparationTimeMinutes", "10")
                        .param("available", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("food/food-edit"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("Customer registration rejects existing duplicate email gracefully")
    void testCustomerRegistrationDuplicateEmail() throws Exception {
        mockMvc.perform(post("/customers/new/account")
                        .param("fullName", "Eleanor Duplicate")
                        .param("email", "eleanor.fernando@vanguard.lk")
                        .param("phone", "+94-77-9999999")
                        .param("address", "No. 1, Main Street, Colombo"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/customer-register"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("Customer registration rejects invalid phone number format")
    void testCustomerRegistrationInvalidPhone() throws Exception {
        mockMvc.perform(post("/customers/new/guest")
                        .param("fullName", "John Doe")
                        .param("email", "john.doe.unique@test.lk")
                        .param("phone", "abc")
                        .param("address", "Galle"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/customer-register"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("Online order form rejects missing customer or empty delivery address")
    void testOnlineOrderValidationFailure() throws Exception {
        mockMvc.perform(post("/orders/new/online")
                        .session(createAdminSession())
                        .param("customerId", "1")
                        .param("foodItemId", "1")
                        .param("quantity", "0")
                        .param("deliveryAddress", "")
                        .param("deliveryFee", "350.00"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/order-create"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("Pickup order form rejects invalid window or empty contact phone")
    void testPickupOrderValidationFailure() throws Exception {
        mockMvc.perform(post("/orders/new/pickup")
                        .session(createAdminSession())
                        .param("customerId", "1")
                        .param("foodItemId", "1")
                        .param("quantity", "1")
                        .param("pickupTimeSlot", "")
                        .param("pickupWindowMinutes", "2")
                        .param("pickupContactPhone", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("order/order-create"))
                .andExpect(model().attribute("activeTab", "pickup"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("Card payment rejects invalid card last four digits")
    void testCardPaymentValidationFailure() throws Exception {
        mockMvc.perform(post("/payments/1/card")
                        .session(createAdminSession())
                        .param("cardLastFour", "12")
                        .param("cardType", "VISA")
                        .param("authorizationCode", "AUTH-123")
                        .param("gatewayTransactionId", "GW-123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("Cash payment rejects tender lower than order total amount")
    void testCashPaymentValidationFailure() throws Exception {
        mockMvc.perform(post("/payments/1/cash")
                        .session(createAdminSession())
                        .param("amountTendered", "10.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));
    }
}
