package com.caseware.templateupdates.diff;

import java.util.Objects;

/**
 * One entry of the raw technical diff, e.g.
 * {@code {"op":"replace","path":"/sections/materiality/guidance/thresholdPercent","oldValue":4.5,"newValue":4.0}}.
 * Values are the JSON values already parsed into Java (String, Number, Boolean, Map, List) or null.
 */
public record RawChange(DiffOperation op, String path, Object oldValue, Object newValue) {
    public RawChange {
        Objects.requireNonNull(op, "op");
        Objects.requireNonNull(path, "path");
        if (!path.startsWith("/")) throw new IllegalArgumentException("path must be a JSON pointer: " + path);
    }

    public static RawChange add(String path, Object value) { return new RawChange(DiffOperation.ADD, path, null, value); }
    public static RawChange replace(String path, Object oldValue, Object newValue) { return new RawChange(DiffOperation.REPLACE, path, oldValue, newValue); }
    public static RawChange remove(String path, Object oldValue) { return new RawChange(DiffOperation.REMOVE, path, oldValue, null); }
}
