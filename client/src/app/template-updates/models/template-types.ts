import { ActiveDecision } from "./template-interface";

export type UpdateStatus = 'UP_TO_DATE' | 'UPDATE_AVAILABLE' | 'UNKNOWN';
export type UnknownReason = 'NOT_YET_INDEXED' | 'TEMPLATE_NOT_IN_CATALOG' | 'CATALOG_BEHIND';

export type SummaryAvailability = 'READY' | 'COMPUTING' | 'UNAVAILABLE';
export type ChangeKind = 'ADDED' | 'MODIFIED' | 'REMOVED';
export type ItemType = 'QUESTION' | 'CHECKLIST' | 'PROCEDURE' | 'SETTING' | 'TEMPLATE_DETAILS' | 'OTHER';

export type Decision = 'APPLY' | 'DECLINE';



/** 202 Accepted */
export type DecisionAccepted = ActiveDecision;


export type LoadState = 'idle' | 'loading' | 'loaded' | 'error';