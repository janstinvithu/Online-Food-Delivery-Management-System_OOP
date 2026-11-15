package com.fooddelivery.controller;

import com.fooddelivery.model.customer.Customer;
import com.fooddelivery.model.order.FoodOrder;
import com.fooddelivery.model.order.OnlineOrder;
import com.fooddelivery.model.order.PickupOrder;
import com.fooddelivery.service.CustomerService;
import com.fooddelivery.service.FoodService;
import com.fooddelivery.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final FoodService foodService;
    private final CustomerService customerService;

    public OrderController(OrderService orderService,
                           FoodService foodService,
                           CustomerService customerService) {
        this.orderService = orderService;
        this.foodService = foodService;
        this.customerService = customerService;
    }

    @GetMapping
    public String listOrders(@RequestParam(value = "status", required = false) String status,
                             @RequestParam(value = "search", required = false) String search,
                             HttpSession session, Model model) {
        if (session != null && "CUSTOMER".equalsIgnoreCase((String) session.getAttribute("userRole"))) {
            return "redirect:/orders/my-orders";
        }

        List<FoodOrder> orders;
        if (search != null && !search.trim().isEmpty()) {
            orders = orderService.searchOrders(search.trim());
        } else if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            orders = orderService.getOrdersByStatus(status.trim());
        } else {
            orders = orderService.getAllOrders();
        }

        model.addAttribute("orders", orders);
        model.addAttribute("currentStatus", (status != null && !status.trim().isEmpty()) ? status.trim().toUpperCase() : "ALL");
        model.addAttribute("searchQuery", (search != null) ? search.trim() : "");
        model.addAttribute("stats", orderService.getOrderSummaryStats());
        return "order/order-history";
    }

    @GetMapping("/my-orders")
    public String myOrders(@RequestParam(value = "created", required = false) String createdOrderNumber,
                           HttpSession session,
                           Model model) {
        Long customerId = (session != null) ? (Long) session.getAttribute("userId") : null;
        if (customerId == null) {
            return "redirect:/login";
        }

        Customer customer = customerService.getCustomerById(customerId);
        List<FoodOrder> orders = orderService.getOrdersByCustomerId(customerId);

        model.addAttribute("customer", customer);
        model.addAttribute("orders", orders);
        model.addAttribute("createdOrderNumber", createdOrderNumber);
        return "order/my-orders";
    }

    @PostMapping("/my-orders/{id}/cancel")
    public String cancelMyOrder(@PathVariable Long id,
                                HttpSession session,
                                org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Long customerId = (session != null) ? (Long) session.getAttribute("userId") : null;
        if (customerId == null) {
            return "redirect:/login";
        }
        try {
            FoodOrder order = orderService.getOrderById(id);
            if (!order.getCustomer().getId().equals(customerId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized: You do not have permission to cancel this order.");
                return "redirect:/orders/my-orders";
            }
            orderService.cancelOrder(id);
            redirectAttributes.addFlashAttribute("successMessage", "Order #" + order.getOrderNumber() + " was successfully cancelled.");
        } catch (IllegalStateException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/my-orders";
    }

    @GetMapping({"/new", "/create"})
    public String showCreateOrderForm(@RequestParam(value = "tab", defaultValue = "online") String tab,
                                      HttpSession session, Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("foodItems", foodService.getAllActiveFoodItems());
        if (!model.containsAttribute("activeTab")) {
            model.addAttribute("activeTab", tab);
        }

        Long customerId = (session != null) ? (Long) session.getAttribute("userId") : null;
        if (customerId != null) {
            Customer currentCustomer = customerService.getCustomerById(customerId);
            model.addAttribute("currentCustomer", currentCustomer);
            model.addAttribute("selectedCustomerId", customerId);
        }

        return "order/order-create";
    }

    @PostMapping({"/new/online", "/create/online"})
    public String createOnlineOrder(@RequestParam(required = false) Long customerId,
                                    @RequestParam(required = false) String deliveryAddress,
                                    @RequestParam(required = false) String specialInstructions,
                                    @RequestParam(defaultValue = "350.00") BigDecimal deliveryFee,
                                    @RequestParam(required = false) Long foodItemId,
                                    @RequestParam(defaultValue = "1") int quantity,
                                    HttpSession session,
                                    Model model) {
        String cleanAddress = (deliveryAddress != null) ? deliveryAddress.trim() : "";

        if (customerId == null) {
            return returnOrderError(model, session, "online", "Please select a customer for the delivery order.",
                    customerId, foodItemId, quantity, cleanAddress, specialInstructions, deliveryFee);
        }
        if (foodItemId == null) {
            return returnOrderError(model, session, "online", "Please select a food dish or beverage to order.",
                    customerId, foodItemId, quantity, cleanAddress, specialInstructions, deliveryFee);
        }
        if (quantity < 1) {
            return returnOrderError(model, session, "online", "Quantity must be at least 1 item.",
                    customerId, foodItemId, quantity, cleanAddress, specialInstructions, deliveryFee);
        }
        if (deliveryFee == null || deliveryFee.compareTo(BigDecimal.ZERO) < 0) {
            return returnOrderError(model, session, "online", "Delivery fee cannot be negative.",
                    customerId, foodItemId, quantity, cleanAddress, specialInstructions, deliveryFee);
        }
        if (cleanAddress.isEmpty()) {
            return returnOrderError(model, session, "online", "Delivery address is required for dispatch.",
                    customerId, foodItemId, quantity, cleanAddress, specialInstructions, deliveryFee);
        }

        try {
            Map<Long, Integer> items = new HashMap<>();
            items.put(foodItemId, quantity);
            OnlineOrder order = orderService.createOnlineOrder(customerId, cleanAddress, specialInstructions, deliveryFee, items);

            if (session != null && "CUSTOMER".equalsIgnoreCase((String) session.getAttribute("userRole"))) {
                return "redirect:/payments/checkout/" + order.getId();
            }
            return "redirect:/orders";
        } catch (IllegalArgumentException ex) {
            return returnOrderError(model, session, "online", ex.getMessage(),
                    customerId, foodItemId, quantity, cleanAddress, specialInstructions, deliveryFee);
        }
    }

    private String returnOrderError(Model model, HttpSession session, String tab, String errorMsg,
                                    Long custId, Long foodId, int qty, String addr, String instr, BigDecimal fee) {
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("foodItems", foodService.getAllActiveFoodItems());
        model.addAttribute("activeTab", tab);
        model.addAttribute("errorMessage", errorMsg);
        model.addAttribute("selectedCustomerId", custId);
        model.addAttribute("selectedFoodItemId", foodId);
        model.addAttribute("enteredQuantity", qty);
        model.addAttribute("enteredAddress", addr);
        model.addAttribute("enteredInstructions", instr);
        model.addAttribute("enteredDeliveryFee", fee);

        Long sessionCustId = (session != null) ? (Long) session.getAttribute("userId") : null;
        if (sessionCustId != null) {
            model.addAttribute("currentCustomer", customerService.getCustomerById(sessionCustId));
        }
        return "order/order-create";
    }

    @PostMapping({"/new/pickup", "/create/pickup"})
    public String createPickupOrder(@RequestParam(required = false) Long customerId,
                                    @RequestParam(required = false) String pickupTimeSlot,
                                    @RequestParam(defaultValue = "15") int pickupWindowMinutes,
                                    @RequestParam(defaultValue = "1") int counterNumber,
                                    @RequestParam(required = false) String pickupContactPhone,
                                    @RequestParam(defaultValue = "false") boolean curbsidePickup,
                                    @RequestParam(required = false) Long foodItemId,
                                    @RequestParam(defaultValue = "1") int quantity,
                                    HttpSession session,
                                    Model model) {
        String cleanSlot = (pickupTimeSlot != null) ? pickupTimeSlot.trim() : "";
        String cleanPhone = (pickupContactPhone != null) ? pickupContactPhone.trim() : "";

        if (customerId == null) {
            return returnPickupError(model, session, "Please select a customer for the pickup order.",
                    customerId, foodItemId, quantity, cleanSlot, pickupWindowMinutes, counterNumber, cleanPhone, curbsidePickup);
        }
        if (foodItemId == null) {
            return returnPickupError(model, session, "Please select a food dish or beverage to order.",
                    customerId, foodItemId, quantity, cleanSlot, pickupWindowMinutes, counterNumber, cleanPhone, curbsidePickup);
        }
        if (quantity < 1) {
            return returnPickupError(model, session, "Quantity must be at least 1 item.",
                    customerId, foodItemId, quantity, cleanSlot, pickupWindowMinutes, counterNumber, cleanPhone, curbsidePickup);
        }
        if (cleanSlot.isEmpty()) {
            return returnPickupError(model, session, "Pickup time slot is required (e.g. 12:45 PM - 01:15 PM).",
                    customerId, foodItemId, quantity, cleanSlot, pickupWindowMinutes, counterNumber, cleanPhone, curbsidePickup);
        }
        if (pickupWindowMinutes < 5) {
            return returnPickupError(model, session, "Pickup window must be at least 5 minutes.",
                    customerId, foodItemId, quantity, cleanSlot, pickupWindowMinutes, counterNumber, cleanPhone, curbsidePickup);
        }
        if (counterNumber < 1) {
            return returnPickupError(model, session, "Counter number must be at least 1.",
                    customerId, foodItemId, quantity, cleanSlot, pickupWindowMinutes, counterNumber, cleanPhone, curbsidePickup);
        }
        if (cleanPhone.isEmpty()) {
            return returnPickupError(model, session, "Pickup contact phone is required.",
                    customerId, foodItemId, quantity, cleanSlot, pickupWindowMinutes, counterNumber, cleanPhone, curbsidePickup);
        }

        try {
            Map<Long, Integer> items = new HashMap<>();
            items.put(foodItemId, quantity);
            PickupOrder order = orderService.createPickupOrder(customerId, cleanSlot, pickupWindowMinutes, counterNumber,
                    cleanPhone, curbsidePickup, items);

            if (session != null && "CUSTOMER".equalsIgnoreCase((String) session.getAttribute("userRole"))) {
                return "redirect:/payments/checkout/" + order.getId();
            }
            return "redirect:/orders";
        } catch (IllegalArgumentException ex) {
            return returnPickupError(model, session, ex.getMessage(),
                    customerId, foodItemId, quantity, cleanSlot, pickupWindowMinutes, counterNumber, cleanPhone, curbsidePickup);
        }
    }

    private String returnPickupError(Model model, HttpSession session, String errorMsg,
                                     Long custId, Long foodId, int qty, String slot, int window,
                                     int counter, String phone, boolean curbside) {
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("foodItems", foodService.getAllActiveFoodItems());
        model.addAttribute("activeTab", "pickup");
        model.addAttribute("errorMessage", errorMsg);
        model.addAttribute("selectedCustomerId", custId);
        model.addAttribute("selectedFoodItemId", foodId);
        model.addAttribute("enteredQuantity", qty);
        model.addAttribute("enteredPickupTimeSlot", slot);
        model.addAttribute("enteredPickupWindow", window);
        model.addAttribute("enteredCounterNumber", counter);
        model.addAttribute("enteredPickupPhone", phone);
        model.addAttribute("enteredCurbside", curbside);

        Long sessionCustId = (session != null) ? (Long) session.getAttribute("userId") : null;
        if (sessionCustId != null) {
            model.addAttribute("currentCustomer", customerService.getCustomerById(sessionCustId));
        }
        return "order/order-create";
    }

    @GetMapping({"/edit/{id}", "/{id}/status"})
    public String showOrderStatus(@PathVariable Long id,
                                  @RequestParam(value = "error", required = false) String error,
                                  Model model) {
        FoodOrder order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        if (error != null && !model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", error);
        }
        return "order/order-status";
    }

    @PostMapping({"/edit/{id}", "/{id}/status"})
    public String updateOrderStatus(@PathVariable Long id, @RequestParam String status,
                                    org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, status);
            return "redirect:/orders/edit/" + id;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/orders/edit/" + id;
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id,
                              org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(id);
            return "redirect:/orders";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/orders/edit/" + id;
        }
    }
}
