package com.fooddelivery.controller;

import com.fooddelivery.model.delivery.Delivery;
import com.fooddelivery.model.delivery.Payment;
import com.fooddelivery.model.order.FoodOrder;
import com.fooddelivery.service.DeliveryService;
import com.fooddelivery.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final OrderService orderService;

    public DeliveryController(DeliveryService deliveryService, OrderService orderService) {
        this.deliveryService = deliveryService;
        this.orderService = orderService;
    }

    @GetMapping("/deliveries")
    public String listDeliveries(Model model) {
        model.addAttribute("deliveries", deliveryService.getAllDeliveries());
        model.addAttribute("couriers", deliveryService.getAllCouriers());
        return "delivery/delivery-tracking";
    }

    @GetMapping({"/deliveries/new", "/deliveries/assign/{id}"})
    public String showAssignRiderForm(@PathVariable(required = false) Long id, Model model) {
        List<Delivery> pendingDeliveries = deliveryService.getAllDeliveries().stream()
                .filter(d -> d.getDeliveryPerson() == null)
                .toList();
        model.addAttribute("pendingDeliveries", pendingDeliveries);

        Delivery selectedDelivery = (id != null) ? deliveryService.getDeliveryById(id)
                : (pendingDeliveries.isEmpty() ? null : pendingDeliveries.get(0));
        model.addAttribute("delivery", selectedDelivery);
        model.addAttribute("availableCouriers", deliveryService.getAvailableCouriers());
        return "delivery/delivery-assign";
    }

    @PostMapping({"/deliveries/new", "/deliveries/assign/{id}"})
    public String assignCourier(@PathVariable(required = false) Long id,
                                @RequestParam(required = false) Long deliveryId,
                                @RequestParam(required = false) Long courierId,
                                RedirectAttributes redirectAttributes) {
        Long targetDeliveryId = (id != null) ? id : deliveryId;
        if (targetDeliveryId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a delivery to assign.");
            return "redirect:/deliveries/new";
        }
        if (courierId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select an available courier to dispatch.");
            return "redirect:/deliveries/assign/" + targetDeliveryId;
        }

        try {
            deliveryService.assignCourier(targetDeliveryId, courierId);
            return "redirect:/deliveries";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/deliveries/assign/" + targetDeliveryId;
        }
    }

    @PostMapping("/deliveries/{id}/status")
    public String updateDeliveryStatus(@PathVariable Long id, @RequestParam String status) {
        deliveryService.updateDeliveryStatus(id, status);
        return "redirect:/deliveries";
    }

    @PostMapping("/deliveries/{id}/cancel")
    public String cancelDelivery(@PathVariable Long id) {
        deliveryService.updateDeliveryStatus(id, "CANCELLED");
        return "redirect:/deliveries";
    }

    @GetMapping({"/payments/{orderId}", "/payments/checkout/{orderId}"})
    public String showPaymentDetails(@PathVariable Long orderId,
                                     @RequestParam(value = "error", required = false) String error,
                                     Model model) {
        FoodOrder order = orderService.getOrderById(orderId);
        model.addAttribute("order", order);
        if (error != null && !model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", error);
        }
        try {
            Payment payment = deliveryService.getPaymentByOrderId(orderId);
            model.addAttribute("payment", payment);
        } catch (Exception e) {
            model.addAttribute("payment", null);
        }
        return "delivery/payment-details";
    }

    @PostMapping("/payments/{orderId}/cash")
    public String recordCashPayment(@PathVariable Long orderId,
                                    @RequestParam(required = false) BigDecimal amountTendered,
                                    RedirectAttributes redirectAttributes) {
        if (amountTendered == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please enter the cash amount tendered.");
            return "redirect:/payments/" + orderId;
        }
        FoodOrder order = orderService.getOrderById(orderId);
        if (amountTendered.compareTo(order.getTotalAmount()) < 0) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cash tendered (" + amountTendered + " LKR) must be at least the total amount due (" + order.getTotalAmount() + " LKR).");
            return "redirect:/payments/" + orderId;
        }
        try {
            orderService.attachCashPayment(orderId, amountTendered);
            return "redirect:/payments/" + orderId;
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/payments/" + orderId;
        }
    }

    @PostMapping("/payments/{orderId}/card")
    public String recordCardPayment(@PathVariable Long orderId,
                                    @RequestParam(required = false) String cardLastFour,
                                    @RequestParam(required = false) String cardType,
                                    @RequestParam(required = false) String authorizationCode,
                                    @RequestParam(required = false) String gatewayTransactionId,
                                    RedirectAttributes redirectAttributes) {
        String cleanLastFour = (cardLastFour != null) ? cardLastFour.trim() : "";
        if (!cleanLastFour.matches("^\\d{4}$")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Card last 4 digits must be exactly 4 numeric digits.");
            return "redirect:/payments/" + orderId;
        }
        if (cardType == null || cardType.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Card type is required.");
            return "redirect:/payments/" + orderId;
        }
        if (authorizationCode == null || authorizationCode.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Authorization code is required.");
            return "redirect:/payments/" + orderId;
        }
        if (gatewayTransactionId == null || gatewayTransactionId.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gateway transaction ID is required.");
            return "redirect:/payments/" + orderId;
        }

        try {
            deliveryService.processCardPayment(orderId, cleanLastFour, cardType.trim(), authorizationCode.trim(), gatewayTransactionId.trim());
            return "redirect:/payments/" + orderId;
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/payments/" + orderId;
        }
    }
}
