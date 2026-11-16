package com.fooddelivery.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FoodIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession createAdminSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userRole", "ADMIN");
        session.setAttribute("userName", "Administrator");
        session.setAttribute("userEmail", "admin@foodflow.lk");
        return session;
    }

    private MockHttpSession createCustomerSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userRole", "CUSTOMER");
        session.setAttribute("userId", 1L);
        return session;
    }

    @Test
    @DisplayName("Admin can view food menu list")
    void testAdminListFoods() throws Exception {
        mockMvc.perform(get("/foods")
                        .session(createAdminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("food/food-list"))
                .andExpect(model().attributeExists("foodItems"));
    }

    @Test
    @DisplayName("Admin can search food items by keyword")
    void testAdminSearchFoods() throws Exception {
        mockMvc.perform(get("/foods")
                        .param("search", "Biryani")
                        .session(createAdminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("food/food-list"))
                .andExpect(model().attributeExists("foodItems", "searchQuery"));
    }

    @Test
    @DisplayName("Customer is denied access to admin food menu directory")
    void testCustomerDeniedAccessToFoods() throws Exception {
        mockMvc.perform(get("/foods")
                        .session(createCustomerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/my-orders"));
    }

    @Test
    @DisplayName("Admin can add a new Main Course")
    void testAddMainCourseSuccess() throws Exception {
        mockMvc.perform(post("/foods/new/main-course")
                        .session(createAdminSession())
                        .param("name", "Pol Roti with Lunu Miris Special")
                        .param("description", "Traditional coconut flatbread with fiery onion-chilli sambol")
                        .param("price", "750.00")
                        .param("preparationTimeMinutes", "15")
                        .param("cuisineType", "Authentic Village")
                        .param("vegetarian", "true")
                        .param("spiceLevel", "HOT")
                        .param("servingPortion", "3 Roti with Clay Bowl Sambol"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/foods"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("Admin can add a new Beverage")
    void testAddBeverageSuccess() throws Exception {
        mockMvc.perform(post("/foods/new/beverage")
                        .session(createAdminSession())
                        .param("name", "Fresh Woodapple Nectar")
                        .param("description", "Velvety woodapple pulp blended with organic coconut milk and kithul treacle")
                        .param("price", "550.00")
                        .param("preparationTimeMinutes", "5")
                        .param("volumeSize", "Tall Glass (350ml)")
                        .param("volumeMl", "350")
                        .param("cold", "true")
                        .param("caffeineContentMg", "0")
                        .param("sugarLevel", "MEDIUM"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/foods"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("Add food item form validates blank name")
    void testAddMainCourseValidationFailure() throws Exception {
        mockMvc.perform(post("/foods/new/main-course")
                        .session(createAdminSession())
                        .param("name", "")
                        .param("price", "1000.00")
                        .param("preparationTimeMinutes", "20")
                        .param("cuisineType", "Sri Lankan")
                        .param("spiceLevel", "MILD")
                        .param("servingPortion", "Standard"))
                .andExpect(status().isOk())
                .andExpect(view().name("food/food-add"));
    }

    @Test
    @DisplayName("Admin can toggle food item availability")
    void testToggleFoodAvailability() throws Exception {
        // Toggle to unavailable
        mockMvc.perform(post("/foods/1/toggle-availability")
                        .session(createAdminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/foods"))
                .andExpect(flash().attributeExists("successMessage"));

        // Toggle back to available to preserve state for other test suites
        mockMvc.perform(post("/foods/1/toggle-availability")
                        .session(createAdminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/foods"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("Admin can update an existing food item with subclass fields")
    void testUpdateFoodItem() throws Exception {
        mockMvc.perform(post("/foods/edit/1")
                        .session(createAdminSession())
                        .param("name", "Chicken Dum Biryani Special (Updated)")
                        .param("description", "Updated description with extra raita")
                        .param("price", "1950.00")
                        .param("preparationTimeMinutes", "25")
                        .param("available", "true")
                        .param("cuisineType", "Sri Lankan Dum Special")
                        .param("vegetarian", "false")
                        .param("spiceLevel", "HOT")
                        .param("servingPortion", "Full Clay Pot"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/foods"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("Deleting a food item linked to orders shows safe error message instead of 500")
    void testDeleteOrderedFoodItemHandlesReferentialIntegrity() throws Exception {
        // Food item 1 is linked to Order 1 in data.sql
        mockMvc.perform(post("/foods/delete/1")
                        .session(createAdminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/foods"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("Admin can export food menu to CSV")
    void testExportMenuCsv() throws Exception {
        mockMvc.perform(get("/foods/export")
                        .session(createAdminSession()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"food_menu_catalog.csv\""))
                .andExpect(content().contentType("text/csv;charset=UTF-8"));
    }

    @Test
    @DisplayName("Admin can import food menu from CSV upload")
    void testImportMenuCsv() throws Exception {
        String csv = """
                TYPE,NAME,DESCRIPTION,PRICE,PREPARATION_TIME,EXTRA1,EXTRA2,EXTRA3,EXTRA4,EXTRA5
                MAIN_COURSE,Test Import Kottu,Spicy dinner,1600.00,15,Western,false,MEDIUM,Standard
                """;
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/foods/import")
                        .file(file)
                        .session(createAdminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/foods"))
                .andExpect(flash().attributeExists("successMessage"));
    }
}

