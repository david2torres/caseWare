import { row } from "../utils/template-utils";
import { PublishedVersion, EngagementUpdateStatus, ChangeItem } from "./template-interface";


const INDEXED_AT = '2026-09-01T08:00:00Z';

const AUDIT: PublishedVersion[] = [
    { version: 3, publishedAt: '2026-05-12T13:00:00Z' },
    { version: 4, publishedAt: '2026-07-07T13:00:00Z' },
    { version: 5, publishedAt: '2026-08-18T13:00:00Z' },
];

const REVIEW: PublishedVersion[] = [
    { version: 6, publishedAt: '2026-05-20T13:00:00Z' },
    { version: 7, publishedAt: '2026-07-21T13:00:00Z' },
    { version: 8, publishedAt: '2026-08-25T13:00:00Z' },
];

const RISK: PublishedVersion[] = [
    { version: 10, publishedAt: '2026-04-28T13:00:00Z' },
    { version: 11, publishedAt: '2026-06-30T13:00:00Z' },
    { version: 12, publishedAt: '2026-08-11T13:00:00Z' },
];

export const CATALOG_AS_OF = '2026-08-25T13:10:00Z';

export const NAMES: Record<string, string> = {
    'AUDIT-CA': 'Canadian Audit Engagement',
    'REVIEW-CA': 'Canadian Review Engagement',
    'RISK-CA': 'Canadian Risk Assessment',
};

export const VERSIONS: Record<string, PublishedVersion[]> = { 'AUDIT-CA': AUDIT, 'REVIEW-CA': REVIEW, 'RISK-CA': RISK };


export const ENGAGEMENTS: EngagementUpdateStatus[] = [
    row('ENG-1001', 'Northstar Manufacturing 2026', 'AUDIT-CA', 5),
    row('ENG-1002', 'Maple Ridge Foods 2026', 'AUDIT-CA', 4),
    row('ENG-1003', 'Harbourview Logistics 2026', 'AUDIT-CA', 3),
    row('ENG-1004', 'Pinecrest Holdings 2026', 'AUDIT-CA', 5),
    row('ENG-1005', 'Cedar Peak Services 2026', 'REVIEW-CA', 8),
    row('ENG-1006', 'Westmount Consulting 2026', 'REVIEW-CA', 7),
    row('ENG-1007', 'Bluewater Hospitality 2026', 'REVIEW-CA', 6),
    row('ENG-1008', 'Summit Property Group 2026', 'REVIEW-CA', 8),
    row('ENG-1009', 'Northern Grid Energy 2026', 'RISK-CA', 12),
    row('ENG-1010', 'Greenfield Health Services 2026', 'RISK-CA', 11),
    row('ENG-1011', 'Stonebridge Construction 2026', 'RISK-CA', 10),
    {
        engagementId: 'ENG-1012',
        engagementName: 'Prairie Star Investments 2026',
        templateId: 'RISK-CA',
        templateDisplayName: NAMES['RISK-CA'],
        status: 'UNKNOWN',
        unknownReason: 'NOT_YET_INDEXED',
        currentVersion: null,
        latestVersion: 12,
        pendingVersions: [],
        activeDecision: null,
        declinedThroughVersion: null,
        statusAsOf: INDEXED_AT,
    },
];

