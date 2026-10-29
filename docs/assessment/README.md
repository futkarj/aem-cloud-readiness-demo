# Cloud readiness assessment: legacy-65-lts

An assessment of the AEM 6.5 LTS baseline (`legacy-65-lts/`) for AEM as a Cloud Service, run **from the code
only**: no running 6.5 LTS instance, no Best Practices Analyzer (BPA) report, no Cloud Acceleration Manager.
Run on 2026-10-28.

Because the baseline contains 15 known, deliberate legacy patterns
([LEGACY-PATTERNS.md](../../legacy-65-lts/LEGACY-PATTERNS.md)), this also measures what each tool catches.

## Tools

| Tool | Version | What it looked at |
|---|---|---|
| Adobe `aem-cloud-service` skills: `migration` runbook | adobe/skills, Oct 2026 | Java, HTL, OSGi configs, poms, content XML, Dispatcher layout |
| Adobe `aem-cloud-service` skills: `code-assessment` analyzer | same | Java and Maven |
| AEM Analyser Maven Plugin (the Cloud Manager build analysers) | 1.7.8, SDK 2026.10.29023 | The built `all` package |
| Adobe Dispatcher Converter, via the `migration` skill (Branch E) | as pinned by the skill | `legacy-65-lts/dispatcher` |
| Dispatcher SDK validator | 2.0.275 | The converted Dispatcher config |

Outputs in this folder: [migration-runbook.md](migration-runbook.md) (with its findings cache
`migration-runbook.json`) and [dispatcher-conversion/](dispatcher-conversion/).

## Coverage of the 15 planted patterns

| # | Pattern | Caught by | Notes |
|---|---|---|---|
| 1 | Path-bound servlet (`/bin/...`) | Not flagged | |
| 2 | Administrative login | Not flagged | Not even by the build analysers: the methods are deprecated, but their packages are still exported |
| 3 | Unbounded query | code-assessment (`tuning-oak-query-indexes`) | Flagged as an index-coverage problem rather than as unbounded |
| 4 | Replication API with an agent filter | migration runbook, code-assessment | |
| 5 | `AssetManager.createAsset` | migration runbook, code-assessment | |
| 6 | JCR `EventListener` | migration runbook, code-assessment | |
| 7 | OSGi `EventHandler` doing work | migration runbook, code-assessment | |
| 8 | Sling Commons Scheduler `Runnable` | Not flagged | The archetype's own scheduler is the Cloud-tolerated whiteboard style |
| 9 | Static template | migration runbook (content scan) | |
| 10 | Classic UI (ExtJS) dialog | **Missed** | The content scan reads only `.content.xml`; this dialog is serialized as `dialog.xml`, which FileVault also allows and many real codebases use |
| 11 | `sling:OsgiConfig` XML without run modes | Not flagged | The config scan looks for secrets, not format or run modes |
| 12 | Mutable data in `/apps` | Not flagged | |
| 13 | Custom replication agent under `/etc` | Not flagged | BPA reports these from the repository (`replication.agent`) |
| 14 | Classic design under `/etc/designs` | Not flagged | |
| 15 | Unversioned custom Oak index | migration runbook (skill-guided manual scan) | Without BPA, the skill asks the agent to locate `_oak_index` definitions by hand |

**7 of 15 caught** (one partially), all with local tools and no AEM instance. The code-level patterns
(Java APIs) are well covered. Repository-level patterns (`/apps`, `/etc`, configs, dialogs) mostly need BPA,
which inspects a running repository.

Also reported, beyond the planted list:
- `SimpleResourceListener` (from the archetype): a `ResourceChangeListener` the runbook asks to review.
- Mockito 4.1.0 in the parent pom (outdated).

## The build analysers pass

`aemanalyser-maven-plugin` 1.7.8 against SDK 2026.10 ran every Cloud Manager analyser task on the legacy
`all` package and **passed**. Its only warnings came from Adobe's Core Components and one log-format config.
A green Cloud Manager build does not mean the code is ready: admin logins, replication agents and JCR
observation all compile and package fine, then fail at runtime on Cloud Service.

## Dispatcher conversion

Adobe's converter ran on the AMS v2.0 config (`standard` mode, 27 filter rules, 10 rewrites) and reported
success. The skill's verification step then **hard-stopped on lost filter rules**:

- The converter deleted `ams_publish_filters.any` (the site's 19 publish filter rules) but left the farm
  including it. The output kept only Adobe's default filters.
- Farm includes for client headers and virtual hosts pointed at files the converter had renamed.
- The project's own vhost kept an IP-allowlist include, an absolute proxy include, an empty force-SSL block,
  and a rewrites path Cloud Service rejects.

Fixes applied to the converted copy (the legacy config itself is unchanged):

| Issue | Fix |
|---|---|
| Lost filter rules | `filters.any` = Adobe defaults + the 2 source rules the defaults lack (`model.json`, 3D formats from DAM). 13 rules were already covered; 2 AMS-specific denies (`/content/ams/healthcheck`, `regent.html`) were dropped |
| Broken farm includes | Pointed at the consolidated `clientheaders.any` and `virtualhosts.any` |
| IP allowlist include | Removed. On Cloud Service, IP allowlisting is configured in Cloud Manager |
| Absolute proxy include, empty force-SSL block | Removed. TLS and redirects happen at the Adobe CDN |
| Rewrites include path | `Include conf.d/rewrites/rewrite.rules` |
| Duplicate stock AMS publish vhost | Disabled; the project vhost gets a `"*"` alias, as in `cloud-ready` |
| `LogLevel` overrides | Removed (not allowed on Cloud Service) |
| `security.conf` | Dropped with the converter's `conf.d/*.conf` cleanup. `<Limit>` is never carried over; `TraceEnable` is Adobe-managed |

Result: the Dispatcher SDK validator 2.0.275 passes all three phases (syntax, `httpd -t`, immutable files),
and the skill's verification passes.

Follow-up for a real migration: AMS `base_rewrite.rules` blocked spoofed `X-Forwarded-For` headers and
WordPress probes. Those protections belong in CDN traffic filter rules on Cloud Service.

## What this means for a customer conversation

- Local tools give a fast, credible first pass on **code** (the Java blockers) in minutes, before anyone
  has a Cloud Service program.
- They are not a full readiness assessment. Half the planted patterns live in the repository, and that is
  BPA's job on a real instance.
- Automated Dispatcher conversion saves time, but its output needs expert review: here it silently dropped
  the site's security filters.
