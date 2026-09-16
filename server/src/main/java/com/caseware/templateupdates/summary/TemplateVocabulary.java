package com.caseware.templateupdates.summary;

import java.util.Optional;

/**
 * Display names owned by the content team, read from the template content itself
 * (e.g. {@code sections.planning.displayName = "Planning"}). Falls back to humanised keys when absent.
 */
public interface TemplateVocabulary {
    Optional<String> sectionDisplayName(String templateId, int version, String sectionKey);

    TemplateVocabulary NONE = (templateId, version, sectionKey) -> Optional.empty();
}
