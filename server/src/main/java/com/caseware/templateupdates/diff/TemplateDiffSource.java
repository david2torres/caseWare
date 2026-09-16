package com.caseware.templateupdates.diff;

/**
 * Port over the shared diff cache (keyed by templateId + fromVersion + toVersion).
 * Consecutive pairs are precomputed on publish; other pairs are computed on first request.
 */
public interface TemplateDiffSource {
    DiffLookup find(String templateId, int fromVersion, int toVersion);
}
