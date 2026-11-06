package com.fooddelivery.common.util;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class OrderIdGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private OrderIdGenerator() {
        // Utility class private constructor
    }

    public static String generateOrderNumber() {
        String datePrefix = LocalDate.now().format(DATE_FORMATTER);
        int suffix = 1000 + RANDOM.nextInt(9000);
        return "ORD-" + datePrefix + "-" + suffix;
    }

    public static String generateTrackingNumber() {
        int suffix = 100000 + RANDOM.nextInt(900000);
        return "TRK-" + suffix;
    }

    public static String generateTransactionReference(String prefix) {
        int suffix = 100000 + RANDOM.nextInt(900000);
        return "TXN-" + prefix.toUpperCase() + "-" + suffix;
    }
}