/* ---------- Generated from the Java summary logic ---------- */
// ENG-1002 v4 -> v5
export const ITEMS_ENG_1002: ChangeItem[] = [
    {
        changeId: 'modified:/sections/planning/questions/3/label', kind: 'MODIFIED', itemType: 'QUESTION', area: 'Planning', title: 'Question wording changed',
        detail: 'Question 3 in Planning', before: 'Has management identified significant estimates?', after: 'Has management identified significant accounting estimates and related estimation uncertainty?',
        changedInVersions: [5], requiresResponse: false, technicalPath: '/sections/planning/questions/3/label'
    },
    {
        changeId: 'modified:/sections/materiality/guidance/thresholdPercent', kind: 'MODIFIED', itemType: 'SETTING', area: 'Materiality', title: 'Threshold percent decreased',
        detail: null, before: '4.5', after: '4',
        changedInVersions: [5], requiresResponse: false, technicalPath: '/sections/materiality/guidance/thresholdPercent'
    },
    {
        changeId: 'added:/sections/completion/checklists/subsequent-events', kind: 'ADDED', itemType: 'CHECKLIST', area: 'Completion', title: 'New checklist added',
        detail: '"Subsequent events review" | Steps: Confirm inquiry with management; Evaluate events requiring adjustment; Document the conclusion | Reference CHK-SE-01', before: null, after: null,
        changedInVersions: [5], requiresResponse: false, technicalPath: '/sections/completion/checklists/subsequent-events'
    },
];
// ENG-1003 v3 -> v5
export const ITEMS_ENG_1003: ChangeItem[] = [
    {
        changeId: 'added:/sections/planning/questions/7', kind: 'ADDED', itemType: 'QUESTION', area: 'Planning', title: 'New question added',
        detail: '"Were any new fraud risk factors identified during planning?" | Reference Q-PLN-007', before: null, after: null,
        changedInVersions: [4], requiresResponse: true, technicalPath: '/sections/planning/questions/7'
    },
    {
        changeId: 'modified:/sections/materiality/guidance/thresholdPercent', kind: 'MODIFIED', itemType: 'SETTING', area: 'Materiality', title: 'Threshold percent decreased',
        detail: null, before: '5', after: '4',
        changedInVersions: [4, 5], requiresResponse: false, technicalPath: '/sections/materiality/guidance/thresholdPercent'
    },
    {
        changeId: 'removed:/sections/planning/procedures/legacy-risk-confirmation', kind: 'REMOVED', itemType: 'PROCEDURE', area: 'Planning', title: 'Procedure removed',
        detail: '"Confirm legacy risk classification" | Reference PROC-PLN-004', before: null, after: null,
        changedInVersions: [4], requiresResponse: false, technicalPath: '/sections/planning/procedures/legacy-risk-confirmation'
    },
    {
        changeId: 'modified:/sections/planning/questions/3/label', kind: 'MODIFIED', itemType: 'QUESTION', area: 'Planning', title: 'Question wording changed',
        detail: 'Question 3 in Planning', before: 'Has management identified significant estimates?', after: 'Has management identified significant accounting estimates and related estimation uncertainty?',
        changedInVersions: [5], requiresResponse: false, technicalPath: '/sections/planning/questions/3/label'
    },
    {
        changeId: 'added:/sections/completion/checklists/subsequent-events', kind: 'ADDED', itemType: 'CHECKLIST', area: 'Completion', title: 'New checklist added',
        detail: '"Subsequent events review" | Steps: Confirm inquiry with management; Evaluate events requiring adjustment; Document the conclusion | Reference CHK-SE-01', before: null, after: null,
        changedInVersions: [5], requiresResponse: false, technicalPath: '/sections/completion/checklists/subsequent-events'
    },
];
// ENG-1006 v7 -> v8
export const ITEMS_ENG_1006: ChangeItem[] = [
    {
        changeId: 'modified:/metadata/displayName', kind: 'MODIFIED', itemType: 'TEMPLATE_DETAILS', area: 'Template details', title: 'Template name changed',
        detail: null, before: 'Canadian Review Engagement', after: 'Canadian Review Engagement 2026',
        changedInVersions: [8], requiresResponse: false, technicalPath: '/metadata/displayName'
    },
    {
        changeId: 'modified:/sections/analytics/procedures/2/tolerance', kind: 'MODIFIED', itemType: 'PROCEDURE', area: 'Analytics', title: 'Procedure tolerance decreased',
        detail: 'Procedure 2 in Analytics', before: '0.12', after: '0.1',
        changedInVersions: [8], requiresResponse: false, technicalPath: '/sections/analytics/procedures/2/tolerance'
    },
    {
        changeId: 'added:/sections/completion/checklists/going-concern', kind: 'ADDED', itemType: 'CHECKLIST', area: 'Completion', title: 'New checklist added',
        detail: '"Going concern evaluation" | Steps: Document management\'s assessment; Evaluate contradictory evidence; Record the practitioner\'s conclusion | Reference CHK-GC-01', before: null, after: null,
        changedInVersions: [8], requiresResponse: false, technicalPath: '/sections/completion/checklists/going-concern'
    },
];
// ENG-1007 v6 -> v8
export const ITEMS_ENG_1007: ChangeItem[] = [
    {
        changeId: 'modified:/metadata/displayName', kind: 'MODIFIED', itemType: 'TEMPLATE_DETAILS', area: 'Template details', title: 'Template name changed',
        detail: null, before: 'Canadian Review Engagement', after: 'Canadian Review Engagement 2026',
        changedInVersions: [8], requiresResponse: false, technicalPath: '/metadata/displayName'
    },
    {
        changeId: 'added:/sections/inquiries/questions/12', kind: 'ADDED', itemType: 'QUESTION', area: 'Inquiries', title: 'New question added',
        detail: '"Describe any events after the reporting date that may require adjustment or disclosure." | Reference Q-INQ-012', before: null, after: null,
        changedInVersions: [7], requiresResponse: false, technicalPath: '/sections/inquiries/questions/12'
    },
    {
        changeId: 'modified:/sections/analytics/procedures/2/tolerance', kind: 'MODIFIED', itemType: 'PROCEDURE', area: 'Analytics', title: 'Procedure tolerance decreased',
        detail: 'Procedure 2 in Analytics', before: '0.15', after: '0.1',
        changedInVersions: [7, 8], requiresResponse: false, technicalPath: '/sections/analytics/procedures/2/tolerance'
    },
    {
        changeId: 'removed:/sections/inquiries/questions/4/helpText', kind: 'REMOVED', itemType: 'QUESTION', area: 'Inquiries', title: 'Question help text removed',
        detail: 'Question 4 in Inquiries', before: 'Ask management to describe changes in accounting policies since the prior year.', after: null,
        changedInVersions: [7], requiresResponse: false, technicalPath: '/sections/inquiries/questions/4/helpText'
    },
    {
        changeId: 'added:/sections/completion/checklists/going-concern', kind: 'ADDED', itemType: 'CHECKLIST', area: 'Completion', title: 'New checklist added',
        detail: '"Going concern evaluation" | Steps: Document management\'s assessment; Evaluate contradictory evidence; Record the practitioner\'s conclusion | Reference CHK-GC-01', before: null, after: null,
        changedInVersions: [8], requiresResponse: false, technicalPath: '/sections/completion/checklists/going-concern'
    },
];
// ENG-1010 v11 -> v12
export const ITEMS_ENG_1010: ChangeItem[] = [
    {
        changeId: 'modified:/sections/riskAssessment/scoring/highRiskThreshold', kind: 'MODIFIED', itemType: 'SETTING', area: 'Risk assessment', title: 'High risk threshold decreased',
        detail: null, before: '8', after: '7',
        changedInVersions: [12], requiresResponse: false, technicalPath: '/sections/riskAssessment/scoring/highRiskThreshold'
    },
    {
        changeId: 'added:/sections/monitoring/checklists/control-changes', kind: 'ADDED', itemType: 'CHECKLIST', area: 'Monitoring', title: 'New checklist added',
        detail: '"Control changes since prior assessment" | Reference CHK-CTRL-01', before: null, after: null,
        changedInVersions: [12], requiresResponse: true, technicalPath: '/sections/monitoring/checklists/control-changes'
    },
    {
        changeId: 'modified:/sections/riskAssessment/guidance/reassessmentFrequencyMonths', kind: 'MODIFIED', itemType: 'SETTING', area: 'Risk assessment', title: 'Reassessment frequency months decreased',
        detail: null, before: '12', after: '9',
        changedInVersions: [12], requiresResponse: false, technicalPath: '/sections/riskAssessment/guidance/reassessmentFrequencyMonths'
    },
];

/** Summaries by engagement id, as the detail endpoint would return them. */
export const SUMMARY_ITEMS: Record<string, ChangeItem[]> = {
    'ENG-1002': ITEMS_ENG_1002,
    'ENG-1003': ITEMS_ENG_1003,
    'ENG-1006': ITEMS_ENG_1006,
    'ENG-1007': ITEMS_ENG_1007,
    'ENG-1010': ITEMS_ENG_1010,
};
