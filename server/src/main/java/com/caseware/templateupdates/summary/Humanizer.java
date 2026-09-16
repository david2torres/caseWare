package com.caseware.templateupdates.summary;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/** Small, deterministic text helpers. Deliberately boring: users must be able to trust every word. */
final class Humanizer {

    private Humanizer() {}

    /** "highRiskThreshold" -> "High risk threshold", "going-concern" -> "Going concern". */
    static String words(String key) {
        String spaced = key
                .replaceAll("([a-z0-9])([A-Z])", "$1 $2")
                .replace('-', ' ')
                .replace('_', ' ')
                .trim()
                .toLowerCase(Locale.ROOT);
        return spaced.isEmpty() ? key : Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }

    /** Formats a JSON value for display. Objects are shown by their label (or id) rather than dumped. */
    static String value(Object value) {
        if (value == null) return null;
        if (value instanceof String s) return s;
        if (value instanceof Boolean b) return b ? "Yes" : "No";
        if (value instanceof Number n) return new BigDecimal(n.toString()).stripTrailingZeros().toPlainString();
        if (value instanceof Map<?, ?> m) {
            Object label = m.get("label");
            if (label != null) return label.toString();
            Object id = m.get("id");
            return id != null ? id.toString() : "(details)";
        }
        if (value instanceof List<?> l) {
            return l.stream().map(Humanizer::value).collect(Collectors.joining("; "));
        }
        return value.toString();
    }

    /** "increased"/"decreased" for numbers, otherwise "changed". */
    static String direction(Object oldValue, Object newValue) {
        if (oldValue instanceof Number o && newValue instanceof Number n) {
            int cmp = new BigDecimal(n.toString()).compareTo(new BigDecimal(o.toString()));
            if (cmp > 0) return "increased";
            if (cmp < 0) return "decreased";
        }
        return "changed";
    }
}
