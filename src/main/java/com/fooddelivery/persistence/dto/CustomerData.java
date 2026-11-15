package com.fooddelivery.persistence.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerData {

    private Long id;
    private String type;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private LocalDateTime createdAt;
    private Integer loyaltyPoints;
    private String membershipTier;
    private String passwordHash;
    private String preferredPaymentMethod;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Integer getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(Integer loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
    public String getMembershipTier() { return membershipTier; }
    public void setMembershipTier(String membershipTier) { this.membershipTier = membershipTier; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getPreferredPaymentMethod() { return preferredPaymentMethod; }
    public void setPreferredPaymentMethod(String preferredPaymentMethod) { this.preferredPaymentMethod = preferredPaymentMethod; }
}
