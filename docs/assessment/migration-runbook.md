# AEM Cloud Service — Migration Runbook

> **Read-only planning document.** It lists the migration findings discovered
> across your project. Use a pattern's sample prompt to start the
> one-pattern-per-session apply workflow. No code was changed to produce this.

## Summary

| Field | Value |
|-------|-------|
| Generated | 2026-10-29 01:46:58 UTC |
| Total findings | **8** |
| Patterns with findings | **7** of 15 |

### Findings by pattern

| Pattern | Severity | Findings | Detected via |
|---------|----------|----------|--------------|
| Scheduler | high | — | analyzer |
| Resource Change Listener | high | **1** | analyzer |
| Event Migration (EventListener / EventHandler) | high | **2** | analyzer |
| Asset Manager API | high | **1** | analyzer |
| Replication | high | **1** | analyzer |
| HTL data-sly-test Lint | medium | — | HTL scan |
| OSGi → Cloud Manager Config | high | — | config scan |
| Vault Package Dependencies | high | — | pom-scan |
| Classic UI / Coral 2 Dialogs (LUI) | high | — | content scan |
| Custom Classic Widgets (CDW) | high | — | content scan |
| Static → Editable Templates | medium | **1** | content scan |
| Guava Cache → Caffeine | info | — | LLM scan |
| java.io.InputStream Usage | medium | — | Java source scan |
| Dispatcher AMS/On-prem → AEMaaCS Conversion | high | **1** | content scan |
| Oak Index Migration | high | **1** | LLM scan |

---

## Resource Change Listener (`resourceChangeListener`)

**Severity:** high | **Findings:** 1 | **Detected via:** analyzer

Classes implementing `ResourceChangeListener` / `ExternalResourceChangeListener`. Migrate to a lightweight `ResourceChangeListener` + `JobConsumer` split.

### Affected

| Location | Detail | Severity |
|----------|--------|----------|
| `core/src/main/java/com/demo/legacy/core/listeners/SimpleResourceListener.java:33` | `@Component(service = ResourceChangeListener.class, immediate = true ) @ServiceDescription("Demo to listen on changes in…` | high |

### Sample prompt

```
Use the migration skill: **resourceChangeListener** only.
```

---

## Event Migration (EventListener / EventHandler) (`event-migration`)

**Severity:** high | **Findings:** 2 | **Detected via:** analyzer

Classes implementing JCR `javax.jcr.observation.EventListener` or OSGi `org.osgi.service.event.EventHandler`. Migrate to a lightweight handler + `JobConsumer` split, with `TopologyEventListener` for leader-only work.

### Affected

| Location | Detail | Severity |
|----------|--------|----------|
| `core/src/main/java/com/demo/legacy/core/legacy/JcrPageEventListener.java:21` | `@Component(immediate = true) public class JcrPageEventListener implements EventListener` | high |
| `core/src/main/java/com/demo/legacy/core/legacy/ReplicationEventHandler.java:16` | `@Component(service = EventHandler.class, immediate = true, property = EventConstants.EVENT_TOPIC + "=" + ReplicationAct…` | high |

### Sample prompt

```
Use the migration skill: **eventListener** only.
```

---

## Asset Manager API (`assetApi`)

**Severity:** high | **Findings:** 1 | **Detected via:** analyzer

Calls to `com.day.cq.dam.api.AssetManager` create/upload/delete methods not available on Cloud Service. Migrate to Direct Binary Access and in-JVM `resolver.delete()`.

### Affected

| Location | Detail | Severity |
|----------|--------|----------|
| `core/src/main/java/com/demo/legacy/core/legacy/DamAssetImporter.java:23` | `assetManager.createAsset(path, binary, mimeType, true)` | high |

### Sample prompt

```
Use the migration skill: **assetApi** only.
```

---

## Replication (`replication`)

**Severity:** high | **Findings:** 1 | **Detected via:** analyzer

Usage of `com.day.cq.replication.Replicator` / Sling Replication Agent. Migrate to the Sling Distribution API (`Distributor` + `SimpleDistributionRequest`).

### Affected

| Location | Detail | Severity |
|----------|--------|----------|
| `core/src/main/java/com/demo/legacy/core/legacy/PageActivationService.java:18` | `@Component(service = PageActivationService.class) public class PageActivationService` | high |

### Sample prompt

```
Use the migration skill: **replication** only.
```

---

## Static → Editable Templates (`templateModernization`)

**Severity:** medium | **Findings:** 1 | **Detected via:** content scan

Static templates under `apps/<appId>/templates/*` (`cq:Template`) that should become editable templates. Detected heuristically by globbing template `.content.xml`; the template modernization pipeline (Branch C) runs per-template context → execute → validate.

> ⚠️ **Heuristic detection** (content scan) — these are candidate matches, not compiler/BPA-validated findings. Re-confirm each one before editing.

### Affected

