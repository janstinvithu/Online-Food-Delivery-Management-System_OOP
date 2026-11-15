package com.fooddelivery.model.order;

import com.fooddelivery.common.util.PersistentIds;
import java.time.LocalDateTime;

public class OrderReview {

    private Long id;
    private Long orderId;
    private Long customerId;
    private int rating;
    private String comment;
    private String adminReply;
    private LocalDateTime createdAt;
    private LocalDateTime repliedAt;

    public Long getId() { return id; }
    public void assignPersistentId(Long persistentId) { this.id = PersistentIds.assign(this.id, persistentId); }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getAdminReply() { return adminReply; }
    public void setAdminReply(String adminReply) { this.adminReply = adminReply; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getRepliedAt() { return repliedAt; }
    public void setRepliedAt(LocalDateTime repliedAt) { this.repliedAt = repliedAt; }
}
