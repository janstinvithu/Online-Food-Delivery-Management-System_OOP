package com.fooddelivery.repository.impl;

import com.fooddelivery.common.exception.ResourceNotFoundException;
import com.fooddelivery.model.customer.SavedAddress;
import com.fooddelivery.model.delivery.CourierShift;
import com.fooddelivery.model.delivery.DeliveryCheckpoint;
import com.fooddelivery.model.delivery.DeliveryZone;
import com.fooddelivery.model.delivery.Refund;
import com.fooddelivery.model.order.OrderComplaint;
import com.fooddelivery.model.order.OrderReview;
import com.fooddelivery.persistence.ApplicationData;
import com.fooddelivery.persistence.DataStoreManager;
import com.fooddelivery.persistence.dto.CourierShiftData;
import com.fooddelivery.persistence.dto.DeliveryCheckpointData;
import com.fooddelivery.persistence.dto.DeliveryZoneData;
import com.fooddelivery.persistence.dto.RefundData;
import com.fooddelivery.persistence.dto.OrderComplaintData;
import com.fooddelivery.persistence.dto.OrderReviewData;
import com.fooddelivery.persistence.dto.SavedAddressData;
import com.fooddelivery.repository.OperationsRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Repository;

@Repository
public class FileOperationsRepository implements OperationsRepository {

    private final DataStoreManager store;

    public FileOperationsRepository(DataStoreManager store) {
        this.store = store;
    }

    @Override
    public OrderComplaint saveComplaint(OrderComplaint complaint) {
        return store.update(data -> {
            if (complaint.getId() == null) {
                complaint.assignPersistentId(data.getSequences().nextComplaintId());
            }
            upsert(data.getComplaints(), toComplaintData(complaint), OrderComplaintData::getId);
            return complaint;
        });
    }

