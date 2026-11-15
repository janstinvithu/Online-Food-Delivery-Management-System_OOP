package com.fooddelivery.model.delivery;

import com.fooddelivery.common.util.PersistentIds;
import java.math.BigDecimal;

public class DeliveryZone {

    private Long id;
    private String areaName;
    private BigDecimal fee;

    public Long getId() { return id; }
    public void assignPersistentId(Long persistentId) { this.id = PersistentIds.assign(this.id, persistentId); }
    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }
    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }
}
