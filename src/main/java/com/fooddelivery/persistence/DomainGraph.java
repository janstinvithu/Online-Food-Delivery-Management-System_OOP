package com.fooddelivery.persistence;

import com.fooddelivery.model.customer.Customer;
import com.fooddelivery.model.delivery.Delivery;
import com.fooddelivery.model.delivery.DeliveryPerson;
import com.fooddelivery.model.delivery.Payment;
import com.fooddelivery.model.food.FoodItem;
import com.fooddelivery.model.order.FoodOrder;
import java.util.List;

public final class DomainGraph {

    private final List<Customer> customers;
    private final List<FoodItem> foodItems;
    private final List<FoodOrder> orders;
    private final List<Delivery> deliveries;
    private final List<DeliveryPerson> deliveryPersons;
    private final List<Payment> payments;

    public DomainGraph(List<Customer> customers,
                       List<FoodItem> foodItems,
                       List<FoodOrder> orders,
                       List<Delivery> deliveries,
                       List<DeliveryPerson> deliveryPersons,
                       List<Payment> payments) {
        this.customers = customers;
        this.foodItems = foodItems;
        this.orders = orders;
        this.deliveries = deliveries;
        this.deliveryPersons = deliveryPersons;
        this.payments = payments;
    }

    public List<Customer> getCustomers() { return customers; }
    public List<FoodItem> getFoodItems() { return foodItems; }
    public List<FoodOrder> getOrders() { return orders; }
    public List<Delivery> getDeliveries() { return deliveries; }
    public List<DeliveryPerson> getDeliveryPersons() { return deliveryPersons; }
    public List<Payment> getPayments() { return payments; }
}