    @Override
    public List<OrderComplaint> findComplaintsByOrderId(Long orderId) {
        return store.read(data -> data.getComplaints().stream()
                .filter(item -> orderId.equals(item.getOrderId()))
                .map(this::toComplaint)
                .sorted(Comparator.comparing(OrderComplaint::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList());
    }

    @Override
    public Optional<OrderComplaint> findComplaintById(Long id) {
        return store.read(data -> data.getComplaints().stream()
                .filter(item -> id.equals(item.getId()))
                .map(this::toComplaint)
                .findFirst());
    }

    @Override
    public void deleteComplaintById(Long id) {
        remove(id, ApplicationData::getComplaints, OrderComplaintData::getId, "OrderComplaint");
    }

    @Override
    public OrderReview saveReview(OrderReview review) {
        return store.update(data -> {
            if (review.getId() == null) {
                review.assignPersistentId(data.getSequences().nextReviewId());
            }
            upsert(data.getReviews(), toReviewData(review), OrderReviewData::getId);
            return review;
        });
    }

    @Override
    public Optional<OrderReview> findReviewByOrderId(Long orderId) {
        return store.read(data -> data.getReviews().stream()
                .filter(item -> orderId.equals(item.getOrderId()))
                .map(this::toReview)
                .findFirst());
    }

    @Override
    public Optional<OrderReview> findReviewById(Long id) {
        return store.read(data -> data.getReviews().stream()
                .filter(item -> id.equals(item.getId()))
                .map(this::toReview)
                .findFirst());
    }

    @Override
    public void deleteReviewById(Long id) {
        remove(id, ApplicationData::getReviews, OrderReviewData::getId, "OrderReview");
    }

    @Override
    public SavedAddress saveAddress(SavedAddress address) {
        return store.update(data -> {
            if (address.getId() == null) {
                address.assignPersistentId(data.getSequences().nextSavedAddressId());
            }
            upsert(data.getSavedAddresses(), toAddressData(address), SavedAddressData::getId);
            return address;
        });
    }

    @Override
    public List<SavedAddress> findAddressesByCustomerId(Long customerId) {
        return store.read(data -> data.getSavedAddresses().stream()
                .filter(item -> customerId.equals(item.getCustomerId()))
                .map(this::toAddress)
                .toList());
    }

    @Override
    public Optional<SavedAddress> findAddressById(Long id) {
        return store.read(data -> data.getSavedAddresses().stream()
                .filter(item -> id.equals(item.getId()))
                .map(this::toAddress)
                .findFirst());
    }

    @Override
    public void deleteAddressById(Long id) {
        remove(id, ApplicationData::getSavedAddresses, SavedAddressData::getId, "SavedAddress");
    }

    @Override
    public DeliveryZone saveZone(DeliveryZone zone) {
        return store.update(data -> {
            if (zone.getId() == null) {
                zone.assignPersistentId(data.getSequences().nextDeliveryZoneId());
            }
            upsert(data.getDeliveryZones(), toZoneData(zone), DeliveryZoneData::getId);
            return zone;
        });
    }

    @Override
    public List<DeliveryZone> findAllZones() {
        return store.read(data -> data.getDeliveryZones().stream().map(this::toZone).toList());
    }

    @Override
    public Optional<DeliveryZone> findZoneById(Long id) {
        return store.read(data -> data.getDeliveryZones().stream()
                .filter(item -> id.equals(item.getId()))
                .map(this::toZone)
                .findFirst());
    }

    @Override
    public boolean zoneReferencedByOrder(Long zoneId) {
        return store.read(data -> references(data.getOrders(), zoneId) || references(data.getArchivedOrders(), zoneId));
    }

    @Override
    public void deleteZoneById(Long id) {
        remove(id, ApplicationData::getDeliveryZones, DeliveryZoneData::getId, "DeliveryZone");
    }

    @Override
    public CourierShift saveShift(CourierShift shift) {
        return store.update(data -> {
            if (shift.getId() == null) {
                shift.assignPersistentId(data.getSequences().nextCourierShiftId());
            }
            upsert(data.getCourierShifts(), toShiftData(shift), CourierShiftData::getId);
            return shift;
        });
    }

    @Override
    public List<CourierShift> findShiftsByCourierId(Long courierId) {
        return store.read(data -> data.getCourierShifts().stream()
                .filter(item -> courierId.equals(item.getCourierId()))
                .map(this::toShift)
                .sorted(Comparator.comparing(CourierShift::getShiftDate).thenComparing(CourierShift::getStartTime))
                .toList());
    }

    @Override
    public Optional<CourierShift> findShiftById(Long id) {
        return store.read(data -> data.getCourierShifts().stream()
                .filter(item -> id.equals(item.getId()))
                .map(this::toShift)
                .findFirst());
    }

    @Override
    public void deleteShiftById(Long id) {
        remove(id, ApplicationData::getCourierShifts, CourierShiftData::getId, "CourierShift");
    }

    @Override
    public DeliveryCheckpoint saveCheckpoint(DeliveryCheckpoint checkpoint) {
        return store.update(data -> {
            if (checkpoint.getId() == null) {
                checkpoint.assignPersistentId(data.getSequences().nextCheckpointId());
            }
            upsert(data.getDeliveryCheckpoints(), toCheckpointData(checkpoint), DeliveryCheckpointData::getId);
            return checkpoint;
        });
    }

    @Override
    public List<DeliveryCheckpoint> findCheckpointsByDeliveryId(Long deliveryId) {
        return store.read(data -> data.getDeliveryCheckpoints().stream()
                .filter(item -> deliveryId.equals(item.getDeliveryId()))
                .map(this::toCheckpoint)
                .sorted(Comparator.comparing(DeliveryCheckpoint::getRecordedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList());
    }

    @Override
    public Optional<DeliveryCheckpoint> findCheckpointById(Long id) {
        return store.read(data -> data.getDeliveryCheckpoints().stream()
                .filter(item -> id.equals(item.getId()))
                .map(this::toCheckpoint)
                .findFirst());
    }

    @Override
    public void deleteCheckpointById(Long id) {
        remove(id, ApplicationData::getDeliveryCheckpoints, DeliveryCheckpointData::getId, "DeliveryCheckpoint");
    }

    @Override
    public Refund saveRefund(Refund refund) {
        return store.update(data -> {
            if (refund.getId() == null) {
                refund.assignPersistentId(data.getSequences().nextRefundId());
            }
            upsert(data.getRefunds(), toRefundData(refund), RefundData::getId);
            return refund;
        });
    }

    @Override
    public List<Refund> findRefundsByPaymentId(Long paymentId) {
        return store.read(data -> data.getRefunds().stream()
                .filter(item -> paymentId.equals(item.getPaymentId()))
                .map(this::toRefund)
                .toList());
    }

    @Override
    public Optional<Refund> findRefundById(Long id) {
        return store.read(data -> data.getRefunds().stream()
                .filter(item -> id.equals(item.getId()))
                .map(this::toRefund)
                .findFirst());
    }

    @Override
    public boolean refundExistsForPayment(Long paymentId) {
        return store.read(data -> data.getRefunds().stream().anyMatch(item -> paymentId.equals(item.getPaymentId())));
    }

    @Override
    public void deleteRefundById(Long id) {
        remove(id, ApplicationData::getRefunds, RefundData::getId, "Refund");
    }

    private boolean references(List<com.fooddelivery.persistence.dto.FoodOrderData> orders, Long zoneId) {
        return orders.stream().anyMatch(order -> zoneId.equals(order.getDeliveryZoneId()));
    }

    private <T> void remove(Long id, Function<ApplicationData, List<T>> listFn, Function<T, Long> idFn, String name) {
        if (id == null) {
            throw new IllegalArgumentException(name + " id is required");
        }
        store.update(data -> {
            boolean removed = listFn.apply(data).removeIf(item -> id.equals(idFn.apply(item)));
            if (!removed) {
                throw new ResourceNotFoundException(name, "id", id);
            }
            return null;
        });
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

    private OrderComplaintData toComplaintData(OrderComplaint source) {
        OrderComplaintData data = new OrderComplaintData();
        data.setId(source.getId());
        data.setOrderId(source.getOrderId());
        data.setCustomerId(source.getCustomerId());
        data.setMessage(source.getMessage());
        data.setStatus(source.getStatus());
        data.setCreatedAt(source.getCreatedAt());
        return data;
    }

    private OrderComplaint toComplaint(OrderComplaintData source) {
        OrderComplaint complaint = new OrderComplaint();
        complaint.assignPersistentId(source.getId());
        complaint.setOrderId(source.getOrderId());
        complaint.setCustomerId(source.getCustomerId());
        complaint.setMessage(source.getMessage());
        complaint.setStatus(source.getStatus());
        complaint.setCreatedAt(source.getCreatedAt());
        return complaint;
    }

    private OrderReviewData toReviewData(OrderReview source) {
        OrderReviewData data = new OrderReviewData();
        data.setId(source.getId());
        data.setOrderId(source.getOrderId());
        data.setCustomerId(source.getCustomerId());
        data.setRating(source.getRating());
        data.setComment(source.getComment());
        data.setAdminReply(source.getAdminReply());
        data.setCreatedAt(source.getCreatedAt());
        data.setRepliedAt(source.getRepliedAt());
        return data;
    }

    private OrderReview toReview(OrderReviewData source) {
        OrderReview review = new OrderReview();
        review.assignPersistentId(source.getId());
        review.setOrderId(source.getOrderId());
        review.setCustomerId(source.getCustomerId());
        review.setRating(source.getRating());
        review.setComment(source.getComment());
        review.setAdminReply(source.getAdminReply());
        review.setCreatedAt(source.getCreatedAt());
        review.setRepliedAt(source.getRepliedAt());
        return review;
    }

    private SavedAddressData toAddressData(SavedAddress source) {
        SavedAddressData data = new SavedAddressData();
        data.setId(source.getId());
        data.setCustomerId(source.getCustomerId());
        data.setLabel(source.getLabel());
        data.setAddressLine(source.getAddressLine());
        return data;
    }

    private SavedAddress toAddress(SavedAddressData source) {
        SavedAddress address = new SavedAddress();
        address.assignPersistentId(source.getId());
        address.setCustomerId(source.getCustomerId());
        address.setLabel(source.getLabel());
        address.setAddressLine(source.getAddressLine());
        return address;
    }

    private DeliveryZoneData toZoneData(DeliveryZone source) {
        DeliveryZoneData data = new DeliveryZoneData();
        data.setId(source.getId());
        data.setAreaName(source.getAreaName());
        data.setFee(source.getFee());
        return data;
    }

    private DeliveryZone toZone(DeliveryZoneData source) {
        DeliveryZone zone = new DeliveryZone();
        zone.assignPersistentId(source.getId());
        zone.setAreaName(source.getAreaName());
        zone.setFee(source.getFee());
        return zone;
    }

    private CourierShiftData toShiftData(CourierShift source) {
        CourierShiftData data = new CourierShiftData();
        data.setId(source.getId());
        data.setCourierId(source.getCourierId());
        data.setShiftDate(source.getShiftDate());
        data.setStartTime(source.getStartTime());
        data.setEndTime(source.getEndTime());
        return data;
    }

    private CourierShift toShift(CourierShiftData source) {
        CourierShift shift = new CourierShift();
        shift.assignPersistentId(source.getId());
        shift.setCourierId(source.getCourierId());
        shift.setShiftDate(source.getShiftDate());
        shift.setStartTime(source.getStartTime());
        shift.setEndTime(source.getEndTime());
        return shift;
    }

    private DeliveryCheckpointData toCheckpointData(DeliveryCheckpoint source) {
        DeliveryCheckpointData data = new DeliveryCheckpointData();
        data.setId(source.getId());
        data.setDeliveryId(source.getDeliveryId());
        data.setStep(source.getStep());
        data.setNote(source.getNote());
        data.setRecordedAt(source.getRecordedAt());
        return data;
    }

    private RefundData toRefundData(Refund source) {
        RefundData data = new RefundData();
        data.setId(source.getId());
        data.setPaymentId(source.getPaymentId());
        data.setOrderId(source.getOrderId());
        data.setAmount(source.getAmount());
        data.setReason(source.getReason());
        data.setStatus(source.getStatus());
        data.setCreatedAt(source.getCreatedAt());
        return data;
    }

    private Refund toRefund(RefundData source) {
        Refund refund = new Refund();
        refund.assignPersistentId(source.getId());
        refund.setPaymentId(source.getPaymentId());
        refund.setOrderId(source.getOrderId());
        refund.setAmount(source.getAmount());
        refund.setReason(source.getReason());
        refund.setStatus(source.getStatus());
        refund.setCreatedAt(source.getCreatedAt());
        return refund;
    }

    private DeliveryCheckpoint toCheckpoint(DeliveryCheckpointData source) {
        DeliveryCheckpoint checkpoint = new DeliveryCheckpoint();
        checkpoint.assignPersistentId(source.getId());
        checkpoint.setDeliveryId(source.getDeliveryId());
        checkpoint.setStep(source.getStep());
        checkpoint.setNote(source.getNote());
        checkpoint.setRecordedAt(source.getRecordedAt());
        return checkpoint;
    }
}
