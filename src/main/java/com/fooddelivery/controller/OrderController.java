package com.fooddelivery.controller;

import com.fooddelivery.common.exception.ResourceNotFoundException;
import com.fooddelivery.model.customer.Customer;
import com.fooddelivery.model.order.FoodOrder;
import com.fooddelivery.model.order.OnlineOrder;
import com.fooddelivery.model.order.PickupOrder;
import com.fooddelivery.service.CustomerService;
import com.fooddelivery.service.FoodService;
import com.fooddelivery.service.OperationsService;
import com.fooddelivery.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final FoodService foodService;
    private final CustomerService customerService;
    private final OperationsService operationsService;

    public OrderController(OrderService orderService,
                           FoodService foodService,
                           CustomerService customerService,
                           OperationsService operationsService) {
        this.orderService = orderService;
        this.foodService = foodService;
        this.customerService = customerService;
        this.operationsService = operationsService;
    }

    @GetMapping
    public String listOrders(@RequestParam(value = "orderNumber", required = false) String orderNumber,
                             @RequestParam(value = "status", required = false) String status,
                             @RequestParam(value = "type", required = false) String type,
                             HttpSession session, Model model) {
        if (session != null && "CUSTOMER".equalsIgnoreCase((String) session.getAttribute("userRole"))) {
            return "redirect:/orders/my-orders";
        }
        model.addAttribute("orders", orderService.searchOrders(orderNumber, status, type));
        model.addAttribute("orderNumber", orderNumber == null ? "" : orderNumber);
        model.addAttribute("status", status == null ? "" : status);
        model.addAttribute("type", type == null ? "" : type);
        model.addAttribute("filtered", hasText(orderNumber) || hasText(status) || hasText(type));
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

    @GetMapping({"/new", "/create"})
    public String showCreateOrderForm(@RequestParam(value = "tab", defaultValue = "online") String tab,
                                      @RequestParam(value = "food", required = false) Long foodId,
                                      HttpSession session, Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("foodItems", foodService.getAllActiveFoodItems());
        if (!model.containsAttribute("activeTab")) {
            model.addAttribute("activeTab", tab);
        }
        if (foodId != null && !model.containsAttribute("selectedFoodItemId")) {
            model.addAttribute("selectedFoodItemId", foodId);
        }

        Long customerId = (session != null) ? (Long) session.getAttribute("userId") : null;
        if (customerId != null) {
            Customer currentCustomer = customerService.getCustomerById(customerId);
            model.addAttribute("currentCustomer", currentCustomer);
            model.addAttribute("selectedCustomerId", customerId);
        }
        addOrderingChoices(model, session);

        return "order/order-create";
    }

    @PostMapping({"/new/online", "/create/online"})
    public String createOnlineOrder(@RequestParam(required = false) Long customerId,
                                    @RequestParam(required = false) String deliveryAddress,
                                    @RequestParam(required = false) String specialInstructions,
                                    @RequestParam(required = false) String deliveryZoneId,
                                    @RequestParam(defaultValue = "350.00") BigDecimal deliveryFee,
                                    @RequestParam(required = false) List<Long> foodItemId,
                                    @RequestParam(required = false) List<Integer> quantity,
                                    HttpSession session,
                                    Model model) {
        String cleanAddress = (deliveryAddress != null) ? deliveryAddress.trim() : "";
        customerId = customerIdForActor(session, customerId);
        Long selectedFoodId = firstFoodId(foodItemId);
        int selectedQuantity = firstQuantity(quantity);

        if (customerId == null) {
            return returnOrderError(model, session, "online", "Please select a customer for the delivery order.",
                    customerId, selectedFoodId, selectedQuantity, cleanAddress, specialInstructions, deliveryFee);
        }
        String itemError = itemSelectionError(foodItemId, quantity);
        if (itemError != null) {
            return returnOrderError(model, session, "online", itemError,
                    customerId, selectedFoodId, selectedQuantity, cleanAddress, specialInstructions, deliveryFee);
        }
        if (deliveryFee == null || deliveryFee.compareTo(BigDecimal.ZERO) < 0) {
            return returnOrderError(model, session, "online", "Delivery fee cannot be negative.",
                    customerId, selectedFoodId, selectedQuantity, cleanAddress, specialInstructions, deliveryFee);
        }
        if (cleanAddress.isEmpty()) {
            return returnOrderError(model, session, "online", "Delivery address is required for dispatch.",
                    customerId, selectedFoodId, selectedQuantity, cleanAddress, specialInstructions, deliveryFee);
        }

        try {
            Long zoneId = (deliveryZoneId == null || deliveryZoneId.isBlank()) ? null : Long.valueOf(deliveryZoneId.trim());
            if (zoneId != null) {
                deliveryFee = operationsService.getZone(zoneId).getFee();
            }
            Map<Long, Integer> items = itemMap(foodItemId, quantity);
            FoodOrder order = isAdmin(session)
                    ? orderService.createAdminOrder("ONLINE", customerId, cleanAddress, specialInstructions, deliveryFee,
                    null, 15, 1, null, false, items)
                    : orderService.createOnlineOrder(customerId, cleanAddress, specialInstructions, deliveryFee, items);
            if (zoneId != null) {
                operationsService.attachZone(order.getId(), zoneId);
            }

            if (session != null && "CUSTOMER".equalsIgnoreCase((String) session.getAttribute("userRole"))) {
                return "redirect:/payments/checkout/" + order.getId();
            }
            return "redirect:/orders";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return returnOrderError(model, session, "online", ex.getMessage(),
                    customerId, selectedFoodId, selectedQuantity, cleanAddress, specialInstructions, deliveryFee);
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
        addOrderingChoices(model, session);
        return "order/order-create";
    }

    @PostMapping({"/new/pickup", "/create/pickup"})
    public String createPickupOrder(@RequestParam(required = false) Long customerId,
                                    @RequestParam(required = false) String pickupTimeSlot,
                                    @RequestParam(defaultValue = "15") int pickupWindowMinutes,
                                    @RequestParam(defaultValue = "1") int counterNumber,
                                    @RequestParam(required = false) String pickupContactPhone,
                                    @RequestParam(defaultValue = "false") boolean curbsidePickup,
                                    @RequestParam(required = false) List<Long> foodItemId,
                                    @RequestParam(required = false) List<Integer> quantity,
                                    HttpSession session,
                                    Model model) {
        String cleanSlot = (pickupTimeSlot != null) ? pickupTimeSlot.trim() : "";
        String cleanPhone = (pickupContactPhone != null) ? pickupContactPhone.trim() : "";
        customerId = customerIdForActor(session, customerId);
        Long selectedFoodId = firstFoodId(foodItemId);
        int selectedQuantity = firstQuantity(quantity);

        if (customerId == null) {
            return returnPickupError(model, session, "Please select a customer for the pickup order.",
                    customerId, selectedFoodId, selectedQuantity, cleanSlot, pickupWindowMinutes, counterNumber, cleanPhone, curbsidePickup);
        }
        String itemError = itemSelectionError(foodItemId, quantity);
        if (itemError != null) {
            return returnPickupError(model, session, itemError,
                    customerId, selectedFoodId, selectedQuantity, cleanSlot, pickupWindowMinutes, counterNumber, cleanPhone, curbsidePickup);
        }
        if (cleanSlot.isEmpty()) {
            return returnPickupError(model, session, "Pickup time slot is required (e.g. 12:45 PM - 01:15 PM).",
                    customerId, selectedFoodId, selectedQuantity, cleanSlot, pickupWindowMinutes, counterNumber, cleanPhone, curbsidePickup);
        }
        if (pickupWindowMinutes < 5) {
            return returnPickupError(model, session, "Pickup window must be at least 5 minutes.",
                    customerId, selectedFoodId, selectedQuantity, cleanSlot, pickupWindowMinutes, counterNumber, cleanPhone, curbsidePickup);
        }
        if (counterNumber < 1) {
            return returnPickupError(model, session, "Counter number must be at least 1.",
                    customerId, selectedFoodId, selectedQuantity, cleanSlot, pickupWindowMinutes, counterNumber, cleanPhone, curbsidePickup);
        }
        if (cleanPhone.isEmpty()) {
            return returnPickupError(model, session, "Pickup contact phone is required.",
                    customerId, selectedFoodId, selectedQuantity, cleanSlot, pickupWindowMinutes, counterNumber, cleanPhone, curbsidePickup);
        }

        try {
            Map<Long, Integer> items = itemMap(foodItemId, quantity);
            FoodOrder order = isAdmin(session)
                    ? orderService.createAdminOrder("PICKUP", customerId, null, null, null,
                    cleanSlot, pickupWindowMinutes, counterNumber, cleanPhone, curbsidePickup, items)
                    : orderService.createPickupOrder(customerId, cleanSlot, pickupWindowMinutes, counterNumber,
                    cleanPhone, curbsidePickup, items);

            if (session != null && "CUSTOMER".equalsIgnoreCase((String) session.getAttribute("userRole"))) {
                return "redirect:/payments/checkout/" + order.getId();
            }
            return "redirect:/orders";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return returnPickupError(model, session, ex.getMessage(),
                    customerId, selectedFoodId, selectedQuantity, cleanSlot, pickupWindowMinutes, counterNumber, cleanPhone, curbsidePickup);
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
        addOrderingChoices(model, session);
        return "order/order-create";
    }

    @GetMapping({"/edit/{id}", "/{id}/status"})
    public String showOrderStatus(@PathVariable Long id,
                                  @RequestParam(value = "error", required = false) String error,
                                  Model model) {
        FoodOrder order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        model.addAttribute("statusChoices", orderService.statusChoices(order));
        model.addAttribute("canArchive", orderService.canArchive(order));
        addFeedback(model, order);
        model.addAttribute("adminViewer", true);
        if (error != null && !model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", error);
        }
        return "order/order-status";
    }

    @PostMapping({"/edit/{id}", "/{id}/status"})
    public String updateOrderStatus(@PathVariable Long id, @RequestParam String status,
                                    RedirectAttributes redirectAttributes) {
        try {
            FoodOrder updated = orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Order " + updated.getOrderNumber() + " is now " + updated.getOrderStatus() + ".");
            return "redirect:/orders/edit/" + id;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/orders/edit/" + id;
        }
    }

    @PostMapping("/edit/{id}/archive")
    public String archiveInvalidOrder(@PathVariable Long id, HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/orders/my-orders";
        }
        try {
            FoodOrder order = orderService.getOrderById(id);
            String orderNumber = order.getOrderNumber();
            orderService.deleteInvalidOrder(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Invalid order " + orderNumber + " was archived and removed from the active list.");
            return "redirect:/orders";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/orders/edit/" + id;
        }
    }

    @GetMapping("/my-orders/{id}")
    public String showCustomerOrder(@PathVariable Long id, HttpSession session, Model model,
                                    RedirectAttributes redirectAttributes) {
        if (isAdmin(session)) {
            return "redirect:/orders/edit/" + id;
        }
        FoodOrder order = orderService.getOrderById(id);
        String denied = denyUnlessOwner(session, order, redirectAttributes);
        if (denied != null) {
            return denied;
        }
        model.addAttribute("order", order);
        model.addAttribute("foodItems", foodService.getAllActiveFoodItems());
        model.addAttribute("editable", orderService.canCustomerEdit(order));
        model.addAttribute("onlineOrder", order instanceof OnlineOrder);
        model.addAttribute("pickupOrder", order instanceof PickupOrder);
        model.addAttribute("canCancel", orderService.canCancel(order));
        addFeedback(model, order);
        model.addAttribute("adminViewer", false);
        return "order/order-details";
    }

    @PostMapping("/my-orders/{id}/details")
    public String updateCustomerOrder(@PathVariable Long id,
                                      @RequestParam(required = false) String deliveryAddress,
                                      @RequestParam(required = false) String specialInstructions,
                                      @RequestParam(required = false) BigDecimal deliveryFee,
                                      @RequestParam(required = false) String pickupTimeSlot,
                                      @RequestParam(defaultValue = "15") int pickupWindowMinutes,
                                      @RequestParam(defaultValue = "1") int counterNumber,
                                      @RequestParam(required = false) String pickupContactPhone,
                                      @RequestParam(defaultValue = "false") boolean curbsidePickup,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        FoodOrder order = orderService.getOrderById(id);
        String denied = denyUnlessOwner(session, order, redirectAttributes);
        if (denied != null) {
            return denied;
        }
        try {
            if (order instanceof OnlineOrder) {
                if (deliveryAddress == null || deliveryAddress.trim().length() < 5) {
                    throw new IllegalArgumentException("Delivery address is required.");
                }
                if (deliveryFee == null || deliveryFee.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Delivery fee cannot be negative.");
                }
                orderService.updateOnlineOrder(id, deliveryAddress, specialInstructions, deliveryFee);
            } else if (order instanceof PickupOrder) {
                if (pickupTimeSlot == null || pickupTimeSlot.isBlank()) {
                    throw new IllegalArgumentException("Pickup time slot is required.");
                }
                if (pickupWindowMinutes < 5) {
                    throw new IllegalArgumentException("Pickup window must be at least 5 minutes.");
                }
                if (counterNumber < 1) {
                    throw new IllegalArgumentException("Counter number must be at least 1.");
                }
                if (pickupContactPhone == null || pickupContactPhone.isBlank()) {
                    throw new IllegalArgumentException("Pickup contact phone is required.");
                }
                orderService.updatePickupOrder(id, pickupTimeSlot, pickupWindowMinutes, counterNumber,
                        pickupContactPhone, curbsidePickup);
            }
            redirectAttributes.addFlashAttribute("successMessage",
                    "Order " + order.getOrderNumber() + " was updated. The order number and placement time are unchanged.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/my-orders/" + id;
    }

    @PostMapping("/my-orders/{id}/note")
    public String saveOrderNote(@PathVariable Long id, @RequestParam(required = false) String note,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        String denied = ownedOrderOrRedirect(id, session, redirectAttributes);
        if (denied != null) {
            return denied;
        }
        try {
            if (orderService.getOrderNote(id).isBlank()) {
                orderService.createOrderNote(id, note);
                redirectAttributes.addFlashAttribute("successMessage", "Order note added.");
            } else {
                orderService.updateOrderNote(id, note);
                redirectAttributes.addFlashAttribute("successMessage", "Order note updated.");
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/my-orders/" + id;
    }

    @PostMapping("/my-orders/{id}/note/delete")
    public String deleteOrderNote(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        String denied = ownedOrderOrRedirect(id, session, redirectAttributes);
        if (denied != null) {
            return denied;
        }
        try {
            orderService.deleteOrderNote(id);
            redirectAttributes.addFlashAttribute("successMessage", "Order note removed.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/my-orders/" + id;
    }

    @PostMapping("/my-orders/{id}/items")
    public String addOrderItem(@PathVariable Long id,
                               @RequestParam(required = false) Long foodItemId,
                               @RequestParam(defaultValue = "1") int quantity,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        String denied = ownedOrderOrRedirect(id, session, redirectAttributes);
        if (denied != null) {
            return denied;
        }
        try {
            if (foodItemId == null) {
                throw new IllegalArgumentException("Please select a food item.");
            }
            if (quantity < 1) {
                throw new IllegalArgumentException("Quantity must be at least 1.");
            }
            orderService.addOrderItem(id, foodItemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Food item added and the order total was recalculated.");
        } catch (IllegalArgumentException | IllegalStateException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/my-orders/" + id;
    }

    @PostMapping("/my-orders/{id}/items/{itemId}")
    public String updateOrderItemQuantity(@PathVariable Long id, @PathVariable Long itemId,
                                          @RequestParam(defaultValue = "0") int quantity,
                                          HttpSession session,
                                          RedirectAttributes redirectAttributes) {
        String denied = ownedOrderOrRedirect(id, session, redirectAttributes);
        if (denied != null) {
            return denied;
        }
        try {
            if (quantity < 1) {
                throw new IllegalArgumentException("Quantity must be at least 1.");
            }
            orderService.updateOrderItemQuantity(id, itemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Quantity updated and the order total was recalculated.");
        } catch (IllegalArgumentException | IllegalStateException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/my-orders/" + id;
    }

    @PostMapping("/my-orders/{id}/items/{itemId}/remove")
    public String removeOrderItem(@PathVariable Long id, @PathVariable Long itemId,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        String denied = ownedOrderOrRedirect(id, session, redirectAttributes);
        if (denied != null) {
            return denied;
        }
        try {
            orderService.removeOrderItem(id, itemId);
            redirectAttributes.addFlashAttribute("successMessage", "Food item removed and the order total was recalculated.");
        } catch (IllegalArgumentException | IllegalStateException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/my-orders/" + id;
    }

    @PostMapping({"/my-orders/{id}/cancel", "/{id}/cancel"})
    public String cancelOrder(@PathVariable Long id, HttpSession session,
                              RedirectAttributes redirectAttributes) {
        try {
            FoodOrder order = orderService.getOrderById(id);
            String denied = denyUnlessOwner(session, order, redirectAttributes);
            if (denied != null) {
                return denied;
            }
            FoodOrder cancelled = order instanceof PickupOrder
                    ? orderService.cancelPickupOrder(id)
                    : order instanceof OnlineOrder
                    ? orderService.cancelOnlineOrder(id)
                    : orderService.cancelOrder(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Order " + cancelled.getOrderNumber() + " was cancelled. It remains in your history.");
            return isAdmin(session) ? "redirect:/orders" : "redirect:/orders/my-orders";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return isAdmin(session) ? "redirect:/orders/edit/" + id : "redirect:/orders/my-orders/" + id;
        }
    }

    @GetMapping("/addresses")
    public String showAddresses(HttpSession session, Model model) {
        if (isAdmin(session)) {
            return "redirect:/orders";
        }
        Long userId = (Long) session.getAttribute("userId");
        model.addAttribute("addresses", operationsService.getAddresses(userId));
        return "order/addresses";
    }

    @PostMapping("/addresses")
    public String saveAddress(@RequestParam(required = false) Long id,
                              @RequestParam(required = false) String label,
                              @RequestParam(required = false) String addressLine,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (isAdmin(session)) {
            return "redirect:/orders";
        }
        Long userId = (Long) session.getAttribute("userId");
        try {
            if (id == null) {
                operationsService.createAddress(userId, label, addressLine);
                redirectAttributes.addFlashAttribute("successMessage", "Address saved.");
            } else {
                operationsService.updateAddress(id, userId, label, addressLine);
                redirectAttributes.addFlashAttribute("successMessage", "Address updated.");
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/addresses";
    }

    @PostMapping("/addresses/{id}/delete")
    public String deleteAddress(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (isAdmin(session)) {
            return "redirect:/orders";
        }
        try {
            operationsService.deleteAddress(id, (Long) session.getAttribute("userId"));
            redirectAttributes.addFlashAttribute("successMessage", "Address removed.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/addresses";
    }

    @PostMapping("/my-orders/{id}/complaints")
    public String saveComplaint(@PathVariable Long id,
                                @RequestParam(required = false) Long complaintId,
                                @RequestParam(required = false) String message,
                                @RequestParam(required = false) String status,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        String denied = ownedOrderOrRedirect(id, session, redirectAttributes);
        if (denied != null) {
            return denied;
        }
        try {
            FoodOrder order = orderService.getOrderById(id);
            Long actor = isAdmin(session) ? null : (Long) session.getAttribute("userId");
            if (complaintId == null) {
                operationsService.createComplaint(id, order.getCustomer().getId(), message);
                redirectAttributes.addFlashAttribute("successMessage", "Complaint added.");
            } else {
                String nextStatus = isAdmin(session) ? status : null;
                operationsService.updateComplaint(complaintId, actor, message, nextStatus);
                redirectAttributes.addFlashAttribute("successMessage", "Complaint updated.");
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return feedbackRedirect(id, session);
    }

    @PostMapping("/my-orders/{id}/complaints/{complaintId}/delete")
    public String deleteComplaint(@PathVariable Long id, @PathVariable Long complaintId,
                                  HttpSession session, RedirectAttributes redirectAttributes) {
        String denied = ownedOrderOrRedirect(id, session, redirectAttributes);
        if (denied != null) {
            return denied;
        }
        try {
            Long actor = isAdmin(session) ? null : (Long) session.getAttribute("userId");
            operationsService.deleteComplaint(complaintId, actor);
            redirectAttributes.addFlashAttribute("successMessage", "Complaint removed.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return feedbackRedirect(id, session);
    }

    @PostMapping("/my-orders/{id}/review")
    public String saveReview(@PathVariable Long id,
                             @RequestParam(required = false) Long reviewId,
                             @RequestParam(defaultValue = "5") int rating,
                             @RequestParam(required = false) String comment,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        String denied = ownedOrderOrRedirect(id, session, redirectAttributes);
        if (denied != null) {
            return denied;
        }
        try {
            FoodOrder order = orderService.getOrderById(id);
            Long actor = isAdmin(session) ? null : (Long) session.getAttribute("userId");
            if (reviewId == null) {
                operationsService.createReview(id, order.getCustomer().getId(), rating, comment);
                redirectAttributes.addFlashAttribute("successMessage", "Review added.");
            } else {
                operationsService.updateReview(reviewId, actor, rating, comment);
                redirectAttributes.addFlashAttribute("successMessage", "Review updated.");
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return feedbackRedirect(id, session);
    }

    @PostMapping("/my-orders/{id}/review/delete")
    public String deleteReview(@PathVariable Long id, @RequestParam Long reviewId,
                               HttpSession session, RedirectAttributes redirectAttributes) {
        String denied = ownedOrderOrRedirect(id, session, redirectAttributes);
        if (denied != null) {
            return denied;
        }
        try {
            Long actor = isAdmin(session) ? null : (Long) session.getAttribute("userId");
            operationsService.deleteReview(reviewId, actor);
            redirectAttributes.addFlashAttribute("successMessage", "Review removed.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return feedbackRedirect(id, session);
    }

    @PostMapping("/my-orders/{id}/review/reply")
    public String replyToReview(@PathVariable Long id, @RequestParam Long reviewId,
                                @RequestParam(required = false) String reply,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        String denied = ownedOrderOrRedirect(id, session, redirectAttributes);
        if (denied != null) {
            return denied;
        }
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only an administrator can reply to a review.");
            return feedbackRedirect(id, session);
        }
        try {
            operationsService.replyToReview(reviewId, reply);
            redirectAttributes.addFlashAttribute("successMessage", "Reply saved.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return feedbackRedirect(id, session);
    }

    private String feedbackRedirect(Long id, HttpSession session) {
        return isAdmin(session) ? "redirect:/orders/edit/" + id : "redirect:/orders/my-orders/" + id;
    }

    private void addFeedback(Model model, FoodOrder order) {
        model.addAttribute("complaints", operationsService.getComplaints(order.getId()));
        model.addAttribute("review", operationsService.getReview(order.getId()).orElse(null));
        model.addAttribute("canComplain", "DELIVERED".equals(order.getOrderStatus()) || "CANCELLED".equals(order.getOrderStatus()));
        model.addAttribute("canReview", true);
    }

    private void addOrderingChoices(Model model, HttpSession session) {
        model.addAttribute("deliveryZones", operationsService.getZones());
        Long userId = session == null ? null : (Long) session.getAttribute("userId");
        model.addAttribute("savedAddresses", userId == null ? List.of() : operationsService.getAddresses(userId));
    }

    private String ownedOrderOrRedirect(Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        FoodOrder order = orderService.getOrderById(id);
        return denyUnlessOwner(session, order, redirectAttributes);
    }

    private String denyUnlessOwner(HttpSession session, FoodOrder order, RedirectAttributes redirectAttributes) {
        if (isAdmin(session)) {
            return null;
        }
        Long userId = session == null ? null : (Long) session.getAttribute("userId");
        if (userId == null || order.getCustomer() == null || !userId.equals(order.getCustomer().getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only view and change your own orders.");
            return "redirect:/orders/my-orders";
        }
        return null;
    }

    private Long customerIdForActor(HttpSession session, Long submittedCustomerId) {
        if (session != null && "CUSTOMER".equalsIgnoreCase((String) session.getAttribute("userRole"))) {
            return (Long) session.getAttribute("userId");
        }
        return submittedCustomerId;
    }

    private boolean isAdmin(HttpSession session) {
        return session != null && "ADMIN".equalsIgnoreCase((String) session.getAttribute("userRole"));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String itemSelectionError(List<Long> foodItemIds, List<Integer> quantities) {
        if (foodItemIds == null || foodItemIds.stream().noneMatch(id -> id != null)) {
            return "Please select a food dish or beverage to order.";
        }
        for (int i = 0; i < foodItemIds.size(); i++) {
            Long foodId = foodItemIds.get(i);
            if (foodId == null) {
                continue;
            }
            Integer qty = (quantities != null && i < quantities.size()) ? quantities.get(i) : 1;
            if (qty == null || qty < 1) {
                return "Quantity must be at least 1 item.";
            }
            try {
                FoodItem foodItem = foodService.getFoodItemById(foodId);
                if (!foodItem.isAvailable()) {
                    return "'" + foodItem.getName() + "' is currently sold out and cannot be ordered.";
                }
            } catch (ResourceNotFoundException ignored) {
            }
        }
        return null;
    }

    private Map<Long, Integer> itemMap(List<Long> foodItemIds, List<Integer> quantities) {
        Map<Long, Integer> items = new LinkedHashMap<>();
        for (int i = 0; i < foodItemIds.size(); i++) {
            Long id = foodItemIds.get(i);
            if (id == null) {
                continue;
            }
            int qty = (quantities != null && i < quantities.size() && quantities.get(i) != null)
                    ? quantities.get(i) : 1;
            items.merge(id, qty, Integer::sum);
        }
        return items;
    }

    private Long firstFoodId(List<Long> foodItemIds) {
        if (foodItemIds == null) {
            return null;
        }
        return foodItemIds.stream().filter(id -> id != null).findFirst().orElse(null);
    }

    private int firstQuantity(List<Integer> quantities) {
        if (quantities == null || quantities.isEmpty() || quantities.get(0) == null) {
            return 1;
        }
        return quantities.get(0);
    }
}
