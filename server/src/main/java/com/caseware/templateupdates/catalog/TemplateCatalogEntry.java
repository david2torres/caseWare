package com.caseware.templateupdates.catalog;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Firm-agnostic view of a product template and its published versions.
 * Maintained from TemplatePublished events; shared by every firm.
 */
public record TemplateCatalogEntry(String templateId, String displayName, List<TemplateVersion> versions) {

    public TemplateCatalogEntry {
        Objects.requireNonNull(templateId, "templateId");
        Objects.requireNonNull(displayName, "displayName");
        if (versions == null || versions.isEmpty()) {
            throw new IllegalArgumentException("a catalog entry needs at least one published version");
        }
        // Defensive copy, always sorted ascending so callers never depend on input order.
        versions = versions.stream().sorted(Comparator.comparingInt(TemplateVersion::version)).toList();
    }

    public int latestVersion() {
        return versions.getLast().version();
    }

    /** Versions strictly newer than {@code baseline}, oldest first. */
    public List<TemplateVersion> versionsAfter(int baseline) {
        return versions.stream().filter(v -> v.version() > baseline).toList();
    }
}
