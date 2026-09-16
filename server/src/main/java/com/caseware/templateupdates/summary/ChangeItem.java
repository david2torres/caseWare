package com.caseware.templateupdates.summary;

import java.util.List;
import java.util.Objects;

/**
 * One human-readable change, as shown to a practitioner. Mirrors {@code ChangeItem} in the API contract.
 *
 * @param area               section display name, e.g. "Materiality"
 * @param title              short sentence, e.g. "Threshold percent decreased"
 * @param detail             optional extra context (question text, checklist steps, ...)
 * @param before             human-formatted previous value, null when not applicable
 * @param after              human-formatted new value, null when not applicable
 * @param changedInVersions  which pending versions touched this item (lets users see accumulated history)
 * @param requiresResponse   true when the change introduces a required item the team will have to complete
 * @param technicalPath      raw JSON pointer, kept for traceability/support; not the primary UI text
 */
public record ChangeItem(
        String changeId,
        ChangeKind kind,
        ItemType itemType,
        String area,
        String title,
        String detail,
        String before,
        String after,
        List<Integer> changedInVersions,
        boolean requiresResponse,
        String technicalPath) {

    public ChangeItem {
        Objects.requireNonNull(changeId);
        Objects.requireNonNull(kind);
        Objects.requireNonNull(itemType);
        Objects.requireNonNull(area);
        Objects.requireNonNull(title);
        Objects.requireNonNull(technicalPath);
        changedInVersions = List.copyOf(changedInVersions);
    }

    ChangeItem withChangedInVersions(List<Integer> versions) {
        return new ChangeItem(changeId, kind, itemType, area, title, detail, before, after, versions,
                requiresResponse, technicalPath);
    }
}
