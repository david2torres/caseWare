package com.caseware.templateupdates;

import com.caseware.templateupdates.engagement.EngagementTemplateBaseline;
import com.caseware.templateupdates.engagement.PendingUpdateEvaluator;
import com.caseware.templateupdates.engagement.PendingUpdateState;
import com.caseware.templateupdates.engagement.UpdateStatus;
import com.caseware.templateupdates.summary.ChangeDescriber;
import com.caseware.templateupdates.summary.ChangeItem;
import com.caseware.templateupdates.summary.ChangeSummary;
import com.caseware.templateupdates.summary.PendingUpdateSummaryService;
import com.caseware.templateupdates.summary.TemplateVocabulary;

import java.time.Clock;
import java.util.List;

/** Demo only: prints what the server would answer for a few engagements. Not part of the logic. */
public class DemoMain {
    public static void main(String[] args) {
        // 1. "Database" of engagements (in real life: the per-firm index table)
        List<EngagementTemplateBaseline> index = List.of(
                EngagementTemplateBaseline.indexed("ENG-1005", "Cedar Peak Services 2026", "REVIEW-CA", 8, SampleData.INDEXED_AT),
                EngagementTemplateBaseline.indexed("ENG-1006", "Westmount Consulting 2026", "REVIEW-CA", 7, SampleData.INDEXED_AT),
                EngagementTemplateBaseline.indexed("ENG-1007", "Bluewater Hospitality 2026", "REVIEW-CA", 6, SampleData.INDEXED_AT));

        // 2. Logic wired with in-memory data (in real life: catalog table and diff cache)
        var evaluator = new PendingUpdateEvaluator(SampleData.catalog());
        var diffs = new SampleData.InMemoryDiffSource()
                .with(SampleData.reviewV6toV7()).with(SampleData.reviewV7toV8()).with(SampleData.reviewV6toV8());
        var summaries = new PendingUpdateSummaryService(diffs, new ChangeDescriber(TemplateVocabulary.NONE), Clock.systemUTC());

        // 3. What GET /template-updates and GET /pending would return
        for (var engagement : index) {
            PendingUpdateState state = evaluator.evaluate(engagement, SampleData.CATALOG_AS_OF);
            System.out.printf("%n%s (%s v%d) -> %s, pending %s%n", engagement.engagementName(), engagement.templateId(),
                    engagement.baselineVersion(), state.status(),
                    state.pendingVersions().stream().map(v -> "v" + v.version()).toList());

            if (state.status() == UpdateStatus.UPDATE_AVAILABLE) {
                ChangeSummary summary = summaries.summarize(state);
                for (ChangeItem item : summary.items()) {
                    System.out.printf("   [%s] %s: %s  (before=%s, after=%s, changed in %s)%n",
                            item.kind(), item.area(), item.title(), item.before(), item.after(), item.changedInVersions());
                }
            }
        }
    }
}
