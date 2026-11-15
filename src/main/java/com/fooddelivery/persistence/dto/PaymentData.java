package com.fooddelivery.persistence.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentData {

    private Long id;
    private String type;
    private Long orderId;
    private BigDecimal amount;
    private String paymentStatus;
    private LocalDateTime paymentTimestamp;
    private String transactionReference;
    private BigDecimal amountTendered;
    private BigDecimal changeDue;
    private String cardLastFour;
    private String cardType;
    private String authorizationCode;
    private String gatewayTransactionId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public LocalDateTime getPaymentTimestamp() { return paymentTimestamp; }
    public void setPaymentTimestamp(LocalDateTime paymentTimestamp) { this.paymentTimestamp = paymentTimestamp; }
    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
    public BigDecimal getAmountTendered() { return amountTendered; }
    public void setAmountTendered(BigDecimal amountTendered) { this.amountTendered = amountTendered; }
    public BigDecimal getChangeDue() { return changeDue; }
    public void setChangeDue(BigDecimal changeDue) { this.changeDue = changeDue; }
    public String getCardLastFour() { return cardLastFour; }
    public void setCardLastFour(String cardLastFour) { this.cardLastFour = cardLastFour; }
    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }
    public String getAuthorizationCode() { return authorizationCode; }
    public void setAuthorizationCode(String authorizationCode) { this.authorizationCode = authorizationCode; }
    public String getGatewayTransactionId() { return gatewayTransactionId; }
    public void setGatewayTransactionId(String gatewayTransactionId) { this.gatewayTransactionId = gatewayTransactionId; }
}
