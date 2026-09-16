package com.caseware.templateupdates.catalog;

import java.util.Optional;

/** Read port over the template catalog projection (not the full template database). */
public interface TemplateCatalog {
    Optional<TemplateCatalogEntry> find(String templateId);
}
