package com.fooddelivery.model.customer;

import com.fooddelivery.common.util.PersistentIds;

public class SavedAddress {

    private Long id;
    private Long customerId;
    private String label;
    private String addressLine;

    public Long getId() { return id; }
    public void assignPersistentId(Long persistentId) { this.id = PersistentIds.assign(this.id, persistentId); }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getAddressLine() { return addressLine; }
    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }
}
