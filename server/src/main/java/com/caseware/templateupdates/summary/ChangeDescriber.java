package com.caseware.templateupdates.summary;

import com.caseware.templateupdates.diff.RawChange;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Translates ONE raw diff entry into ONE human-readable {@link ChangeItem}.
 *
 * Rule-based and deterministic on purpose (see DESIGN.md): the output is part of an audit trail
 * ("what did the practitioner see when they applied?"), so it must be reproducible and testable.
 *
 * Understands the template path grammar seen in the sample data:
 *   /metadata/{field}
 *   /sections/{section}/{questions|checklists|procedures}/{key}[/{field}]
 *   /sections/{section}/{guidance|scoring}/{field}
 *   /sections/{section}/{anythingElse...}          -> generic, still readable fallback
 */
public final class ChangeDescriber {

    private final TemplateVocabulary vocabulary;

    public ChangeDescriber(TemplateVocabulary vocabulary) {
        this.vocabulary = Objects.requireNonNull(vocabulary);
    }

    /**
     * @param vocabularyVersion version whose display names to use (the target version of the update)
     */
    public ChangeItem describe(String templateId, int vocabularyVersion, RawChange change) {
        List<String> seg = parsePointer(change.path());
        ChangeKind kind = switch (change.op()) {
            case ADD -> ChangeKind.ADDED;
            case REPLACE -> ChangeKind.MODIFIED;
            case REMOVE -> ChangeKind.REMOVED;
        };

        if (!seg.isEmpty() && seg.get(0).equals("metadata")) {
            return describeMetadata(kind, seg, change);
        }
        if (seg.size() >= 2 && seg.get(0).equals("sections")) {
            String area = vocabulary.sectionDisplayName(templateId, vocabularyVersion, seg.get(1))
                    .orElseGet(() -> Humanizer.words(seg.get(1)));
            if (seg.size() >= 4 && itemTypeOf(seg.get(2)) != null) {
                return describeItem(kind, itemTypeOf(seg.get(2)), area, seg, change);
            }
            if (seg.size() >= 4 && (seg.get(2).equals("guidance") || seg.get(2).equals("scoring"))) {
                return describeSetting(kind, area, seg, change);
            }
            return describeGeneric(kind, area, seg, change);
        }
        return describeGeneric(kind, "Template", seg, change);
    }

    private ChangeItem describeMetadata(ChangeKind kind, List<String> seg, RawChange c) {
        String field = seg.size() > 1 ? Humanizer.words(seg.get(seg.size() - 1)) : "Template details";
        String title = field.equals("Display name") ? "Template name " + verb(kind) : field + " " + verb(kind);
        return item(c, kind, ItemType.TEMPLATE_DETAILS, "Template details", title, null, false);
    }

    private ChangeItem describeItem(ChangeKind kind, ItemType type, String area, List<String> seg, RawChange c) {
        String noun = Humanizer.words(type.name());
        String key = seg.get(3);

        if (seg.size() == 4) {
            // The whole question/checklist/procedure was added or removed.
            Object value = kind == ChangeKind.REMOVED ? c.oldValue() : c.newValue();
            Map<?, ?> obj = value instanceof Map<?, ?> m ? m : Map.of();
            String title = kind == ChangeKind.ADDED ? "New " + noun.toLowerCase() + " added" : noun + " " + verb(kind);

            List<String> details = new ArrayList<>();
            if (obj.get("label") != null) details.add("\"" + obj.get("label") + "\"");
            if (obj.get("items") instanceof List<?> steps && !steps.isEmpty()) {
                details.add("Steps: " + Humanizer.value(steps));
            }
            if (obj.get("options") instanceof List<?> options && !options.isEmpty()) {
                details.add("Options: " + Humanizer.value(options));
            }
            if (obj.get("id") != null) details.add("Reference " + obj.get("id"));

            boolean required = kind == ChangeKind.ADDED && Boolean.TRUE.equals(obj.get("required"));
            return item(c, kind, type, area, title, details.isEmpty() ? null : String.join(" | ", details), required);
        }

        // A field of an existing item changed, e.g. /questions/3/label or /procedures/2/tolerance.
        String field = seg.get(seg.size() - 1);
        String title = switch (field) {
            case "label" -> noun + " wording " + verb(kind);
            case "helpText" -> noun + " help text " + verb(kind);
            case "required" -> noun + " requirement " + verb(kind);
            default -> noun + " " + Humanizer.words(field).toLowerCase() + " " + directionalVerb(kind, c);
        };
        String detail = noun + " " + key + " in " + area;
        boolean nowRequired = field.equals("required") && Boolean.TRUE.equals(c.newValue());
        return item(c, kind, type, area, title, detail, nowRequired);
    }

    private ChangeItem describeSetting(ChangeKind kind, String area, List<String> seg, RawChange c) {
        String title = Humanizer.words(seg.get(seg.size() - 1)) + " " + directionalVerb(kind, c);
        return item(c, kind, ItemType.SETTING, area, title, null, false);
    }

    private ChangeItem describeGeneric(ChangeKind kind, String area, List<String> seg, RawChange c) {
        String last = seg.isEmpty() ? "Content" : Humanizer.words(seg.get(seg.size() - 1));
        Object value = kind == ChangeKind.REMOVED ? c.oldValue() : c.newValue();
        String detail = value instanceof Map<?, ?> ? Humanizer.value(value) : null;
        return item(c, kind, ItemType.OTHER, area, last + " " + directionalVerb(kind, c), detail, false);
    }

    private static ChangeItem item(RawChange c, ChangeKind kind, ItemType type, String area,
                                   String title, String detail, boolean requiresResponse) {
        // Whole added/removed objects are summarised in 'detail'; before/after is for scalar-ish edits.
        boolean objectValue = c.oldValue() instanceof Map<?, ?> || c.newValue() instanceof Map<?, ?>;
        String before = objectValue ? null : Humanizer.value(c.oldValue());
        String after = objectValue ? null : Humanizer.value(c.newValue());
        return new ChangeItem(kind.name().toLowerCase() + ":" + c.path(), kind, type, area, title, detail,
                before, after, List.of(), requiresResponse, c.path());
    }

    private static String verb(ChangeKind kind) {
        return switch (kind) {
            case ADDED -> "added";
            case MODIFIED -> "changed";
            case REMOVED -> "removed";
        };
    }

    private static String directionalVerb(ChangeKind kind, RawChange c) {
        return kind == ChangeKind.MODIFIED ? Humanizer.direction(c.oldValue(), c.newValue()) : verb(kind);
    }

    private static ItemType itemTypeOf(String collection) {
        return switch (collection) {
            case "questions" -> ItemType.QUESTION;
            case "checklists" -> ItemType.CHECKLIST;
            case "procedures" -> ItemType.PROCEDURE;
            default -> null;
        };
    }

    /** RFC 6901 JSON pointer -> segments ("~1" is "/", "~0" is "~"). */
    static List<String> parsePointer(String pointer) {
        List<String> out = new ArrayList<>();
        for (String raw : pointer.substring(1).split("/", -1)) {
            out.add(raw.replace("~1", "/").replace("~0", "~"));
        }
        return out;
    }
}
