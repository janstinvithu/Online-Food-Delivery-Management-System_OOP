package com.fooddelivery.persistence;

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
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class DataValidator {

    public void validate(ApplicationData data) {
        data.ensureCollections();
        if (data.getSchemaVersion() < 1) {
            throw new DataStoreException("Unsupported JSON schema version: " + data.getSchemaVersion());
        }
        requireUniqueIds("customer", data.getCustomers().stream().map(CustomerData::getId).toList());
        requireUniqueIds("food item", data.getFoodItems().stream().map(FoodItemData::getId).toList());
        requireUniqueIds("order", data.getOrders().stream().map(FoodOrderData::getId).toList());
        requireUniqueIds("archived order", data.getArchivedOrders().stream().map(FoodOrderData::getId).toList());
        requireUniqueIds("delivery", data.getDeliveries().stream().map(DeliveryData::getId).toList());
        requireUniqueIds("courier", data.getDeliveryPersons().stream().map(DeliveryPersonData::getId).toList());
        requireUniqueIds("payment", data.getPayments().stream().map(PaymentData::getId).toList());

        Set<Long> customerIds = ids(data.getCustomers(), CustomerData::getId);
        Set<Long> foodIds = ids(data.getFoodItems(), FoodItemData::getId);
        Set<Long> orderIds = ids(data.getOrders(), FoodOrderData::getId);
        Set<Long> archivedIds = ids(data.getArchivedOrders(), FoodOrderData::getId);
        Set<Long> deliveryIds = ids(data.getDeliveries(), DeliveryData::getId);
        Set<Long> personIds = ids(data.getDeliveryPersons(), DeliveryPersonData::getId);
        Set<String> emails = new HashSet<>();
        for (CustomerData customer : data.getCustomers()) {
            requireText(customer.getFullName(), "Customer name");
            requireText(customer.getEmail(), "Customer email");
            requireText(customer.getPhone(), "Customer phone");
            if (!emails.add(customer.getEmail().toLowerCase())) {
                throw new DataStoreException("Duplicate customer email: " + customer.getEmail());
            }
        }
        for (FoodItemData food : data.getFoodItems()) {
            requireText(food.getName(), "Food name");
            if (food.getPrice() == null || food.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new DataStoreException("Food item " + food.getId() + " has an invalid price");
            }
            if ("BEVERAGE".equalsIgnoreCase(food.getType())) {
                requireText(food.getVolumeSize(), "Beverage volume");
                requireText(food.getSugarLevel(), "Beverage sugar level");
            } else if ("MAIN_COURSE".equalsIgnoreCase(food.getType())) {
                requireText(food.getCuisineType(), "Cuisine type");
                requireText(food.getSpiceLevel(), "Spice level");
                requireText(food.getServingPortion(), "Serving portion");
            } else {
                throw new DataStoreException("Food item " + food.getId() + " has an unknown type");
            }
        }
        Set<String> phones = new HashSet<>();
        for (DeliveryPersonData person : data.getDeliveryPersons()) {
            requireText(person.getName(), "Courier name");
            requireText(person.getPhone(), "Courier phone");
            if (!phones.add(person.getPhone())) {
                throw new DataStoreException("Duplicate courier phone: " + person.getPhone());
            }
        }
        for (Long activeId : orderIds) {
            if (archivedIds.contains(activeId)) {
                throw new DataStoreException("Order id " + activeId + " is both active and archived");
            }
        }
        Set<String> orderNumbers = new HashSet<>();
        Set<Long> itemIds = new HashSet<>();
        for (FoodOrderData order : data.getOrders()) {
            validateOrder(order, customerIds, foodIds, deliveryIds, itemIds, orderNumbers);
        }
        for (FoodOrderData order : data.getArchivedOrders()) {
            validateOrder(order, customerIds, foodIds, deliveryIds, itemIds, orderNumbers);
        }
        Set<String> tracking = new HashSet<>();
        for (DeliveryData delivery : data.getDeliveries()) {
            requireText(delivery.getTrackingNumber(), "Tracking number");
            requireText(delivery.getDeliveryStatus(), "Delivery status");
            if (!tracking.add(delivery.getTrackingNumber())) {
                throw new DataStoreException("Duplicate tracking number: " + delivery.getTrackingNumber());
            }
            if (delivery.getDeliveryPersonId() != null && !personIds.contains(delivery.getDeliveryPersonId())) {
                throw new DataStoreException("Delivery " + delivery.getId() + " references a missing courier");
            }
            if (delivery.getOnlineOrderId() != null
                    && !orderIds.contains(delivery.getOnlineOrderId())
                    && !archivedIds.contains(delivery.getOnlineOrderId())) {
                throw new DataStoreException("Delivery " + delivery.getId() + " references a missing order");
            }
        }
        Set<Long> paidOrders = new HashSet<>();
        Set<String> references = new HashSet<>();
        for (PaymentData payment : data.getPayments()) {
            if (payment.getOrderId() == null
                    || (!orderIds.contains(payment.getOrderId()) && !archivedIds.contains(payment.getOrderId()))) {
                throw new DataStoreException("Payment " + payment.getId() + " references a missing order");
            }
            if (!paidOrders.add(payment.getOrderId())) {
                throw new DataStoreException("Order " + payment.getOrderId() + " has more than one payment");
            }
            requireText(payment.getTransactionReference(), "Transaction reference");
            if (!references.add(payment.getTransactionReference())) {
                throw new DataStoreException("Duplicate transaction reference: " + payment.getTransactionReference());
            }
            if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new DataStoreException("Payment " + payment.getId() + " has an invalid amount");
            }
            if ("CASH".equalsIgnoreCase(payment.getType())) {
                if (payment.getAmountTendered() == null || payment.getAmountTendered().compareTo(payment.getAmount()) < 0) {
                    throw new DataStoreException("Cash payment " + payment.getId() + " is short of the amount due");
                }
            } else if ("CARD".equalsIgnoreCase(payment.getType())) {
                if (payment.getCardLastFour() == null || !payment.getCardLastFour().matches("\\d{4}")) {
                    throw new DataStoreException("Card payment " + payment.getId() + " must store only the last four digits");
                }
                requireText(payment.getCardType(), "Card type");
                requireText(payment.getAuthorizationCode(), "Authorization code");
                requireText(payment.getGatewayTransactionId(), "Gateway transaction id");
            } else {
                throw new DataStoreException("Payment " + payment.getId() + " has an unknown type");
            }
        }
        validateOperations(data, customerIds, orderIds, archivedIds, deliveryIds, personIds);
    }

    private void validateOperations(ApplicationData data, Set<Long> customerIds, Set<Long> orderIds,
                                    Set<Long> archivedIds, Set<Long> deliveryIds, Set<Long> personIds) {
        requireUniqueIds("complaint", data.getComplaints().stream().map(OrderComplaintData::getId).toList());
        requireUniqueIds("review", data.getReviews().stream().map(OrderReviewData::getId).toList());
        requireUniqueIds("saved address", data.getSavedAddresses().stream().map(SavedAddressData::getId).toList());
        requireUniqueIds("delivery zone", data.getDeliveryZones().stream().map(DeliveryZoneData::getId).toList());
        requireUniqueIds("courier shift", data.getCourierShifts().stream().map(CourierShiftData::getId).toList());
        requireUniqueIds("checkpoint", data.getDeliveryCheckpoints().stream().map(DeliveryCheckpointData::getId).toList());
        requireUniqueIds("refund", data.getRefunds().stream().map(RefundData::getId).toList());

        Set<Long> zoneIds = ids(data.getDeliveryZones(), DeliveryZoneData::getId);
        Set<String> zoneNames = new HashSet<>();
        for (DeliveryZoneData zone : data.getDeliveryZones()) {
            requireText(zone.getAreaName(), "Delivery zone");
            if (zone.getFee() == null || zone.getFee().compareTo(BigDecimal.ZERO) < 0) {
                throw new DataStoreException("Delivery zone " + zone.getId() + " has an invalid fee");
            }
            if (!zoneNames.add(zone.getAreaName().toLowerCase())) {
                throw new DataStoreException("Duplicate delivery zone: " + zone.getAreaName());
            }
        }
        for (FoodOrderData order : data.getOrders()) {
            requireZone(order, zoneIds);
        }
        for (FoodOrderData order : data.getArchivedOrders()) {
            requireZone(order, zoneIds);
        }
        Set<Long> reviewedOrders = new HashSet<>();
        for (OrderReviewData review : data.getReviews()) {
            requireOrderRef(review.getOrderId(), orderIds, archivedIds, "Review " + review.getId());
            if (review.getCustomerId() == null || !customerIds.contains(review.getCustomerId())) {
                throw new DataStoreException("Review " + review.getId() + " references a missing customer");
            }
            if (review.getRating() < 1 || review.getRating() > 5) {
                throw new DataStoreException("Review " + review.getId() + " has an invalid rating");
            }
            requireText(review.getComment(), "Review comment");
            if (!reviewedOrders.add(review.getOrderId())) {
                throw new DataStoreException("Order " + review.getOrderId() + " has more than one review");
            }
        }
        for (OrderComplaintData complaint : data.getComplaints()) {
            requireOrderRef(complaint.getOrderId(), orderIds, archivedIds, "Complaint " + complaint.getId());
            if (complaint.getCustomerId() == null || !customerIds.contains(complaint.getCustomerId())) {
                throw new DataStoreException("Complaint " + complaint.getId() + " references a missing customer");
            }
            requireText(complaint.getMessage(), "Complaint");
            if (!"OPEN".equalsIgnoreCase(complaint.getStatus()) && !"RESOLVED".equalsIgnoreCase(complaint.getStatus())) {
                throw new DataStoreException("Complaint " + complaint.getId() + " has an unknown status");
            }
        }
        for (SavedAddressData address : data.getSavedAddresses()) {
            if (address.getCustomerId() == null || !customerIds.contains(address.getCustomerId())) {
                throw new DataStoreException("Saved address " + address.getId() + " references a missing customer");
            }
            requireText(address.getLabel(), "Address label");
            requireText(address.getAddressLine(), "Saved address");
        }
        for (CourierShiftData shift : data.getCourierShifts()) {
            if (shift.getCourierId() == null || !personIds.contains(shift.getCourierId())) {
                throw new DataStoreException("Courier shift " + shift.getId() + " references a missing courier");
            }
            if (shift.getShiftDate() == null || shift.getStartTime() == null || shift.getEndTime() == null
                    || !shift.getEndTime().isAfter(shift.getStartTime())) {
                throw new DataStoreException("Courier shift " + shift.getId() + " has an invalid time");
            }
        }
        for (DeliveryCheckpointData checkpoint : data.getDeliveryCheckpoints()) {
            if (checkpoint.getDeliveryId() == null || !deliveryIds.contains(checkpoint.getDeliveryId())) {
                throw new DataStoreException("Checkpoint " + checkpoint.getId() + " references a missing delivery");
            }
            if (checkpoint.getStep() == null
                    || !(checkpoint.getStep().equals("PICKED_UP")
                    || checkpoint.getStep().equals("ON_THE_WAY")
                    || checkpoint.getStep().equals("NEARBY"))) {
                throw new DataStoreException("Checkpoint " + checkpoint.getId() + " has an unknown step");
            }
            if (checkpoint.getRecordedAt() == null) {
                throw new DataStoreException("Checkpoint " + checkpoint.getId() + " requires a time");
            }
        }
        Set<Long> paymentIds = ids(data.getPayments(), PaymentData::getId);
        java.util.Map<Long, BigDecimal> paymentAmounts = new java.util.HashMap<>();
        for (PaymentData payment : data.getPayments()) {
            paymentAmounts.put(payment.getId(), payment.getAmount());
        }
        Set<Long> refundedPayments = new HashSet<>();
        for (RefundData refund : data.getRefunds()) {
            if (refund.getPaymentId() == null || !paymentIds.contains(refund.getPaymentId())) {
                throw new DataStoreException("Refund " + refund.getId() + " references a missing payment");
            }
            requireOrderRef(refund.getOrderId(), orderIds, archivedIds, "Refund " + refund.getId());
            requireText(refund.getReason(), "Refund reason");
            if (!"REQUESTED".equalsIgnoreCase(refund.getStatus()) && !"PAID".equalsIgnoreCase(refund.getStatus())) {
                throw new DataStoreException("Refund " + refund.getId() + " has an unknown status");
            }
            BigDecimal paymentAmount = paymentAmounts.get(refund.getPaymentId());
            if (refund.getAmount() == null || refund.getAmount().compareTo(BigDecimal.ZERO) <= 0
                    || (paymentAmount != null && refund.getAmount().compareTo(paymentAmount) > 0)) {
                throw new DataStoreException("Refund " + refund.getId() + " has an invalid amount");
            }
            if (!refundedPayments.add(refund.getPaymentId())) {
                throw new DataStoreException("Payment " + refund.getPaymentId() + " has more than one refund");
            }
        }
    }

    private static void requireZone(FoodOrderData order, Set<Long> zoneIds) {
        if (order.getDeliveryZoneId() != null && !zoneIds.contains(order.getDeliveryZoneId())) {
            throw new DataStoreException("Order " + order.getId() + " references a missing delivery zone");
        }
    }

    private static void requireOrderRef(Long orderId, Set<Long> orderIds, Set<Long> archivedIds, String label) {
        if (orderId == null || (!orderIds.contains(orderId) && !archivedIds.contains(orderId))) {
            throw new DataStoreException(label + " references a missing order");
        }
    }

    private void validateOrder(FoodOrderData order, Set<Long> customerIds, Set<Long> foodIds,
                               Set<Long> deliveryIds, Set<Long> itemIds, Set<String> orderNumbers) {
        requireText(order.getOrderNumber(), "Order number");
        requireText(order.getOrderStatus(), "Order status");
        if (!orderNumbers.add(order.getOrderNumber())) {
            throw new DataStoreException("Duplicate order number: " + order.getOrderNumber());
        }
        if (order.getCustomerId() == null || !customerIds.contains(order.getCustomerId())) {
            throw new DataStoreException("Order " + order.getId() + " references a missing customer");
        }
        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new DataStoreException("Order " + order.getId() + " has an invalid total");
        }
        if ("ONLINE".equalsIgnoreCase(order.getType())) {
            requireText(order.getDeliveryAddress(), "Delivery address");
            if (order.getDeliveryId() != null && !deliveryIds.contains(order.getDeliveryId())) {
                throw new DataStoreException("Online order " + order.getId() + " references a missing delivery");
            }
        } else if ("PICKUP".equalsIgnoreCase(order.getType())) {
            requireText(order.getPickupTimeSlot(), "Pickup time slot");
            requireText(order.getPickupContactPhone(), "Pickup contact phone");
        } else {
            throw new DataStoreException("Order " + order.getId() + " has an unknown type");
        }
        if (order.getItems() != null) {
            for (OrderItemData item : order.getItems()) {
                if (item.getId() == null || !itemIds.add(item.getId())) {
                    throw new DataStoreException("Order " + order.getId() + " has a missing or duplicate item id");
                }
                if (item.getFoodItemId() == null || !foodIds.contains(item.getFoodItemId())) {
                    throw new DataStoreException("Order item " + item.getId() + " references a missing food item");
                }
                if (item.getQuantity() <= 0 || item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new DataStoreException("Order item " + item.getId() + " has an invalid quantity or price");
                }
            }
        }
    }

    private static void requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new DataStoreException(label + " is required");
        }
    }

    private static void requireUniqueIds(String label, List<Long> ids) {
        Set<Long> seen = new HashSet<>();
        for (Long id : ids) {
            if (id == null || id <= 0 || !seen.add(id)) {
                throw new DataStoreException("Invalid or duplicate " + label + " id: " + id);
            }
        }
    }

    private static <T> Set<Long> ids(List<T> items, java.util.function.Function<T, Long> idFn) {
        Set<Long> ids = new HashSet<>();
        for (T item : items) {
            Long id = idFn.apply(item);
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }
}
