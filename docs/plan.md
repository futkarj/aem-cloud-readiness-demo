# Build plan

Working plan for the demo. The README describes the finished demo; this file tracks how we get there.

## Local runtimes
Everything runs on one laptop, outside any cloud sync folder, under `~/dev/containers/aem-sdk/`:

| Runtime | Where | Port | Status |
|---|---|---|---|
| AEMaaCS SDK author (2026.1.23963) | `author/` | 4502 | Running; `cloud-ready` deployed |
| Dispatcher tools 2.0.275 (Docker) | `dispatcher/dispatcher-sdk-2.0.275/` | 8080 | `cloud-ready` passes all 3 phases |
| AEM 6.5 LTS baseline author | `aem-65-lts/` (planned) | 4602 | Not installed: needs a license |

```bash
# Cloud SDK author (4 GB heap; the start script's default 1 GB is too small)
cd ~/dev/containers/aem-sdk/author
CQ_JVM_OPTS='-server -Xmx4g -Djava.awt.headless=true' ./crx-quickstart/bin/start
./crx-quickstart/bin/stop

# Dispatcher: validate a project's config, then run it in front of the SDK author
cd ~/dev/containers/aem-sdk/dispatcher/dispatcher-sdk-2.0.275
IMMUTABLE_FILES_UPDATE=false ./bin/validate.sh <project>/dispatcher/src   # without the variable, a mismatch loops on a yes/no prompt
./bin/update_maven.sh <project>/dispatcher/src                            # sync immutable files to this tools version
./bin/docker_run.sh <project>/dispatcher/src host.docker.internal:4502 8080
```

## Phases
1. **Cloud target first (done).** `cloud-ready/` is generated from AEM Project Archetype 58 (`aemVersion=cloud`),
   built with the Cloud Manager build analysers (`aemanalyser-maven-plugin`), and deployed to the SDK author:
   the core bundle is active and `/content/clouddemo/us/en.html` renders.
   - `aem.sdk.api` is pinned to `2026.1.23963.20260108T001551Z-251200` to match the local SDK runtime. The
     archetype defaults to the newest API (2026.10), which the build flags as "outdated" on 2026.1; that
     warning is expected until the local SDK is upgraded.
2. **Legacy baseline (code done, runtime blocked).** `legacy-65-lts/` is generated from archetype 58 with
   `aemVersion=6.5.7` (it has no LTS option), moved to `com.adobe.aem:uber-jar:6.6.0:apis`, `aem.port=4602`.
   AEM 6.5 LTS carries the internal version 6.6.x: Software Distribution's "Download AEM 6.5 LTS" serves
   `cq-quickstart-6.6.0.jar`, and `uber-jar` 6.6.1 to 6.6.3 line up with LTS SP1 to SP3.
   It carries 15 deliberate legacy patterns, listed in [legacy-65-lts/LEGACY-PATTERNS.md](../legacy-65-lts/LEGACY-PATTERNS.md),
   and builds offline.
   - The modern archetype's package validators reject some legacy layouts outright (OSGi configs in an
     `application` package, `/oak:index` in a `content` package), so those patterns sit where the build
     allows them: XML config in `ui.config`, the unversioned index in `ui.apps`.
   - Installing AEM 6.5 LTS needs a license (customer name + download ID from Software Distribution).
     The `license.properties` files in the installer folders are the Cloud SDK's generic ones, which are
     not for 6.5 LTS.
3. **Assess (done, from code).** No 6.5 LTS license, so no running instance and no BPA. Instead: Adobe's
   `aem-cloud-service` skills (migration runbook, code-assessment), the build analysers on the legacy `all`
   package, and the Dispatcher Converter. Results and a coverage score against the 15 planted patterns are in
   [docs/assessment/](assessment/README.md). BPA stays optional, if a license turns up.
4. **Modernize.** Port each flagged pattern into `cloud-ready/`, documenting each change in `docs/diffs.md`.
   Deploy to the SDK and re-validate the Dispatcher config.
5. **Move content.** Content Transfer Tool (`content-transfer.all-3.0.58.zip`) from the baseline. Ingestion
   needs a Cloud Service target, so locally we show extraction setup and explain ingestion with Cloud
   Acceleration Manager.

## Open questions
- 6.5 LTS license: none available, so the baseline never runs. BPA and the Content Transfer Tool need a
  running instance; the demo covers them with the talk track instead.
- BPA 2.1.58 is downloaded; confirm it supports 6.5 LTS when it is installed.
- 6.5 LTS SP3: install on top of the first release (6.6.0) for a realistic, patched baseline.
- Content Transfer Tool 3.0.58: confirm it supports 6.5 LTS as a source.
- Java: 6.5 LTS supports Java 17 and 21, so the machine's Java 21 should do.
