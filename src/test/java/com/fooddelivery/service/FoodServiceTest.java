package com.fooddelivery.service;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FoodServiceTest {

    @Mock
    private FoodItemRepository foodItemRepository;

    @InjectMocks
    private FoodService foodService;

    private MainCourse biryani;

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
}
