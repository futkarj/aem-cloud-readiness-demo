# Cloud-Ready AEM — 6.5 LTS → AEMaaCS Modernization Demo

Assess and modernize an AEM 6.5 LTS project for AEM as a Cloud Service, **locally, without a cloud sandbox**.
Write-up and talk track: https://rohit-jain-portfolio.super.site/cloud-ready-aem-without-a-cloud-sandbox

Status: in progress. The Cloud target runs on the local SDK, and the 6.5 LTS baseline has been assessed from
its code ([docs/assessment](docs/assessment/README.md)). Modernizing the flagged patterns is next.
See [docs/plan.md](docs/plan.md).

## Repo layout
```
cloud-ready/      # AEM Project Archetype 58, aemVersion=cloud (built and deployed to the local SDK)
legacy-65-lts/    # 6.5 LTS project with 15 deliberate legacy patterns (see LEGACY-PATTERNS.md)
docs/plan.md      # Build plan and local runtime setup
docs/assessment/  # Code-only readiness assessment: migration runbook, analyser results, Dispatcher conversion
docs/diffs.md     # (planned) Before/after explanations (feeds the demo)
```

## Prereqs
- Java 21, Maven 3.9+, Docker (for the Dispatcher tools)
- AEMaaCS SDK quickstart + Dispatcher tools, from Adobe Software Distribution (needs an entitled Adobe org)
- AEM 6.5 LTS quickstart for the legacy baseline. Its jar and Maven artifacts use the internal version 6.6.x:
  `cq-quickstart-6.6.0.jar`, `com.adobe.aem:uber-jar:6.6.0`

| Instance | Port |
|---|---|
| AEMaaCS SDK author | 4502 |
| AEM 6.5 LTS author (baseline) | 4602 |
| Dispatcher (Docker) | 8080 |

## Step 1: Cloud target on the local SDK
```bash
mvn -B archetype:generate -D archetypeGroupId=com.adobe.aem \
  -D archetypeArtifactId=aem-project-archetype -D archetypeVersion=58 \
  -D appTitle="Cloud Demo" -D appId=clouddemo -D groupId=com.demo.cloud \
  -D artifactId=cloud-ready -D aemVersion=cloud -D includeDispatcherConfig=y
```
Pin `aem.sdk.api` in `cloud-ready/pom.xml` to the SDK version you run locally (here
`2026.1.23963.20260108T001551Z-251200`), then build with the Cloud Manager build analysers and deploy:
```bash
cd cloud-ready && mvn clean install -PautoInstallSinglePackage
```
Validate the Dispatcher config with the SDK's Dispatcher tools:
```bash
IMMUTABLE_FILES_UPDATE=false ./bin/validate.sh <repo>/cloud-ready/dispatcher/src
```
Keep the Adobe-managed (immutable) files in step with your Dispatcher tools: run `./bin/update_maven.sh
<repo>/cloud-ready/dispatcher/src` after upgrading the tools. Archetype 58 ships some immutable files newer and
some older than any single tools release; with tools 2.0.275 plus `update_maven.sh`, all three phases pass.

## Step 2: legacy 6.5 LTS baseline
Archetype 58 accepts `aemVersion` `6.4.8.4`, `6.5.7` or `cloud`, so generate with `6.5.7` and update the API
dependency to `uber-jar:6.6.0:apis` (6.5 LTS). Deploy to the 6.5 LTS author on 4602:
```bash
cd legacy-65-lts && mvn clean install -PautoInstallSinglePackage -Daem.port=4602
```
The legacy patterns to assess are listed in [legacy-65-lts/LEGACY-PATTERNS.md](legacy-65-lts/LEGACY-PATTERNS.md).

## Step 3: assess
From the code, no instance needed: Adobe's `aem-cloud-service` skills (`/plugin marketplace add adobe/skills`)
for the migration runbook and code assessment, the AEM Analyser on the built `all` package, and the Dispatcher
Converter. Results: [docs/assessment/](docs/assessment/README.md).

With a licensed 6.5 LTS instance, add Best Practices Analyzer (**Tools → Operations → Best Practices
Analyzer**), which also covers the repository-level patterns the code tools miss.

## Step 4: modernize
- Static → editable templates: AEM Modernize Tools (https://github.com/adobe/aem-modernize-tools)
- Split code into immutable (`/apps`) and mutable (`/conf`, `/content`) packages, following the archetype's
  `ui.apps` / `ui.content` structure
- Move OSGi configs to `config.<runmode>` folders in `.cfg.json` format
- Document each change in `docs/diffs.md`, then redeploy `cloud-ready` to the SDK and re-validate the Dispatcher config
