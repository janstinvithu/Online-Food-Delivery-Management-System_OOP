package com.fooddelivery.common.util;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Component("currency")
public class CurrencyUtil {

    private static final String CURRENCY_CODE = "LKR";
    private static final DecimalFormat DECIMAL_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DECIMAL_FORMAT = new DecimalFormat("#,##0.00", symbols);
    }

    public static String format(BigDecimal amount) {
        if (amount == null) {
            return CURRENCY_CODE + " 0.00";
        }
        return CURRENCY_CODE + " " + DECIMAL_FORMAT.format(amount);
    }

    public static String format(double amount) {
        return format(BigDecimal.valueOf(amount));
    }

    public static String formatRs(BigDecimal amount) {
        if (amount == null) {
            return "Rs. 0.00";
        }
        return "Rs. " + DECIMAL_FORMAT.format(amount);
    }

    public String formatCurrency(BigDecimal amount) {
        return format(amount);
    }
}
