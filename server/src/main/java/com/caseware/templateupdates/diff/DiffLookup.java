package com.caseware.templateupdates.diff;

/**
 * Result of asking for a diff. Makes "not computed yet" and "failed" explicit instead of null/exceptions,
 * because the API contract must represent both states to the client.
 */
public sealed interface DiffLookup {
    record Ready(RawTemplateDiff diff) implements DiffLookup {}
    record NotYetComputed() implements DiffLookup {}
    record Failed(String reason) implements DiffLookup {}
}
