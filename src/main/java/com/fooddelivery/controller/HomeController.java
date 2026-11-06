package com.fooddelivery.controller;

import com.fooddelivery.model.order.FoodOrder;
import com.fooddelivery.service.CustomerService;
import com.fooddelivery.service.DeliveryService;
import com.fooddelivery.service.FoodService;
import com.fooddelivery.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;

@Controller
public class HomeController {

    private final FoodService foodService;
    private final CustomerService customerService;
    private final OrderService orderService;
    private final DeliveryService deliveryService;

    public HomeController(FoodService foodService,
                          CustomerService customerService,
                          OrderService orderService,
                          DeliveryService deliveryService) {
        this.foodService = foodService;
        this.customerService = customerService;
        this.orderService = orderService;
        this.deliveryService = deliveryService;
    }

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        if (session != null && "CUSTOMER".equalsIgnoreCase((String) session.getAttribute("userRole"))) {
            return "redirect:/orders/my-orders";
        }
        List<FoodOrder> orders = orderService.getAllOrders();
        BigDecimal grossRevenue = BigDecimal.ZERO;
        for (FoodOrder order : orders) {
            if (order.getTotalAmount() != null) {
                grossRevenue = grossRevenue.add(order.getTotalAmount());
            }
        }

        model.addAttribute("totalOrdersCount", orders.size());
        model.addAttribute("grossRevenue", grossRevenue);
        model.addAttribute("menuItemsCount", foodService.getAllActiveFoodItems().size());
        model.addAttribute("customersCount", customerService.getAllCustomers().size());
        model.addAttribute("availableCouriersCount", deliveryService.getAvailableCouriers().size());
        model.addAttribute("recentOrders", orders);
        return "index";
    }
}