| Location | Detail | Severity |
|----------|--------|----------|
| `ui.apps/src/main/content/jcr_root/apps/legacydemo/templates/legacy-page/.content.xml` | `custom static-template 'legacy-page' — modernize to an editable template (Branch C)` | medium |

### Sample prompt

```
Use the migration skill: migrate my static templates to editable templates and generate the AEM Modernize Tools rewrite rules.
```

---

## java.io.InputStream Usage (`inputStream`)

**Severity:** medium | **Findings:** 0 (+1 informational) | **Detected via:** Java source scan

Custom Java code using `java.io.*InputStream` (BPA `DG` / `java.io.inputstream`, ADVISORY). Adobe documents the risk as binary data streamed through the JVM, with direct binary upload and CDN delivery as the fix. BPA findings come first (CAM, then a BPA CSV): the flagged classes are resolved to workspace files; the whole workspace is scanned only when BPA has no inputStream findings. One finding per file, ordered by risk. Each stream site is pre-classified: **F** `ObjectInputStream` deserialization, **E** binary streamed or processed in the JVM (DAM binary reads such as `asset.getOriginal().getStream()`, binaries proxied to responses, client uploads into the repository, native processes), **D** local-filesystem reads, **C** unbounded in-memory reads, **B** unclosed stream or undisposed JCR `Binary` (hygiene), **A** benign (informational). Fix kinds: `decision` (needs the user), `review` (confirm a read limit), `auto` (try-with-resources). BPA rows for classes outside the workspace collapse to one informational row per bundle: customer code kept elsewhere, platform-provided (AEM Forms add-on) or third-party.

> ⚠️ **Heuristic detection** (Java source scan) — these are candidate matches, not compiler/BPA-validated findings. Re-confirm each one before editing.

### Affected

| Location | Detail | Severity |
|----------|--------|----------|
| `core/src/main/java/com/demo/legacy/core/legacy/DamAssetImporter.java:18` | `benign (1 site): java.io stream type used only as a parameter/return/field type` | info |

### Sample prompt

```
Use the migration skill: fix inputStream (java.io.InputStream) findings — propose CDN delivery and direct binary upload where binaries pass through the JVM, and fix unclosed streams.
```

---

## Dispatcher AMS/On-prem → AEMaaCS Conversion (`dispatcherConversion`)

**Severity:** high | **Findings:** 1 | **Detected via:** content scan

An AMS or on-premise Dispatcher configuration convertible to AEM as a Cloud Service (Branch E). Detected heuristically; the conversion wraps Adobe's aem-cs-source-migration dispatcher-converter with mode detection, config generation, output verification, and validation.

> ⚠️ **Heuristic detection** (content scan) — these are candidate matches, not compiler/BPA-validated findings. Re-confirm each one before editing.

### Affected

| Location | Detail | Severity |
|----------|--------|----------|
| `.` | `Dispatcher config (ams) — 27 filter / 10 rewrite rules → convertible (Branch E)` | high |

### Sample prompt

```
Use the migration skill: convert my dispatcher configuration to AEM as a Cloud Service.
```

---

## Oak Index Migration (`oakIndex`)

**Severity:** high | **Findings:** 1 | **Detected via:** LLM scan

Legacy `_oak_index/*.xml` definitions (BPA subtypes `index.rule.violation` / `standard.index.modification`, category OID) convertible to AEM as a Cloud Service Oak index definitions (Branch F, beta). BPA is the sole source of truth — one finding per Oak index JCR path; there is no analyzer or content-scan fallback. Wraps Adobe's aem-cs-source-migration index-converter tool rather than re-implementing transformation rules.

### Affected

| Location | Detail | Severity |
|----------|--------|----------|
| `ui.apps/src/main/content/jcr_root/_oak_index/legacydemoLucene/.content.xml:3` | `jcr:primaryType="oak:QueryIndexDefinition" (custom Lucene index, no -custom-N version suffix)` | high |

### Sample prompt

```
Use the migration skill: fix oakIndex findings using BPA CSV at ./reports/bpa.csv.
```

---

## Scanned — no findings

- **Scheduler** (`scheduler`)
- **HTL data-sly-test Lint** (`htlLint`)
- **OSGi → Cloud Manager Config** (`osgiConfig`)
- **Vault Package Dependencies** (`vault-package-dependencies`)
- **Classic UI / Coral 2 Dialogs (LUI)** (`lui`)
- **Custom Classic Widgets (CDW)** (`cdw`)
- **Guava Cache → Caffeine** (`guavaCache`)

## Scan warnings

- deprecated-api-rules-missing: expected TSV at /var/folders/d5/8c8b4b8j1g15q4t1s746ssfw0000gq/T/aem-code-assessment/deprecated-api-rules.tsv — run remove-deprecated-api/scripts/detect.sh preflight first
- XML sling:OsgiConfig nodes are not scanned by this detector — review .content.xml config nodes manually.

---

*Generated by the AEM Cloud Service migration skill — runbook-generator.js*