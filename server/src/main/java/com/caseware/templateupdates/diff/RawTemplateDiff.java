package com.caseware.templateupdates.diff;

import java.util.List;

/** Raw diff between two versions of the same template, as produced by the existing diff tool. */
public record RawTemplateDiff(String templateId, int fromVersion, int toVersion, List<RawChange> changes) {
    public RawTemplateDiff {
        if (fromVersion >= toVersion) throw new IllegalArgumentException("fromVersion must be < toVersion");
        changes = List.copyOf(changes);
    }
}
