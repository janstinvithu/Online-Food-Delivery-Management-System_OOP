package com.fooddelivery.model.delivery;

import com.fooddelivery.common.util.PersistentIds;
import java.time.LocalDateTime;

public class DeliveryCheckpoint {

    private Long id;
    private Long deliveryId;
    private String step;
    private String note;
    private LocalDateTime recordedAt;

    public Long getId() { return id; }
    public void assignPersistentId(Long persistentId) { this.id = PersistentIds.assign(this.id, persistentId); }
    public Long getDeliveryId() { return deliveryId; }
    public void setDeliveryId(Long deliveryId) { this.deliveryId = deliveryId; }
    public String getStep() { return step; }
    public void setStep(String step) { this.step = step; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
