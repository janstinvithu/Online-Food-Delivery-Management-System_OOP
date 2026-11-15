package com.fooddelivery.persistence;

import com.fooddelivery.model.customer.Customer;
import com.fooddelivery.model.customer.RegisteredCustomer;
import com.fooddelivery.model.delivery.CardPayment;
import com.fooddelivery.model.delivery.CashPayment;
import com.fooddelivery.model.delivery.Delivery;
import com.fooddelivery.model.delivery.DeliveryPerson;
import com.fooddelivery.model.delivery.Payment;
import com.fooddelivery.model.food.Beverage;
import com.fooddelivery.model.food.FoodItem;
import com.fooddelivery.model.food.MainCourse;
import com.fooddelivery.model.order.FoodOrder;
import com.fooddelivery.model.order.OnlineOrder;
import com.fooddelivery.model.order.OrderItem;
import com.fooddelivery.model.order.PickupOrder;
import com.fooddelivery.persistence.dto.CustomerData;
import com.fooddelivery.persistence.dto.DeliveryData;
import com.fooddelivery.persistence.dto.DeliveryPersonData;
import com.fooddelivery.persistence.dto.FoodItemData;
import com.fooddelivery.persistence.dto.FoodOrderData;
import com.fooddelivery.persistence.dto.OrderItemData;
import com.fooddelivery.persistence.dto.PaymentData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DomainMapper {

    public DomainGraph toGraph(ApplicationData data) {
        data.ensureCollections();
        Map<Long, Customer> customers = new LinkedHashMap<>();
        for (CustomerData source : data.getCustomers()) {
            Customer customer = toCustomer(source);
            customers.put(customer.getId(), customer);
        }
        Map<Long, FoodItem> foods = new LinkedHashMap<>();
        for (FoodItemData source : data.getFoodItems()) {
            FoodItem food = toFoodItem(source);
            foods.put(food.getId(), food);
        }
        Map<Long, DeliveryPerson> people = new LinkedHashMap<>();
        for (DeliveryPersonData source : data.getDeliveryPersons()) {
            DeliveryPerson person = toDeliveryPerson(source);
            people.put(person.getId(), person);
        }
        Map<Long, Delivery> deliveries = new LinkedHashMap<>();
        for (DeliveryData source : data.getDeliveries()) {
            Delivery delivery = toDelivery(source, people);
            deliveries.put(delivery.getId(), delivery);
        }
        Map<Long, FoodOrder> orders = new LinkedHashMap<>();
        for (FoodOrderData source : data.getOrders()) {
            FoodOrder order = toOrder(source, customers, foods, deliveries);
            orders.put(order.getId(), order);
        }
        for (DeliveryData source : data.getDeliveries()) {
            if (source.getOnlineOrderId() == null) {
                continue;
            }
            Delivery delivery = deliveries.get(source.getId());
            FoodOrder order = orders.get(source.getOnlineOrderId());
            if (delivery != null && order instanceof OnlineOrder online) {
                delivery.setOnlineOrder(online);
                if (delivery.getDeliveryPerson() != null) {
                    delivery.getDeliveryPerson().restoreDelivery(delivery);
                }
            }
        }
        List<Payment> payments = new ArrayList<>();
        for (PaymentData source : data.getPayments()) {
            FoodOrder order = orders.get(source.getOrderId());
            if (order == null) {
                throw new DataStoreException("Payment " + source.getId() + " references missing order " + source.getOrderId());
            }
            payments.add(toPayment(source, order));
        }
        return new DomainGraph(
                List.copyOf(customers.values()),
                List.copyOf(foods.values()),
                List.copyOf(orders.values()),
                List.copyOf(deliveries.values()),
                List.copyOf(people.values()),
                List.copyOf(payments));
    }

    public Customer toCustomer(CustomerData data) {
        Customer customer = "REGISTERED".equalsIgnoreCase(data.getType())
                ? new RegisteredCustomer()
                : new Customer();
        customer.assignPersistentId(data.getId());
        customer.setFullName(data.getFullName());
        customer.setEmail(data.getEmail());
        customer.setPhone(data.getPhone());
        customer.setAddress(data.getAddress());
        customer.restoreCreatedAt(data.getCreatedAt());
        if (customer instanceof RegisteredCustomer registered) {
            int points = data.getLoyaltyPoints() == null ? 0 : data.getLoyaltyPoints();
            registered.setLoyaltyPoints(points);
            if (data.getMembershipTier() != null) {
                registered.setMembershipTier(data.getMembershipTier());
            }
            registered.setPasswordHash(data.getPasswordHash());
            registered.setPreferredPaymentMethod(data.getPreferredPaymentMethod());
        }
        return customer;
    }

    public CustomerData fromCustomer(Customer customer) {
        CustomerData data = new CustomerData();
        data.setId(customer.getId());
        data.setType(customer.isRegisteredCustomer() ? "REGISTERED" : "GUEST");
        data.setFullName(customer.getFullName());
        data.setEmail(customer.getEmail());
        data.setPhone(customer.getPhone());
        data.setAddress(customer.getAddress());
        data.setCreatedAt(customer.getCreatedAt());
        if (customer instanceof RegisteredCustomer registered) {
            data.setLoyaltyPoints(registered.getLoyaltyPoints());
            data.setMembershipTier(registered.getMembershipTier());
            data.setPasswordHash(registered.getPasswordHash());
            data.setPreferredPaymentMethod(registered.getPreferredPaymentMethod());
        }
        return data;
    }

    public FoodItem toFoodItem(FoodItemData data) {
        if ("BEVERAGE".equalsIgnoreCase(data.getType())) {
            Beverage beverage = new Beverage();
            applyFoodBase(beverage, data);
            beverage.setVolumeSize(data.getVolumeSize());
            beverage.setVolumeMl(data.getVolumeMl() == null ? 1 : data.getVolumeMl());
            beverage.setCold(Boolean.TRUE.equals(data.getCold()));
            beverage.setCaffeineContentMg(data.getCaffeineContentMg() == null ? 0 : data.getCaffeineContentMg());
            beverage.setSugarLevel(data.getSugarLevel());
            return beverage;
        }
        MainCourse course = new MainCourse();
        applyFoodBase(course, data);
        course.setCuisineType(data.getCuisineType());
        course.setVegetarian(Boolean.TRUE.equals(data.getVegetarian()));
        course.setSpiceLevel(data.getSpiceLevel());
        course.setServingPortion(data.getServingPortion());
        return course;
    }

    private void applyFoodBase(FoodItem item, FoodItemData data) {
        item.assignPersistentId(data.getId());
        item.setName(data.getName());
        item.setDescription(data.getDescription());
        item.setPrice(data.getPrice());
        item.setPreparationTimeMinutes(data.getPreparationTimeMinutes());
        item.setAvailable(data.isAvailable());
    }

    public FoodItemData fromFoodItem(FoodItem item) {
        FoodItemData data = new FoodItemData();
        data.setId(item.getId());
        data.setName(item.getName());
        data.setDescription(item.getDescription());
        data.setPrice(item.getPrice());
        data.setAvailable(item.isAvailable());
        data.setPreparationTimeMinutes(item.getPreparationTimeMinutes());
        if (item instanceof Beverage beverage) {
            data.setType("BEVERAGE");
            data.setVolumeSize(beverage.getVolumeSize());
            data.setVolumeMl(beverage.getVolumeMl());
            data.setCold(beverage.isCold());
            data.setCaffeineContentMg(beverage.getCaffeineContentMg());
            data.setSugarLevel(beverage.getSugarLevel());
        } else if (item instanceof MainCourse course) {
            data.setType("MAIN_COURSE");
            data.setCuisineType(course.getCuisineType());
            data.setVegetarian(course.isVegetarian());
            data.setSpiceLevel(course.getSpiceLevel());
            data.setServingPortion(course.getServingPortion());
        } else {
            throw new DataStoreException("Unsupported food item type: " + item.getClass().getName());
        }
        return data;
    }

    public DeliveryPerson toDeliveryPerson(DeliveryPersonData data) {
        DeliveryPerson person = new DeliveryPerson(data.getName(), data.getPhone(), data.getVehicleType(), data.getVehiclePlateNumber());
        person.assignPersistentId(data.getId());
        person.setAvailable(data.isAvailable());
        if (data.getRating() != null) {
            person.setRating(data.getRating());
        }
        return person;
    }

    public DeliveryPersonData fromDeliveryPerson(DeliveryPerson person) {
        DeliveryPersonData data = new DeliveryPersonData();
        data.setId(person.getId());
        data.setName(person.getName());
        data.setPhone(person.getPhone());
        data.setVehicleType(person.getVehicleType());
        data.setVehiclePlateNumber(person.getVehiclePlateNumber());
        data.setAvailable(person.isAvailable());
        data.setRating(person.getRating());
        return data;
    }

    public Delivery toDelivery(DeliveryData data, Map<Long, DeliveryPerson> people) {
        Delivery delivery = new Delivery();
        delivery.assignPersistentId(data.getId());
        delivery.setTrackingNumber(data.getTrackingNumber());
        DeliveryPerson person = null;
        if (data.getDeliveryPersonId() != null) {
            person = people.get(data.getDeliveryPersonId());
            if (person == null) {
                throw new DataStoreException("Delivery " + data.getId() + " references missing courier " + data.getDeliveryPersonId());
            }
        }
        delivery.restorePersistedState(data.getDeliveryStatus(), data.getDepartureTime(), data.getDeliveredTime(), person);
        return delivery;
    }

    public DeliveryData fromDelivery(Delivery delivery) {
        DeliveryData data = new DeliveryData();
        data.setId(delivery.getId());
        data.setTrackingNumber(delivery.getTrackingNumber());
        data.setDeliveryStatus(delivery.getDeliveryStatus());
        data.setDepartureTime(delivery.getDepartureTime());
        data.setDeliveredTime(delivery.getDeliveredTime());
        if (delivery.getDeliveryPerson() != null) {
            data.setDeliveryPersonId(delivery.getDeliveryPerson().getId());
        }
        if (delivery.getOnlineOrder() != null) {
            data.setOnlineOrderId(delivery.getOnlineOrder().getId());
        }
        return data;
    }

    public FoodOrder toOrder(FoodOrderData data,
                             Map<Long, Customer> customers,
                             Map<Long, FoodItem> foods,
                             Map<Long, Delivery> deliveries) {
        Customer customer = customers.get(data.getCustomerId());
        if (customer == null) {
            throw new DataStoreException("Order " + data.getId() + " references missing customer " + data.getCustomerId());
        }
        FoodOrder order;
        if ("PICKUP".equalsIgnoreCase(data.getType())) {
            PickupOrder pickup = new PickupOrder();
            pickup.setCustomer(customer);
            pickup.assignPersistentId(data.getId());
            restoreItems(pickup, data, foods);
            pickup.setPickupTimeSlot(data.getPickupTimeSlot());
            pickup.setPickupWindowMinutes(data.getPickupWindowMinutes() == null ? 1 : data.getPickupWindowMinutes());
            pickup.setCounterNumber(data.getCounterNumber() == null ? 1 : data.getCounterNumber());
            pickup.setPickupContactPhone(data.getPickupContactPhone());
            pickup.setCurbsidePickup(Boolean.TRUE.equals(data.getCurbsidePickup()));
            order = pickup;
        } else {
            OnlineOrder online = new OnlineOrder();
            online.setCustomer(customer);
            online.assignPersistentId(data.getId());
            restoreItems(online, data, foods);
            online.setDeliveryAddress(data.getDeliveryAddress());
            online.setSpecialDeliveryInstructions(data.getSpecialDeliveryInstructions());
            online.setEstimatedDeliveryTime(data.getEstimatedDeliveryTime());
            if (data.getDeliveryFee() != null) {
                online.setDeliveryFee(data.getDeliveryFee());
            }
            online.setDeliveryZoneId(data.getDeliveryZoneId());
            if (data.getDeliveryId() != null) {
                Delivery delivery = deliveries.get(data.getDeliveryId());
                if (delivery == null) {
                    throw new DataStoreException("Online order " + data.getId() + " references missing delivery " + data.getDeliveryId());
                }
                online.setDelivery(delivery);
            }
            order = online;
        }
        order.setOrderNumber(data.getOrderNumber());
        order.setOrderDateTime(data.getOrderDateTime());
        order.setSubtotalAmount(data.getSubtotalAmount());
        order.setDiscountAmount(data.getDiscountAmount());
        order.setTotalAmount(data.getTotalAmount());
        order.setOrderStatus(data.getOrderStatus());
        customer.addOrder(order);
        return order;
    }

    private void restoreItems(FoodOrder order, FoodOrderData data, Map<Long, FoodItem> foods) {
        if (data.getItems() == null) {
            return;
        }
        for (OrderItemData source : data.getItems()) {
            FoodItem food = foods.get(source.getFoodItemId());
            if (food == null) {
                throw new DataStoreException("Order item " + source.getId() + " references missing food item " + source.getFoodItemId());
            }
            OrderItem item = new OrderItem(order, food, source.getQuantity());
            item.assignPersistentId(source.getId());
            item.setUnitPrice(source.getUnitPrice());
            order.restoreOrderItem(item);
        }
    }

    public FoodOrderData fromOrder(FoodOrder order) {
        FoodOrderData data = new FoodOrderData();
        data.setId(order.getId());
        data.setOrderNumber(order.getOrderNumber());
        data.setOrderStatus(order.getOrderStatus());
        data.setOrderDateTime(order.getOrderDateTime());
        data.setSubtotalAmount(order.getSubtotalAmount());
        data.setDiscountAmount(order.getDiscountAmount());
        data.setTotalAmount(order.getTotalAmount());
        data.setCustomerId(order.getCustomer().getId());
        List<OrderItemData> items = new ArrayList<>();
        for (OrderItem item : order.getOrderItems()) {
            OrderItemData line = new OrderItemData();
            line.setId(item.getId());
            line.setFoodItemId(item.getFoodItem().getId());
            line.setQuantity(item.getQuantity());
            line.setUnitPrice(item.getUnitPrice());
            line.setSubtotal(item.getSubtotal());
            items.add(line);
        }
        data.setItems(items);
        if (order instanceof OnlineOrder online) {
            data.setType("ONLINE");
            data.setDeliveryAddress(online.getDeliveryAddress());
            data.setSpecialDeliveryInstructions(online.getSpecialDeliveryInstructions());
            data.setEstimatedDeliveryTime(online.getEstimatedDeliveryTime());
            data.setDeliveryFee(online.getDeliveryFee());
            data.setDeliveryZoneId(online.getDeliveryZoneId());
            if (online.getDelivery() != null) {
                data.setDeliveryId(online.getDelivery().getId());
            }
        } else if (order instanceof PickupOrder pickup) {
            data.setType("PICKUP");
            data.setPickupTimeSlot(pickup.getPickupTimeSlot());
            data.setPickupWindowMinutes(pickup.getPickupWindowMinutes());
            data.setCounterNumber(pickup.getCounterNumber());
            data.setPickupContactPhone(pickup.getPickupContactPhone());
            data.setCurbsidePickup(pickup.isCurbsidePickup());
        } else {
            throw new DataStoreException("Unsupported order type: " + order.getClass().getName());
        }
        return data;
    }

    public Payment toPayment(PaymentData data, FoodOrder order) {
        Payment payment;
        if ("CASH".equalsIgnoreCase(data.getType())) {
            CashPayment cash = new CashPayment();
            cash.assignPersistentId(data.getId());
            cash.setAmount(data.getAmount());
            cash.setAmountTendered(data.getAmountTendered());
            if (data.getChangeDue() != null) {
                cash.setChangeDue(data.getChangeDue());
            }
            payment = cash;
        } else {
            CardPayment card = new CardPayment();
            card.assignPersistentId(data.getId());
            card.setAmount(data.getAmount());
            card.setCardLastFour(data.getCardLastFour());
            card.setCardType(data.getCardType());
            card.setAuthorizationCode(data.getAuthorizationCode());
            card.setGatewayTransactionId(data.getGatewayTransactionId());
            payment = card;
        }
        payment.setPaymentStatus(data.getPaymentStatus());
        payment.setPaymentTimestamp(data.getPaymentTimestamp());
        payment.setTransactionReference(data.getTransactionReference());
        payment.setOrder(order);
        return payment;
    }

    public PaymentData fromPayment(Payment payment) {
        PaymentData data = new PaymentData();
        data.setId(payment.getId());
        data.setOrderId(payment.getOrder().getId());
        data.setAmount(payment.getAmount());
        data.setPaymentStatus(payment.getPaymentStatus());
        data.setPaymentTimestamp(payment.getPaymentTimestamp());
        data.setTransactionReference(payment.getTransactionReference());
        if (payment instanceof CashPayment cash) {
            data.setType("CASH");
            data.setAmountTendered(cash.getAmountTendered());
            data.setChangeDue(cash.getChangeDue());
        } else if (payment instanceof CardPayment card) {
            data.setType("CARD");
            data.setCardLastFour(card.getCardLastFour());
            data.setCardType(card.getCardType());
            data.setAuthorizationCode(card.getAuthorizationCode());
            data.setGatewayTransactionId(card.getGatewayTransactionId());
        } else {
            throw new DataStoreException("Unsupported payment type: " + payment.getClass().getName());
        }
        return data;
    }
}
