package com.fooddelivery.service;

import com.fooddelivery.common.exception.ResourceNotFoundException;
import com.fooddelivery.model.food.Beverage;
import com.fooddelivery.model.food.FoodItem;
import com.fooddelivery.model.food.MainCourse;
import com.fooddelivery.repository.FoodItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class FoodServiceTest {

    @Mock
    private FoodItemRepository foodItemRepository;

    @InjectMocks
    private FoodService foodService;

    private MainCourse biryani;
    private Beverage faluda;

    @BeforeEach
    public void setUp() {
        biryani = new MainCourse(
                "Chicken Dum Biryani Special",
                "Authentic basmati rice dum cooked with marinated chicken and Sri Lankan spices",
                new BigDecimal("1850.00"),
                25,
                "Sri Lankan Dum",
                false,
                "MEDIUM",
                "Full Clay Pot with Boiled Egg & Gravy"
        );

        faluda = new Beverage(
                "Royal Faluda",
                "Chilled sweet rose milk drink",
                new BigDecimal("650.00"),
                8,
                "Tall Glass (400ml)",
                400,
                true,
                0,
                "HIGH"
        );
    }

    @Test
    void testGetAllActiveFoodItems() {
        when(foodItemRepository.findByAvailableTrue()).thenReturn(List.of(biryani));
        List<FoodItem> items = foodService.getAllActiveFoodItems();
        assertEquals(1, items.size());
        assertEquals("Chicken Dum Biryani Special", items.get(0).getName());
        assertEquals(new BigDecimal("1850.00"), items.get(0).getPrice());
    }

    @Test
    void testGetFoodItemById() {
        when(foodItemRepository.findById(1L)).thenReturn(Optional.of(biryani));
        FoodItem item = foodService.getFoodItemById(1L);
        assertNotNull(item);
        assertEquals(new BigDecimal("1850.00"), item.getPrice());
        assertEquals("Main Course (Sri Lankan Dum)", item.getFoodCategory());
    }

    @Test
    void testGetFoodItemByIdNotFound() {
        when(foodItemRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> foodService.getFoodItemById(99L));
    }

    @Test
    void testSearchFoodItems() {
        when(foodItemRepository.searchByName("Biryani")).thenReturn(List.of(biryani));
        List<FoodItem> items = foodService.searchFoodItems("Biryani");
        assertEquals(1, items.size());
        assertEquals("Chicken Dum Biryani Special", items.get(0).getName());
    }

    @Test
    void testSearchFoodItemsEmptyReturnsAll() {
        when(foodItemRepository.findAll()).thenReturn(List.of(biryani, faluda));
        List<FoodItem> items = foodService.searchFoodItems("   ");
        assertEquals(2, items.size());
    }

    @Test
    void testToggleAvailability() {
        assertTrue(biryani.isAvailable());
        when(foodItemRepository.findById(1L)).thenReturn(Optional.of(biryani));
        when(foodItemRepository.save(any(FoodItem.class))).thenAnswer(i -> i.getArgument(0));

        FoodItem toggled = foodService.toggleAvailability(1L);
        assertFalse(toggled.isAvailable());

        // Toggle back
        FoodItem toggledBack = foodService.toggleAvailability(1L);
        assertTrue(toggledBack.isAvailable());
    }

    @Test
    void testUpdateFoodItemMainCourse() {
        when(foodItemRepository.findById(1L)).thenReturn(Optional.of(biryani));
        when(foodItemRepository.save(any(FoodItem.class))).thenAnswer(i -> i.getArgument(0));

        FoodItem updated = foodService.updateFoodItem(1L, "Biryani Platinum", "Upgraded recipe",
                new BigDecimal("2100.00"), 30, true,
                "Jaffna Style", true, "EXTRA_HOT", "Pot for 2",
                null, null, null, null, null);

        assertEquals("Biryani Platinum", updated.getName());
        assertEquals(new BigDecimal("2100.00"), updated.getPrice());
        assertTrue(updated instanceof MainCourse);
        MainCourse mc = (MainCourse) updated;
        assertEquals("Jaffna Style", mc.getCuisineType());
        assertTrue(mc.isVegetarian());
        assertEquals("EXTRA_HOT", mc.getSpiceLevel());
    }

    @Test
    void testUpdateFoodItemBeverage() {
        when(foodItemRepository.findById(2L)).thenReturn(Optional.of(faluda));
        when(foodItemRepository.save(any(FoodItem.class))).thenAnswer(i -> i.getArgument(0));

        FoodItem updated = foodService.updateFoodItem(2L, "Faluda Deluxe", "With extra scoop",
                new BigDecimal("800.00"), 10, true,
                null, null, null, null,
                "Jumbo Glass (500ml)", 500, true, 10, "LOW");

        assertEquals("Faluda Deluxe", updated.getName());
        assertTrue(updated instanceof Beverage);
        Beverage bev = (Beverage) updated;
        assertEquals(500, bev.getVolumeMl());
        assertEquals("LOW", bev.getSugarLevel());
    }

    @Test
    void testImportFoodItems() {
        when(foodItemRepository.saveAll(anyList())).thenReturn(List.of(biryani, faluda));
        List<FoodItem> imported = foodService.importFoodItems(List.of(biryani, faluda));
        assertEquals(2, imported.size());
    }

    @Test
    void testDeleteFoodItem() {
        when(foodItemRepository.findById(1L)).thenReturn(Optional.of(biryani));
        foodService.deleteFoodItem(1L);
        verify(foodItemRepository, times(1)).delete(biryani);
    }

    @Test
    void testGetMainCoursesAndBeverages() {
        when(foodItemRepository.findAllMainCourses()).thenReturn(List.of(biryani));
        when(foodItemRepository.findAllBeverages()).thenReturn(List.of(faluda));

        assertEquals(1, foodService.getMainCourses().size());
        assertEquals(1, foodService.getBeverages().size());
    }
}
