package com.fooddelivery.controller;

import com.fooddelivery.model.food.Beverage;
import com.fooddelivery.model.food.FoodItem;
import com.fooddelivery.model.food.MainCourse;
import com.fooddelivery.service.FoodService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@Controller
@RequestMapping("/foods")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping
    public String listFoods(@RequestParam(value = "search", required = false) String search, Model model) {
        model.addAttribute("foodItems", foodService.searchFoodItems(search));
        model.addAttribute("searchQuery", search);
        return "food/food-list";
    }

    @GetMapping({"/new", "/add"})
    public String showAddFoodForm(Model model) {
        if (!model.containsAttribute("mainCourse")) {
            model.addAttribute("mainCourse", new MainCourse());
        }
        if (!model.containsAttribute("beverage")) {
            model.addAttribute("beverage", new Beverage());
        }
        if (!model.containsAttribute("activeTab")) {
            model.addAttribute("activeTab", "mainCourse");
        }
        return "food/food-add";
    }

    @PostMapping({"/new/main-course", "/add/main-course"})
    public String addMainCourse(@Valid @ModelAttribute("mainCourse") MainCourse mainCourse,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("beverage", new Beverage());
            model.addAttribute("activeTab", "mainCourse");
            return "food/food-add";
        }
        try {
            foodService.saveFoodItem(mainCourse);
            return "redirect:/foods";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("beverage", new Beverage());
            model.addAttribute("activeTab", "mainCourse");
            return "food/food-add";
        }
    }

    @PostMapping({"/new/beverage", "/add/beverage"})
    public String addBeverage(@Valid @ModelAttribute("beverage") Beverage beverage,
                              BindingResult bindingResult,
                              Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mainCourse", new MainCourse());
            model.addAttribute("activeTab", "beverage");
            return "food/food-add";
        }
        try {
            foodService.saveFoodItem(beverage);
            return "redirect:/foods";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("mainCourse", new MainCourse());
            model.addAttribute("activeTab", "beverage");
            return "food/food-add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditFoodForm(@PathVariable Long id, Model model) {
        FoodItem item = foodService.getFoodItemById(id);
        model.addAttribute("foodItem", item);
        return "food/food-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateFoodItem(@PathVariable Long id,
                                 @RequestParam(required = false) String name,
                                 @RequestParam(required = false) String description,
                                 @RequestParam(required = false) BigDecimal price,
                                 @RequestParam(required = false) Integer preparationTimeMinutes,
                                 @RequestParam(defaultValue = "false") boolean available,
                                 Model model) {
        FoodItem item = foodService.getFoodItemById(id);

        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("foodItem", item);
            model.addAttribute("errorMessage", "Food item name must not be blank.");
            return "food/food-edit";
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            model.addAttribute("foodItem", item);
            model.addAttribute("errorMessage", "Price must be strictly positive (> 0.00 LKR).");
            return "food/food-edit";
        }
        if (preparationTimeMinutes == null || preparationTimeMinutes <= 0) {
            model.addAttribute("foodItem", item);
            model.addAttribute("errorMessage", "Preparation time must be at least 1 minute.");
            return "food/food-edit";
        }

        try {
            item.setName(name);
            item.setDescription(description);
            item.setPrice(price);
            item.setPreparationTimeMinutes(preparationTimeMinutes);
            item.setAvailable(available);
            foodService.saveFoodItem(item);
            return "redirect:/foods";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("foodItem", item);
            model.addAttribute("errorMessage", ex.getMessage());
            return "food/food-edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteFoodItem(@PathVariable Long id) {
        foodService.deleteFoodItem(id);
        return "redirect:/foods";
    }
}
