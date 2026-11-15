package com.fooddelivery.persistence;

import com.fooddelivery.model.customer.Customer;
import com.fooddelivery.model.delivery.Delivery;
import com.fooddelivery.model.delivery.DeliveryPerson;
import com.fooddelivery.model.delivery.Payment;
import com.fooddelivery.model.food.FoodItem;
import com.fooddelivery.model.order.FoodOrder;
import com.fooddelivery.model.order.OnlineOrder;
import com.fooddelivery.model.order.OrderItem;
import com.fooddelivery.persistence.dto.CustomerData;
import com.fooddelivery.persistence.dto.DeliveryData;
import com.fooddelivery.persistence.dto.DeliveryPersonData;
import com.fooddelivery.persistence.dto.FoodItemData;
import com.fooddelivery.persistence.dto.FoodOrderData;
import com.fooddelivery.persistence.dto.PaymentData;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@Component
public class AggregateWriter {

    private final DomainMapper mapper;

    public AggregateWriter(DomainMapper mapper) {
        this.mapper = mapper;
    }

    public void writeCustomer(ApplicationData data, Customer customer) {
        if (customer.getEmail() == null) {
            throw new IllegalArgumentException("Customer email is required");
        }
        assertUniqueEmail(data, customer);
        if (customer.getId() == null) {
            customer.assignPersistentId(data.getSequences().nextCustomerId());
        }
        upsert(data.getCustomers(), mapper.fromCustomer(customer), CustomerData::getId);
    }

    public void writeFoodItem(ApplicationData data, FoodItem item) {
        if (item.getId() == null) {
            item.assignPersistentId(data.getSequences().nextFoodItemId());
        }
        upsert(data.getFoodItems(), mapper.fromFoodItem(item), FoodItemData::getId);
    }

    public void writeDeliveryPerson(ApplicationData data, DeliveryPerson person) {
        assertUniquePhone(data, person);
        if (person.getId() == null) {
            person.assignPersistentId(data.getSequences().nextDeliveryPersonId());
        }
        upsert(data.getDeliveryPersons(), mapper.fromDeliveryPerson(person), DeliveryPersonData::getId);
    }

    public void writeOrder(ApplicationData data, FoodOrder order, boolean includePayment) {
        if (order.getCustomer() == null || order.getCustomer().getId() == null) {
            throw new IllegalArgumentException("Order requires a saved customer");
        }
        if (order.getId() == null) {
            order.assignPersistentId(data.getSequences().nextOrderId());
        }
        for (OrderItem item : order.getOrderItems()) {
            if (item.getFoodItem() == null || item.getFoodItem().getId() == null) {
                throw new IllegalArgumentException("Order item requires a saved food item");
            }
            if (item.getId() == null) {
                item.assignPersistentId(data.getSequences().nextOrderItemId());
            }
        }
        if (order instanceof OnlineOrder online && online.getDelivery() != null) {
            Delivery delivery = online.getDelivery();
            if (delivery.getOnlineOrder() == null) {
                delivery.setOnlineOrder(online);
            }
            writeDelivery(data, delivery, true);
        }
        upsert(data.getOrders(), mapper.fromOrder(order), FoodOrderData::getId);
        if (includePayment && order.getPayment() != null) {
            writePayment(data, order.getPayment(), false);
        }
    }

    public void writeDelivery(ApplicationData data, Delivery delivery, boolean writePerson) {
        if (delivery.getId() == null) {
            delivery.assignPersistentId(data.getSequences().nextDeliveryId());
        }
        if (writePerson && delivery.getDeliveryPerson() != null) {
            writeDeliveryPerson(data, delivery.getDeliveryPerson());
        }
        upsert(data.getDeliveries(), mapper.fromDelivery(delivery), DeliveryData::getId);
    }

    public void writePayment(ApplicationData data, Payment payment, boolean updateOrder) {
        if (payment.getOrder() == null || payment.getOrder().getId() == null) {
            throw new IllegalArgumentException("Payment requires a saved order");
        }
        if (payment.getId() == null) {
            payment.assignPersistentId(data.getSequences().nextPaymentId());
        }
        upsert(data.getPayments(), mapper.fromPayment(payment), PaymentData::getId);
        if (updateOrder) {
            writeOrder(data, payment.getOrder(), false);
        }
    }

    private void assertUniqueEmail(ApplicationData data, Customer customer) {
        for (CustomerData existing : data.getCustomers()) {
            if (existing.getEmail() != null
                    && existing.getEmail().equalsIgnoreCase(customer.getEmail())
                    && (customer.getId() == null || !existing.getId().equals(customer.getId()))) {
                throw new IllegalArgumentException("Email is already registered: " + customer.getEmail());
            }
        }
    }

    private void assertUniquePhone(ApplicationData data, DeliveryPerson person) {
        for (DeliveryPersonData existing : data.getDeliveryPersons()) {
            if (existing.getPhone() != null
                    && existing.getPhone().equals(person.getPhone())
                    && (person.getId() == null || !existing.getId().equals(person.getId()))) {
                throw new IllegalArgumentException("Courier phone is already registered: " + person.getPhone());
            }
        }
    }

    private static <T> void upsert(List<T> list, T item, Function<T, Long> idFn) {
        Long id = idFn.apply(item);
        for (int i = 0; i < list.size(); i++) {
            if (id.equals(idFn.apply(list.get(i)))) {
                list.set(i, item);
                return;
            }
        }
        list.add(item);
    }
}
