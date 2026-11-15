package com.fooddelivery.model.delivery;

import com.fooddelivery.model.order.FoodOrder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public class CardPayment extends Payment {

    @NotBlank(message = "Card last four digits are required")
    @Pattern(regexp = "^\\d{4}$", message = "Card last four must be 4 numeric digits")
    private String cardLastFour;

    @NotBlank(message = "Card type is required")
    private String cardType;

    @NotBlank(message = "Authorization code is required")
    private String authorizationCode;

    @NotBlank(message = "Gateway transaction ID is required")
    private String gatewayTransactionId;

    public CardPayment() {
        super();
        this.cardLastFour = "0000";
        this.cardType = "VISA";
        this.authorizationCode = "AUTH-0000";
        this.gatewayTransactionId = "gw_0000";
    }

    public CardPayment(FoodOrder order, BigDecimal amount, String cardLastFour,
                       String cardType, String authorizationCode, String gatewayTransactionId) {
        super(order, amount);
        setCardLastFour(cardLastFour);
        setCardType(cardType);
        setAuthorizationCode(authorizationCode);
        setGatewayTransactionId(gatewayTransactionId);
    }

    public String getCardLastFour() {
        return cardLastFour;
    }

    public final void setCardLastFour(String cardLastFour) {
        if (cardLastFour == null || !cardLastFour.matches("^\\d{4}$")) {
            throw new IllegalArgumentException("Card last four must be exactly 4 digits");
        }
        this.cardLastFour = cardLastFour;
    }

    public String getCardType() {
        return cardType;
    }

    public final void setCardType(String cardType) {
        if (cardType == null || cardType.trim().isEmpty()) {
            throw new IllegalArgumentException("Card type cannot be blank");
        }
        this.cardType = cardType.trim().toUpperCase();
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public final void setAuthorizationCode(String authorizationCode) {
        if (authorizationCode == null || authorizationCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Authorization code cannot be blank");
        }
        this.authorizationCode = authorizationCode.trim();
    }

    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }

    public final void setGatewayTransactionId(String gatewayTransactionId) {
        if (gatewayTransactionId == null || gatewayTransactionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Gateway transaction ID cannot be blank");
        }
        this.gatewayTransactionId = gatewayTransactionId.trim();
    }

    @Override
    public String getPaymentDetails() {
        return cardType + " ending in •••• " + cardLastFour;
    }
}
