# Deliberate legacy patterns

`legacy-65-lts` is AEM Project Archetype 58 (generated with `aemVersion=6.5.7`, then moved to `uber-jar:6.6.0:apis`,
the API for AEM 6.5 LTS)
plus the patterns below. Each one runs on AEM 6.5 LTS but blocks or complicates a move to AEM as a Cloud Service.
They are the inputs for the assessment (BPA, plus the `aem-cloud-service` migration skill), so the findings can
be checked against this list.

| # | Pattern | Where | Cloud Service issue |
|---|---|---|---|
| 1 | Path-bound servlet | `core/.../legacy/ContentExportServlet.java` | `/bin` path binding instead of a resource type |
| 2 | Administrative login | `ContentExportServlet`, `JcrPageEventListener` | `getAdministrativeResourceResolver` / `loginAdministrative` are not allowed; use service users |
| 3 | Unbounded query | `ContentExportServlet` | JCR-SQL2 over a whole tree with no limit or index |
| 4 | Replication agent filter | `core/.../legacy/PageActivationService.java` | Named replication agents are replaced by Sling Content Distribution |
| 5 | DAM `AssetManager.createAsset` | `core/.../legacy/DamAssetImporter.java` | Binary upload goes through direct binary upload and Asset compute |
| 6 | JCR `EventListener` | `core/.../legacy/JcrPageEventListener.java` | Long-lived session and JCR observation; use `ResourceChangeListener` or Sling Jobs |
| 7 | OSGi `EventHandler` doing work | `core/.../legacy/ReplicationEventHandler.java` | Work should move to a Sling Job |
| 8 | Sling Commons Scheduler `Runnable` | `core/.../schedulers/SimpleScheduledTask.java` (from the archetype) | Runs on every instance; use Sling Jobs for once-per-cluster work |
| 9 | Static template | `ui.apps/.../apps/legacydemo/templates/legacy-page` | Editable templates under `/conf` |
| 10 | Classic UI (ExtJS) dialog | `ui.apps/.../components/legacy-teaser/dialog.xml` | Classic UI is gone; needs a Touch UI (Coral 3) dialog |
| 11 | `sling:OsgiConfig` XML, no run mode | `ui.config/.../apps/legacydemo/config/` | `.cfg.json` under `osgiconfig/config.<runmode>`; same value for every environment |
| 12 | Mutable data in `/apps` | `ui.apps/.../apps/legacydemo/settings/feature-flags` | `/apps` is immutable at runtime; move to `/conf` (context-aware config) |
| 13 | Custom replication agent | `ui.content/.../etc/replication/agents.author/legacy-flush` | No replication agents on Cloud Service |
| 14 | Classic design | `ui.content/.../etc/designs/legacydemo` | Template policies replace designs |
| 15 | Unversioned custom Oak index | `ui.apps/.../_oak_index/legacydemoLucene` | Cloud Service index names need a version suffix (`legacydemoLucene-custom-1`) |

The archetype's AMS-style `dispatcher/` config also stays as it is, as input for the Dispatcher Converter.
