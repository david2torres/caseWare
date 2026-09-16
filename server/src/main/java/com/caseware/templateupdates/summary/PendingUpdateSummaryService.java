package com.caseware.templateupdates.summary;

import com.caseware.templateupdates.diff.DiffLookup;
import com.caseware.templateupdates.diff.RawChange;
import com.caseware.templateupdates.diff.RawTemplateDiff;
import com.caseware.templateupdates.diff.TemplateDiffSource;
import com.caseware.templateupdates.engagement.PendingUpdateState;
import com.caseware.templateupdates.engagement.UpdateStatus;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builds the human-readable summary for an engagement's pending update, including accumulated versions.
 *
 * Strategy for accumulation (v6 with v7 and v8 published):
 *  1. The NET changes come from ONE diff baseline -> latest (v6 -> v8). That is exactly what "Apply" does,
 *     and it naturally hides intermediate churn (0.15 -> 0.12 -> 0.10 is shown as 0.15 -> 0.10).
 *     We do NOT compose consecutive diffs ourselves: the diff tool is "quick and reliable", composition is not.
 *  2. Consecutive diffs (v6->v7, v7->v8) are used ONLY to annotate each net item with the versions that touched it.
 *     If one of them is missing we still return the summary, just without that attribution.
 */
public final class PendingUpdateSummaryService {

    private final TemplateDiffSource diffs;
    private final ChangeDescriber describer;
    private final Clock clock;

    public PendingUpdateSummaryService(TemplateDiffSource diffs, ChangeDescriber describer, Clock clock) {
        this.diffs = Objects.requireNonNull(diffs);
        this.describer = Objects.requireNonNull(describer);
        this.clock = Objects.requireNonNull(clock);
    }

    public ChangeSummary summarize(PendingUpdateState state) {
        if (state.status() != UpdateStatus.UPDATE_AVAILABLE) {
            throw new IllegalArgumentException("summary requested for an engagement without a pending update");
        }
        String templateId = state.templateId();
        int from = state.currentVersion();
        int to = state.targetVersion();

        RawTemplateDiff net;
        switch (diffs.find(templateId, from, to)) {
            case DiffLookup.Ready ready -> net = ready.diff();
            case DiffLookup.NotYetComputed ignored -> { return ChangeSummary.computing(from, to); }
            case DiffLookup.Failed failed -> { return ChangeSummary.unavailable(from, to, failed.reason()); }
        }

        List<RawTemplateDiff> steps = consecutiveSteps(state);
        List<ChangeItem> items = new ArrayList<>();
        for (RawChange change : net.changes()) {
            ChangeItem item = describer.describe(templateId, to, change);
            items.add(item.withChangedInVersions(versionsTouching(change.path(), steps)));
        }
        return ChangeSummary.ready(from, to, items, clock.instant());
    }

    private List<RawTemplateDiff> consecutiveSteps(PendingUpdateState state) {
        List<RawTemplateDiff> steps = new ArrayList<>();
        int previous = state.currentVersion();
        for (var pending : state.pendingVersions()) {
            if (diffs.find(state.templateId(), previous, pending.version()) instanceof DiffLookup.Ready r) {
                steps.add(r.diff());
            }
            previous = pending.version();
        }
        return steps;
    }

    /** A step "touches" a net change if it changed the same path, a parent of it, or something inside it. */
    static List<Integer> versionsTouching(String netPath, List<RawTemplateDiff> steps) {
        List<Integer> versions = new ArrayList<>();
        for (RawTemplateDiff step : steps) {
            boolean touched = step.changes().stream().anyMatch(c -> related(c.path(), netPath));
            if (touched) versions.add(step.toVersion());
        }
        // Empty list = attribution unknown (step diffs missing). The client then simply omits "changed in vX".
        return versions;
    }

    private static boolean related(String a, String b) {
        return a.equals(b) || a.startsWith(b + "/") || b.startsWith(a + "/");
    }
}
