package com.certsniper.global.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PercentUtils {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private PercentUtils() {
    }

    /** part ÷ whole × 100을 places자리에서 사사오입 (MySQL ROUND와 같음). 값이 없거나 whole이 0이면 null */
    public static BigDecimal percent(Integer part, Integer whole, int places) {
        if (part == null || whole == null || whole == 0) {
            return null;
        }
        return BigDecimal.valueOf(part).multiply(HUNDRED).divide(BigDecimal.valueOf(whole), places, RoundingMode.HALF_UP);
    }
}
