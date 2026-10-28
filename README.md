# Cloud-Ready AEM — 6.5 → AEMaaCS Modernization Demo

Assess and modernize an AEM 6.5 project for AEM as a Cloud Service, **locally, without a cloud sandbox**.
Write-up and talk track: https://rohit-jain-portfolio.super.site/cloud-ready-aem-without-a-cloud-sandbox

## Repo layout (planned)
```
legacy-65/        # Archetype project for 6.5, with intentional legacy patterns
cloud-ready/      # Same features, regenerated for Cloud + refactored
docs/bpa-report/  # Best Practices Analyzer output + screenshots
docs/diffs.md     # Before/after explanations (feeds the demo)
```

## Prereqs
- Java 11 (6.5 SP) / Java 21 (Cloud SDK), Maven 3.9+
- Local AEM 6.5 author on `localhost:4502`
- *(Optional)* AEMaaCS SDK + Dispatcher tools from Adobe Software Distribution (needs an entitled Adobe org)

## Step 1: generate both projects
Check the latest archetype version first: https://github.com/adobe/aem-project-archetype
```bash
# Legacy 6.5 baseline
mvn -B archetype:generate -D archetypeGroupId=com.adobe.aem \
  -D archetypeArtifactId=aem-project-archetype -D archetypeVersion=YOUR-VERSION \
  -D appTitle="Legacy Demo" -D appId=legacydemo -D groupId=com.demo.legacy \
  -D aemVersion=6.5.0 -D outputDirectory=legacy-65  # check archetype docs for exact 6.5 value

# Cloud-ready target
mvn -B archetype:generate -D archetypeGroupId=com.adobe.aem \
  -D archetypeArtifactId=aem-project-archetype -D archetypeVersion=YOUR-VERSION \
  -D appTitle="Cloud Demo" -D appId=clouddemo -D groupId=com.demo.cloud \
  -D aemVersion=cloud
```

## Step 2: deploy legacy to local 6.5
```bash
cd legacy-65 && mvn clean install -PautoInstallSinglePackage
```
Then add legacy patterns to assess: a static template, a config under `/apps/.../config` without run modes, a custom servlet bound by path, and content inside `/apps`.

## Step 3: run Best Practices Analyzer
Install the BPA package from Software Distribution on 6.5 → **Tools → Operations → Best Practices Analyzer** → export the CSV to `docs/bpa-report/`.

## Step 4: modernize
- Static → editable templates: AEM Modernize Tools (https://github.com/adobe/aem-modernize-tools)
- Split code into immutable (`/apps`) and mutable (`/conf`, `/content`) packages, following the archetype's `ui.apps` / `ui.content` structure
- Move OSGi configs to `config.<runmode>` folders in `.cfg.json` format
- Document each change in `docs/diffs.md`

## Step 5 (optional, requires SDK access)
```bash
java -jar aem-sdk-quickstart-*.jar -r author -p 4503   # avoid a port clash with 6.5
cd cloud-ready && mvn clean install -PautoInstallSinglePackage -Daem.port=4503
# Dispatcher: ./bin/validate.sh ./dispatcher/src
```
