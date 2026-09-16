# Template updates core (Java 21, Part 2)

Plain Java domain + logic, no framework. JUnit 5 tests.

```bash
mvn test
```

| Package | Contents |
|---|---|
| `catalog` | `TemplateCatalogEntry`, `TemplateVersion`, `TemplateCatalog` port |
| `engagement` | `EngagementTemplateBaseline` (index row), `PendingUpdateEvaluator` → `PendingUpdateState` |
| `diff` | Raw diff model (`RawChange`, `RawTemplateDiff`), `DiffLookup` (Ready / NotYetComputed / Failed), `TemplateDiffSource` port |
| `summary` | `ChangeDescriber` (raw change → human-readable `ChangeItem`), `PendingUpdateSummaryService` (net summary + version attribution) |

Tests: `PendingUpdateEvaluatorTest`, `PendingUpdateSummaryServiceTest`, with `SampleData` transcribed from the fixture pack.
