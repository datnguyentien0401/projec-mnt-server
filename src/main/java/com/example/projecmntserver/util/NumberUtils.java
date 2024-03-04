package com.example.projecmntserver.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NumberUtils {
    public static Number add(Number a, Number b) {
        if (a instanceof Long && b instanceof Long) {
            return a.longValue() + b.longValue();
        }
        if (a instanceof Integer && b instanceof Integer) {
            return a.intValue() + b.intValue();
        } else if (a instanceof Double && b instanceof Double) {
            return a.doubleValue() + b.doubleValue();
        }else if (a instanceof Float && b instanceof Float) {
            return a.floatValue() + b.floatValue();
        } else {
            throw new IllegalArgumentException("Unsupported numeric type" + a + ' ' + b);
        }
    }
}
