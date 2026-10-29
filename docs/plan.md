# Build plan

Working plan for the demo. The README describes the finished demo; this file tracks how we get there.

## Local runtimes
Everything runs on one laptop, outside any cloud sync folder, under `~/dev/containers/aem-sdk/`:

| Runtime | Where | Port | Status |
|---|---|---|---|
| AEMaaCS SDK author (2026.1.23963) | `author/` | 4502 | Running; `cloud-ready` deployed |
| Dispatcher tools 2.0.258 (Docker) | `dispatcher/dispatcher-sdk-2.0.258/` | 8080 | Validator passes on the sample config |
| AEM 6.6 baseline author | `aem-66/` (planned) | 4602 | Not installed |

```bash
# Cloud SDK author (4 GB heap; the start script's default 1 GB is too small)
cd ~/dev/containers/aem-sdk/author
CQ_JVM_OPTS='-server -Xmx4g -Djava.awt.headless=true' ./crx-quickstart/bin/start
./crx-quickstart/bin/stop

# Dispatcher: validate a project's config, then run it in front of the SDK author
cd ~/dev/containers/aem-sdk/dispatcher/dispatcher-sdk-2.0.258
./bin/validate.sh <project>/dispatcher/src
./bin/docker_run.sh <project>/dispatcher/src host.docker.internal:4502 8080
```

## Phases
1. **Cloud target first (done).** `cloud-ready/` is generated from AEM Project Archetype 58 (`aemVersion=cloud`),
   built with the Cloud Manager build analysers (`aemanalyser-maven-plugin`), and deployed to the SDK author:
   the core bundle is active and `/content/clouddemo/us/en.html` renders.
   - `aem.sdk.api` is pinned to `2026.1.23963.20260108T001551Z-251200` to match the local SDK runtime. The
     archetype defaults to the newest API (2026.10), which the build flags as "outdated" on 2026.1; that
     warning is expected until the local SDK is upgraded.
2. **Legacy baseline.** Install AEM 6.6 (`cq-quickstart-6.6.0.jar`) on 4602 and generate `legacy-66/`. Archetype 58
   only accepts `aemVersion` `6.4.8.4`, `6.5.7` or `cloud`, so generate with `6.5.7` and move the API
   dependency to 6.6 by hand. Then add the legacy patterns from the README (static template, configs
   without run modes, path-bound servlet, content in `/apps`, custom replication agent).
3. **Assess.** Run Best Practices Analyzer on the 6.6 baseline and export the CSV to `docs/bpa-report/`. The
   report comes from this sample project only; no customer data. Turn the findings into an effort estimate.
   Back it up with the same build analysers on the legacy packages, and with the open-source migration
   tools (Repository Modernizer, Dispatcher Converter, Index Converter).
4. **Modernize.** Port each flagged pattern into `cloud-ready/`, documenting each change in `docs/diffs.md`.
   Deploy to the SDK and re-validate the Dispatcher config.
5. **Move content.** Content Transfer Tool (`content-transfer.all-3.0.58.zip`) from the baseline. Ingestion
   needs a Cloud Service target, so locally we show extraction setup and explain ingestion with Cloud
   Acceleration Manager.

## Open questions
- BPA package: not in the local installers folder. Download it from Software Distribution and confirm it
  supports 6.6.
- Content Transfer Tool 3.0.58: confirm it supports 6.6 as a source.
- Java: confirm 6.6 runs on the machine's Java 21; otherwise install Java 17 alongside.
