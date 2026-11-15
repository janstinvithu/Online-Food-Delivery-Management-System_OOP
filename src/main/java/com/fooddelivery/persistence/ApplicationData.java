package com.fooddelivery.persistence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fooddelivery.persistence.dto.CourierShiftData;
import com.fooddelivery.persistence.dto.CustomerData;
import com.fooddelivery.persistence.dto.DeliveryCheckpointData;
import com.fooddelivery.persistence.dto.DeliveryData;
import com.fooddelivery.persistence.dto.DeliveryPersonData;
import com.fooddelivery.persistence.dto.DeliveryZoneData;
import com.fooddelivery.persistence.dto.FoodItemData;
import com.fooddelivery.persistence.dto.FoodOrderData;
import com.fooddelivery.persistence.dto.OrderComplaintData;
import com.fooddelivery.persistence.dto.OrderItemData;
import com.fooddelivery.persistence.dto.OrderReviewData;
import com.fooddelivery.persistence.dto.PaymentData;
import com.fooddelivery.persistence.dto.RefundData;
import com.fooddelivery.persistence.dto.SavedAddressData;
import com.fooddelivery.persistence.dto.SequencesData;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationData {

    private int schemaVersion = 1;
    private SequencesData sequences = new SequencesData();
    private List<CustomerData> customers = new ArrayList<>();
    private List<FoodItemData> foodItems = new ArrayList<>();
    private List<FoodOrderData> orders = new ArrayList<>();
    private List<DeliveryData> deliveries = new ArrayList<>();
    private List<DeliveryPersonData> deliveryPersons = new ArrayList<>();
    private List<PaymentData> payments = new ArrayList<>();
    private List<FoodOrderData> archivedOrders = new ArrayList<>();
    private List<OrderComplaintData> complaints = new ArrayList<>();
    private List<OrderReviewData> reviews = new ArrayList<>();
    private List<SavedAddressData> savedAddresses = new ArrayList<>();
    private List<DeliveryZoneData> deliveryZones = new ArrayList<>();
    private List<CourierShiftData> courierShifts = new ArrayList<>();
    private List<DeliveryCheckpointData> deliveryCheckpoints = new ArrayList<>();
    private List<RefundData> refunds = new ArrayList<>();

    public static ApplicationData empty() {
        ApplicationData data = new ApplicationData();
        data.ensureCollections();
        return data;
    }

    public void ensureCollections() {
        if (sequences == null) {
            sequences = new SequencesData();
        }
        if (customers == null) {
            customers = new ArrayList<>();
        }
        if (foodItems == null) {
            foodItems = new ArrayList<>();
        }
        if (orders == null) {
            orders = new ArrayList<>();
        }
        if (deliveries == null) {
            deliveries = new ArrayList<>();
        }
        if (deliveryPersons == null) {
            deliveryPersons = new ArrayList<>();
        }
        if (payments == null) {
            payments = new ArrayList<>();
        }
        if (archivedOrders == null) {
            archivedOrders = new ArrayList<>();
        }
        if (complaints == null) {
            complaints = new ArrayList<>();
        }
        if (reviews == null) {
            reviews = new ArrayList<>();
        }
        if (savedAddresses == null) {
            savedAddresses = new ArrayList<>();
        }
        if (deliveryZones == null) {
            deliveryZones = new ArrayList<>();
        }
        if (courierShifts == null) {
            courierShifts = new ArrayList<>();
        }
        if (deliveryCheckpoints == null) {
            deliveryCheckpoints = new ArrayList<>();
        }
        if (refunds == null) {
            refunds = new ArrayList<>();
        }
        for (FoodOrderData order : orders) {
            if (order.getItems() == null) {
                order.setItems(new ArrayList<>());
            }
        }
        for (FoodOrderData order : archivedOrders) {
            if (order.getItems() == null) {
                order.setItems(new ArrayList<>());
            }
        }
    }

    public boolean hasRecords() {
        ensureCollections();
        return !customers.isEmpty()
                || !foodItems.isEmpty()
                || !orders.isEmpty()
                || !deliveries.isEmpty()
                || !deliveryPersons.isEmpty()
                || !payments.isEmpty()
                || !archivedOrders.isEmpty();
    }

    public void alignSequences() {
        ensureCollections();
        sequences.setCustomerId(Math.max(sequences.getCustomerId(), maxId(customers, CustomerData::getId)));
        sequences.setFoodItemId(Math.max(sequences.getFoodItemId(), maxId(foodItems, FoodItemData::getId)));
        sequences.setOrderId(Math.max(sequences.getOrderId(), Math.max(
                maxId(orders, FoodOrderData::getId),
                maxId(archivedOrders, FoodOrderData::getId))));
        sequences.setDeliveryId(Math.max(sequences.getDeliveryId(), maxId(deliveries, DeliveryData::getId)));
        sequences.setDeliveryPersonId(Math.max(sequences.getDeliveryPersonId(), maxId(deliveryPersons, DeliveryPersonData::getId)));
        sequences.setPaymentId(Math.max(sequences.getPaymentId(), maxId(payments, PaymentData::getId)));
        sequences.setComplaintId(Math.max(sequences.getComplaintId(), maxId(complaints, OrderComplaintData::getId)));
        sequences.setReviewId(Math.max(sequences.getReviewId(), maxId(reviews, OrderReviewData::getId)));
        sequences.setSavedAddressId(Math.max(sequences.getSavedAddressId(), maxId(savedAddresses, SavedAddressData::getId)));
        sequences.setDeliveryZoneId(Math.max(sequences.getDeliveryZoneId(), maxId(deliveryZones, DeliveryZoneData::getId)));
        sequences.setCourierShiftId(Math.max(sequences.getCourierShiftId(), maxId(courierShifts, CourierShiftData::getId)));
        sequences.setCheckpointId(Math.max(sequences.getCheckpointId(), maxId(deliveryCheckpoints, DeliveryCheckpointData::getId)));
        sequences.setRefundId(Math.max(sequences.getRefundId(), maxId(refunds, RefundData::getId)));
        long itemMax = 0;
        for (FoodOrderData order : orders) {
            itemMax = Math.max(itemMax, maxId(order.getItems(), OrderItemData::getId));
        }
        for (FoodOrderData order : archivedOrders) {
            itemMax = Math.max(itemMax, maxId(order.getItems(), OrderItemData::getId));
        }
        sequences.setOrderItemId(Math.max(sequences.getOrderItemId(), itemMax));
    }

    private static <T> long maxId(List<T> items, java.util.function.Function<T, Long> idFn) {
        long max = 0;
        if (items == null) {
            return max;
        }
        for (T item : items) {
            Long id = idFn.apply(item);
            if (id != null && id > max) {
                max = id;
            }
        }
        return max;
    }

    public int getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(int schemaVersion) { this.schemaVersion = schemaVersion; }
    public SequencesData getSequences() { return sequences; }
    public void setSequences(SequencesData sequences) { this.sequences = sequences; }
    public List<CustomerData> getCustomers() { return customers; }
    public void setCustomers(List<CustomerData> customers) { this.customers = customers; }
    public List<FoodItemData> getFoodItems() { return foodItems; }
    public void setFoodItems(List<FoodItemData> foodItems) { this.foodItems = foodItems; }
    public List<FoodOrderData> getOrders() { return orders; }
    public void setOrders(List<FoodOrderData> orders) { this.orders = orders; }
    public List<DeliveryData> getDeliveries() { return deliveries; }
    public void setDeliveries(List<DeliveryData> deliveries) { this.deliveries = deliveries; }
    public List<DeliveryPersonData> getDeliveryPersons() { return deliveryPersons; }
    public void setDeliveryPersons(List<DeliveryPersonData> deliveryPersons) { this.deliveryPersons = deliveryPersons; }
    public List<PaymentData> getPayments() { return payments; }
    public void setPayments(List<PaymentData> payments) { this.payments = payments; }
    public List<FoodOrderData> getArchivedOrders() { return archivedOrders; }
    public void setArchivedOrders(List<FoodOrderData> archivedOrders) { this.archivedOrders = archivedOrders; }
    public List<OrderComplaintData> getComplaints() { return complaints; }
    public void setComplaints(List<OrderComplaintData> complaints) { this.complaints = complaints; }
    public List<OrderReviewData> getReviews() { return reviews; }
    public void setReviews(List<OrderReviewData> reviews) { this.reviews = reviews; }
    public List<SavedAddressData> getSavedAddresses() { return savedAddresses; }
    public void setSavedAddresses(List<SavedAddressData> savedAddresses) { this.savedAddresses = savedAddresses; }
    public List<DeliveryZoneData> getDeliveryZones() { return deliveryZones; }
    public void setDeliveryZones(List<DeliveryZoneData> deliveryZones) { this.deliveryZones = deliveryZones; }
    public List<CourierShiftData> getCourierShifts() { return courierShifts; }
    public void setCourierShifts(List<CourierShiftData> courierShifts) { this.courierShifts = courierShifts; }
    public List<DeliveryCheckpointData> getDeliveryCheckpoints() { return deliveryCheckpoints; }
    public void setDeliveryCheckpoints(List<DeliveryCheckpointData> deliveryCheckpoints) { this.deliveryCheckpoints = deliveryCheckpoints; }
    public List<RefundData> getRefunds() { return refunds; }
    public void setRefunds(List<RefundData> refunds) { this.refunds = refunds; }
}
