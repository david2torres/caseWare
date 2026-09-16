package com.caseware.templateupdates;

import com.caseware.templateupdates.catalog.TemplateCatalog;
import com.caseware.templateupdates.catalog.TemplateCatalogEntry;
import com.caseware.templateupdates.catalog.TemplateVersion;
import com.caseware.templateupdates.diff.DiffLookup;
import com.caseware.templateupdates.diff.RawChange;
import com.caseware.templateupdates.diff.RawTemplateDiff;
import com.caseware.templateupdates.diff.TemplateDiffSource;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Hand-transcribed from the fixture pack (templates.json, engagements.json, template-diff-*.json)
 * so the tests need no JSON library. Only the pieces the tests use are included.
 */
public final class SampleData {

    private SampleData() {}

    public static final Instant CATALOG_AS_OF = Instant.parse("2026-08-25T13:10:00Z");
    public static final Instant INDEXED_AT = Instant.parse("2026-09-01T08:00:00Z");

    public static TemplateCatalog catalog() {
        Map<String, TemplateCatalogEntry> entries = Map.of(
                "AUDIT-CA", new TemplateCatalogEntry("AUDIT-CA", "Canadian Audit Engagement", List.of(
                        v(3, "2026-05-12T13:00:00Z"), v(4, "2026-07-07T13:00:00Z"), v(5, "2026-08-18T13:00:00Z"))),
                "REVIEW-CA", new TemplateCatalogEntry("REVIEW-CA", "Canadian Review Engagement", List.of(
                        // deliberately out of order: the entry must sort them
                        v(8, "2026-08-25T13:00:00Z"), v(6, "2026-05-20T13:00:00Z"), v(7, "2026-07-21T13:00:00Z"))));
        return id -> Optional.ofNullable(entries.get(id));
    }

    public static RawTemplateDiff reviewV6toV7() {
        return new RawTemplateDiff("REVIEW-CA", 6, 7, List.of(
                RawChange.add("/sections/inquiries/questions/12", Map.of(
                        "id", "Q-INQ-012", "type", "text",
                        "label", "Describe any events after the reporting date that may require adjustment or disclosure.")),
                RawChange.replace("/sections/analytics/procedures/2/tolerance", 0.15, 0.12),
                RawChange.remove("/sections/inquiries/questions/4/helpText",
                        "Ask management to describe changes in accounting policies since the prior year.")));
    }

    public static RawTemplateDiff reviewV7toV8() {
        return new RawTemplateDiff("REVIEW-CA", 7, 8, List.of(
                RawChange.replace("/metadata/displayName", "Canadian Review Engagement", "Canadian Review Engagement 2026"),
                RawChange.replace("/sections/analytics/procedures/2/tolerance", 0.12, 0.1),
                RawChange.add("/sections/completion/checklists/going-concern", goingConcern())));
    }

    public static RawTemplateDiff reviewV6toV8() {
        return new RawTemplateDiff("REVIEW-CA", 6, 8, List.of(
                RawChange.replace("/metadata/displayName", "Canadian Review Engagement", "Canadian Review Engagement 2026"),
                RawChange.add("/sections/inquiries/questions/12", Map.of(
                        "id", "Q-INQ-012", "type", "text",
                        "label", "Describe any events after the reporting date that may require adjustment or disclosure.")),
                RawChange.replace("/sections/analytics/procedures/2/tolerance", 0.15, 0.1),
                RawChange.remove("/sections/inquiries/questions/4/helpText",
                        "Ask management to describe changes in accounting policies since the prior year."),
                RawChange.add("/sections/completion/checklists/going-concern", goingConcern())));
    }

    public static RawTemplateDiff auditV3toV4() {
        return new RawTemplateDiff("AUDIT-CA", 3, 4, List.of(
                RawChange.add("/sections/planning/questions/7", Map.of(
                        "id", "Q-PLN-007", "type", "yesNo",
                        "label", "Were any new fraud risk factors identified during planning?", "required", true)),
                RawChange.replace("/sections/materiality/guidance/thresholdPercent", 5.0, 4.5),
                RawChange.remove("/sections/planning/procedures/legacy-risk-confirmation", Map.of(
                        "id", "PROC-PLN-004", "label", "Confirm legacy risk classification", "required", false))));
    }

    /** In-memory diff cache. Pairs not registered are reported as "not yet computed". */
    public static final class InMemoryDiffSource implements TemplateDiffSource {
        private final Map<String, DiffLookup> byKey = new HashMap<>();

        public InMemoryDiffSource with(RawTemplateDiff diff) {
            byKey.put(key(diff.templateId(), diff.fromVersion(), diff.toVersion()), new DiffLookup.Ready(diff));
            return this;
        }

        public InMemoryDiffSource failing(String templateId, int from, int to, String reason) {
            byKey.put(key(templateId, from, to), new DiffLookup.Failed(reason));
            return this;
        }

        @Override
        public DiffLookup find(String templateId, int from, int to) {
            return byKey.getOrDefault(key(templateId, from, to), new DiffLookup.NotYetComputed());
        }

        private static String key(String t, int f, int to) { return t + ":" + f + "->" + to; }
    }

    private static Map<String, Object> goingConcern() {
        return Map.of("id", "CHK-GC-01", "label", "Going concern evaluation", "items", List.of(
                "Document management's assessment", "Evaluate contradictory evidence", "Record the practitioner's conclusion"));
    }

    private static TemplateVersion v(int version, String publishedAt) {
        return new TemplateVersion(version, Instant.parse(publishedAt));
    }
}
