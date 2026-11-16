package com.fooddelivery.controller;

import com.fooddelivery.common.exception.ResourceNotFoundException;
import com.fooddelivery.model.food.Beverage;
import com.fooddelivery.model.food.FoodItem;
import com.fooddelivery.model.food.MainCourse;
import com.fooddelivery.service.FoodService;
import com.fooddelivery.util.FoodFileHandler;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/foods")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping
    public String listFoods(@RequestParam(value = "search", required = false) String search,
                            @RequestParam(value = "status", required = false) String status,
                            Model model) {
        List<FoodItem> items = foodService.searchFoodItems(search);
        if ("sold_out".equalsIgnoreCase(status) || "sold-out".equalsIgnoreCase(status) || "soldout".equalsIgnoreCase(status) || "unavailable".equalsIgnoreCase(status)) {
            items = items.stream().filter(i -> !i.isAvailable()).toList();
        } else if ("in_stock".equalsIgnoreCase(status) || "in-stock".equalsIgnoreCase(status) || "available".equalsIgnoreCase(status)) {
            items = items.stream().filter(FoodItem::isAvailable).toList();
        }
        model.addAttribute("foodItems", items);
        model.addAttribute("searchQuery", search);
        model.addAttribute("currentStatusFilter", status);
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
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("beverage", new Beverage());
            model.addAttribute("activeTab", "mainCourse");
            return "food/food-add";
        }
        try {
            foodService.saveFoodItem(mainCourse);
            redirectAttributes.addFlashAttribute("successMessage", "Main Course '" + mainCourse.getName() + "' was successfully added.");
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
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mainCourse", new MainCourse());
            model.addAttribute("activeTab", "beverage");
            return "food/food-add";
        }
        try {
            foodService.saveFoodItem(beverage);
            redirectAttributes.addFlashAttribute("successMessage", "Beverage '" + beverage.getName() + "' was successfully added.");
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
                                 @RequestParam(required = false) String cuisineType,
                                 @RequestParam(required = false) Boolean vegetarian,
                                 @RequestParam(required = false) String spiceLevel,
                                 @RequestParam(required = false) String servingPortion,
                                 @RequestParam(required = false) String volumeSize,
                                 @RequestParam(required = false) Integer volumeMl,
                                 @RequestParam(required = false) Boolean cold,
                                 @RequestParam(required = false) Integer caffeineContentMg,
                                 @RequestParam(required = false) String sugarLevel,
                                 RedirectAttributes redirectAttributes,
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
            foodService.updateFoodItem(id, name, description, price, preparationTimeMinutes, available,
                    cuisineType, vegetarian, spiceLevel, servingPortion,
                    volumeSize, volumeMl, cold, caffeineContentMg, sugarLevel);
            redirectAttributes.addFlashAttribute("successMessage", "Food item '" + name + "' was successfully updated.");
            return "redirect:/foods";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("foodItem", item);
            model.addAttribute("errorMessage", ex.getMessage());
            return "food/food-edit";
        }
    }

    @RequestMapping(value = {"/{id}/toggle-availability", "/toggle-availability/{id}", "/toggle/{id}", "/{id}/toggle"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String toggleAvailability(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            FoodItem item = foodService.toggleAvailability(id);
            String status = item.isAvailable() ? "In Stock" : "Sold Out";
            redirectAttributes.addFlashAttribute("successMessage", "'" + item.getName() + "' is now marked as " + status + ".");
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/foods";
    }

    @PostMapping("/delete/{id}")
    public String deleteFoodItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            FoodItem item = foodService.getFoodItemById(id);
            foodService.deleteFoodItem(id);
            redirectAttributes.addFlashAttribute("successMessage", "Food item '" + item.getName() + "' was successfully deleted.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete this food item because it is associated with existing customer orders. You can mark it as 'Sold Out / Unavailable' instead.");
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/foods";
    }

    @GetMapping("/export")
    public void exportMenuCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"food_menu_catalog.csv\"");
        List<FoodItem> items = foodService.getAllFoodItems();
        FoodFileHandler.exportMenuToCsv(items, response.getWriter());
    }

    @PostMapping("/import")
    public String importMenuCsv(@RequestParam(value = "file", required = false) MultipartFile file,
                                @RequestParam(value = "csvText", required = false) String csvText,
                                RedirectAttributes redirectAttributes) {
        try {
            List<FoodItem> items;
            if (file != null && !file.isEmpty()) {
                try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
                    items = FoodFileHandler.loadMenuFromCsv(reader);
                }
            } else if (csvText != null && !csvText.trim().isEmpty()) {
                items = FoodFileHandler.loadMenuFromString(csvText);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Please provide a valid CSV file or paste CSV content.");
                return "redirect:/foods";
            }

            if (items.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "No valid food items found in the provided CSV.");
                return "redirect:/foods";
            }

            List<FoodItem> saved = foodService.importFoodItems(items);
            redirectAttributes.addFlashAttribute("successMessage", "Successfully imported " + saved.size() + " food item(s) into the menu.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to import CSV: " + ex.getMessage());
        }
        return "redirect:/foods";
    }
}
