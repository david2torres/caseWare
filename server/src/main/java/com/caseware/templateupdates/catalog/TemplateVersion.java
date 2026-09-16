package com.caseware.templateupdates.catalog;

import java.time.Instant;
import java.util.Objects;

/** One published version of a product template. */
public record TemplateVersion(int version, Instant publishedAt) {
    public TemplateVersion {
        if (version < 1) throw new IllegalArgumentException("version must be >= 1");
        Objects.requireNonNull(publishedAt, "publishedAt");
    }
}
