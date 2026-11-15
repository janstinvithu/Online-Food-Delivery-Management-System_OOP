package com.fooddelivery.controller;

import com.fooddelivery.model.customer.RegisteredCustomer;
import com.fooddelivery.model.delivery.CardPayment;
import com.fooddelivery.model.delivery.CashPayment;
import com.fooddelivery.model.delivery.Delivery;
import com.fooddelivery.model.delivery.DeliveryPerson;
import com.fooddelivery.model.delivery.Payment;
import com.fooddelivery.model.order.FoodOrder;
import com.fooddelivery.service.CustomerService;
import com.fooddelivery.service.DeliveryService;
import com.fooddelivery.service.OperationsService;
import com.fooddelivery.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final OrderService orderService;
    private final OperationsService operationsService;
    private final CustomerService customerService;

    public DeliveryController(DeliveryService deliveryService, OrderService orderService,
                              OperationsService operationsService, CustomerService customerService) {
        this.deliveryService = deliveryService;
        this.orderService = orderService;
        this.operationsService = operationsService;
        this.customerService = customerService;
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
        Payment payment = null;
        try {
            payment = deliveryService.getPaymentByOrderId(orderId);
        } catch (Exception ignored) {
            payment = null;
        }
        model.addAttribute("payment", payment);
        model.addAttribute("cashPayment", payment instanceof CashPayment);
        model.addAttribute("cardPayment", payment instanceof CardPayment);
        model.addAttribute("refunds", payment == null ? List.of() : operationsService.getRefunds(payment.getId()));
        boolean registered = order.getCustomer() instanceof RegisteredCustomer;
        model.addAttribute("registeredCustomer", registered);
        model.addAttribute("savedMethod", registered ? ((RegisteredCustomer) order.getCustomer()).getPreferredPaymentMethod() : null);
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

    @PostMapping("/payments/{orderId}/update")
    public String updatePayment(@PathVariable Long orderId,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) BigDecimal amountTendered,
                                @RequestParam(required = false) String authorizationCode,
                                RedirectAttributes redirectAttributes) {
        try {
            deliveryService.updatePayment(orderId, status, amountTendered, authorizationCode);
            redirectAttributes.addFlashAttribute("successMessage", "Payment updated.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/payments/" + orderId;
    }

    @PostMapping("/payments/{orderId}/delete")
    public String deletePayment(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        try {
            Payment payment = deliveryService.getPaymentByOrderId(orderId);
            if (operationsService.hasRefund(payment.getId())) {
                throw new IllegalStateException("This payment has a refund and cannot be deleted.");
            }
            deliveryService.deletePayment(orderId);
            redirectAttributes.addFlashAttribute("successMessage", "Payment removed. The order can be paid again.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/payments/" + orderId;
    }

    @PostMapping("/payments/{orderId}/refunds")
    public String saveRefund(@PathVariable Long orderId,
                             @RequestParam(required = false) Long refundId,
                             @RequestParam(required = false) BigDecimal amount,
                             @RequestParam(required = false) String reason,
                             @RequestParam(required = false) String status,
                             RedirectAttributes redirectAttributes) {
        try {
            if (refundId == null) {
                Payment payment = deliveryService.getPaymentByOrderId(orderId);
                operationsService.createRefund(payment.getId(), amount, reason);
                redirectAttributes.addFlashAttribute("successMessage", "Refund requested.");
            } else {
                operationsService.updateRefund(refundId, amount, reason, status);
                redirectAttributes.addFlashAttribute("successMessage", "Refund updated.");
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/payments/" + orderId;
    }

    @PostMapping("/payments/{orderId}/refunds/{refundId}/delete")
    public String deleteRefund(@PathVariable Long orderId, @PathVariable Long refundId,
                               RedirectAttributes redirectAttributes) {
        try {
            operationsService.deleteRefund(refundId);
            redirectAttributes.addFlashAttribute("successMessage", "Refund removed.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/payments/" + orderId;
    }

    @PostMapping("/payments/{orderId}/method")
    public String savePaymentMethod(@PathVariable Long orderId,
                                    @RequestParam(required = false) String method,
                                    RedirectAttributes redirectAttributes) {
        try {
            FoodOrder order = orderService.getOrderById(orderId);
            customerService.savePaymentMethod(order.getCustomer().getId(), method);
            redirectAttributes.addFlashAttribute("successMessage", "Saved payment method updated.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/payments/" + orderId;
    }

    @PostMapping("/payments/{orderId}/method/delete")
    public String clearPaymentMethod(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        try {
            FoodOrder order = orderService.getOrderById(orderId);
            customerService.clearPaymentMethod(order.getCustomer().getId());
            redirectAttributes.addFlashAttribute("successMessage", "Saved payment method removed.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/payments/" + orderId;
    }

    @GetMapping("/deliveries/couriers")
    public String listCouriers(Model model) {
        model.addAttribute("couriers", deliveryService.getAllCouriers());
        return "delivery/courier-list";
    }

    @GetMapping({"/deliveries/couriers/new", "/deliveries/couriers/edit/{id}"})
    public String showCourierForm(@PathVariable(required = false) Long id, Model model) {
        if (id != null) {
            DeliveryPerson courier = deliveryService.getCourierById(id);
            model.addAttribute("courier", courier);
            model.addAttribute("shifts", operationsService.getShifts(id));
        }
        return "delivery/courier-form";
    }

    @PostMapping({"/deliveries/couriers/new", "/deliveries/couriers/edit/{id}"})
    public String saveCourier(@PathVariable(required = false) Long id,
                              @RequestParam(required = false) String name,
                              @RequestParam(required = false) String phone,
                              @RequestParam(required = false) String vehicleType,
                              @RequestParam(required = false) String vehiclePlateNumber,
                              @RequestParam(required = false) BigDecimal rating,
                              RedirectAttributes redirectAttributes) {
        try {
            if (id == null) {
                deliveryService.createCourier(name, phone, vehicleType, vehiclePlateNumber, rating);
                redirectAttributes.addFlashAttribute("successMessage", "Courier added to the fleet.");
            } else {
                deliveryService.updateCourier(id, name, phone, vehicleType, vehiclePlateNumber, rating);
                redirectAttributes.addFlashAttribute("successMessage", "Courier details updated.");
            }
            return "redirect:/deliveries/couriers";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return id == null ? "redirect:/deliveries/couriers/new" : "redirect:/deliveries/couriers/edit/" + id;
        }
    }

    @PostMapping("/deliveries/couriers/delete/{id}")
    public String deleteCourier(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            DeliveryPerson courier = deliveryService.getCourierById(id);
            deliveryService.deleteCourier(id);
            redirectAttributes.addFlashAttribute("successMessage", courier.getName() + " was removed from the fleet.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/deliveries/couriers";
    }

    @GetMapping("/deliveries/zones")
    public String listZones(@RequestParam(required = false) Long edit, Model model) {
        model.addAttribute("zones", operationsService.getZones());
        if (edit != null) {
            model.addAttribute("zone", operationsService.getZone(edit));
        }
        return "delivery/zones";
    }

    @PostMapping("/deliveries/zones")
    public String saveZone(@RequestParam(required = false) Long id,
                           @RequestParam(required = false) String areaName,
                           @RequestParam(required = false) BigDecimal fee,
                           RedirectAttributes redirectAttributes) {
        try {
            if (id == null) {
                operationsService.createZone(areaName, fee);
                redirectAttributes.addFlashAttribute("successMessage", "Delivery zone added.");
            } else {
                operationsService.updateZone(id, areaName, fee);
                redirectAttributes.addFlashAttribute("successMessage", "Delivery zone updated.");
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/deliveries/zones";
    }

    @PostMapping("/deliveries/zones/{id}/delete")
    public String deleteZone(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            operationsService.deleteZone(id);
            redirectAttributes.addFlashAttribute("successMessage", "Delivery zone removed.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/deliveries/zones";
    }

    @PostMapping("/deliveries/couriers/{id}/shifts")
    public String saveShift(@PathVariable Long id,
                            @RequestParam(required = false) Long shiftId,
                            @RequestParam(required = false) LocalDate shiftDate,
                            @RequestParam(required = false) LocalTime startTime,
                            @RequestParam(required = false) LocalTime endTime,
                            RedirectAttributes redirectAttributes) {
        try {
            if (shiftId == null) {
                operationsService.createShift(id, shiftDate, startTime, endTime);
                redirectAttributes.addFlashAttribute("successMessage", "Shift added.");
            } else {
                operationsService.updateShift(shiftId, shiftDate, startTime, endTime);
                redirectAttributes.addFlashAttribute("successMessage", "Shift updated.");
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/deliveries/couriers/edit/" + id;
    }

    @PostMapping("/deliveries/couriers/{id}/shifts/{shiftId}/delete")
    public String deleteShift(@PathVariable Long id, @PathVariable Long shiftId, RedirectAttributes redirectAttributes) {
        try {
            operationsService.deleteShift(shiftId);
            redirectAttributes.addFlashAttribute("successMessage", "Shift removed.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/deliveries/couriers/edit/" + id;
    }

    @GetMapping("/deliveries/{id}/checkpoints")
    public String showCheckpoints(@PathVariable Long id, Model model) {
        model.addAttribute("delivery", deliveryService.getDeliveryById(id));
        model.addAttribute("checkpoints", operationsService.getCheckpoints(id));
        return "delivery/checkpoints";
    }

    @PostMapping("/deliveries/{id}/checkpoints")
    public String saveCheckpoint(@PathVariable Long id,
                                 @RequestParam(required = false) Long checkpointId,
                                 @RequestParam(required = false) String step,
                                 @RequestParam(required = false) String note,
                                 @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
                                 @RequestParam(required = false) LocalDateTime recordedAt,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (checkpointId == null) {
                operationsService.createCheckpoint(id, step, note);
                redirectAttributes.addFlashAttribute("successMessage", "Checkpoint added.");
            } else {
                operationsService.updateCheckpoint(checkpointId, step, note, recordedAt);
                redirectAttributes.addFlashAttribute("successMessage", "Checkpoint updated.");
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/deliveries/" + id + "/checkpoints";
    }

    @PostMapping("/deliveries/{id}/checkpoints/{checkpointId}/delete")
    public String deleteCheckpoint(@PathVariable Long id, @PathVariable Long checkpointId,
                                   RedirectAttributes redirectAttributes) {
        try {
            operationsService.deleteCheckpoint(checkpointId);
            redirectAttributes.addFlashAttribute("successMessage", "Checkpoint removed.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/deliveries/" + id + "/checkpoints";
    }
}
