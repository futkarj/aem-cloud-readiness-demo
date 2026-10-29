# Cloud-Ready AEM — 6.6 → AEMaaCS Modernization Demo

Assess and modernize an AEM 6.6 project for AEM as a Cloud Service, **locally, without a cloud sandbox**.
Write-up and talk track: https://rohit-jain-portfolio.super.site/cloud-ready-aem-without-a-cloud-sandbox

Status: in progress. The Cloud target is built and running on the local SDK; the 6.6 baseline is next.
See [docs/plan.md](docs/plan.md).

## Repo layout
```
cloud-ready/      # AEM Project Archetype 58, aemVersion=cloud (built and deployed to the local SDK)
legacy-66/        # (planned) 6.6 project with intentional legacy patterns
docs/plan.md      # Build plan and local runtime setup
docs/bpa-report/  # (planned) Best Practices Analyzer output from the sample baseline
docs/diffs.md     # (planned) Before/after explanations (feeds the demo)
```

## Prereqs
- Java 21, Maven 3.9+, Docker (for the Dispatcher tools)
- AEMaaCS SDK quickstart + Dispatcher tools, from Adobe Software Distribution (needs an entitled Adobe org)
- AEM 6.6 quickstart jar for the legacy baseline

| Instance | Port |
|---|---|
| AEMaaCS SDK author | 4502 |
| AEM 6.6 author (baseline) | 4602 |
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
Use Dispatcher tools at least as new as the archetype. Archetype 58's `default_filters.any` is newer than
Dispatcher tools 2.0.258, so the immutability check (phase 3) fails on that file with the older tools.

## Step 2: legacy 6.6 baseline
Archetype 58 accepts `aemVersion` `6.4.8.4`, `6.5.7` or `cloud`, so generate with `6.5.7` and update the API
dependency to 6.6. Deploy to the 6.6 author on 4602:
```bash
cd legacy-66 && mvn clean install -PautoInstallSinglePackage -Daem.port=4602
```
Then add legacy patterns to assess: a static template, a config under `/apps/.../config` without run modes,
a custom servlet bound by path, and content inside `/apps`.

## Step 3: run Best Practices Analyzer
Install the BPA package from Software Distribution on the 6.6 author → **Tools → Operations → Best Practices
Analyzer** → export the CSV to `docs/bpa-report/`. The report covers this sample project only.

## Step 4: modernize
- Static → editable templates: AEM Modernize Tools (https://github.com/adobe/aem-modernize-tools)
- Split code into immutable (`/apps`) and mutable (`/conf`, `/content`) packages, following the archetype's
  `ui.apps` / `ui.content` structure
- Move OSGi configs to `config.<runmode>` folders in `.cfg.json` format
- Document each change in `docs/diffs.md`, then redeploy `cloud-ready` to the SDK and re-validate the Dispatcher config
