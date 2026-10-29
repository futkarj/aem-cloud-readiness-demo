# Legacy Demo

This is an AEM project targeting Adobe Managed Services or on-prem using the Java stack.

It is built locally using Maven and can be tested against a local Quickstart. 

## Modules

- `core`: OSGi bundle. Contains the Java code for backend services, models, and business logic. Uses OSGi for dependency injection, Sling models for exposing content to Sling scripts and JUnit for unit testing.
- `dispatcher`: Contains Dispatcher configuration suitable for Adobe Managed Services or on-prem deployments, including caching and security settings. 
- `ui.apps`: FileVault content package. Contains the application code, including components, templates, client libraries, and content structure. Uses HTL as the scripting engine.
- `ui.apps.structure`: FileVault content package. Empty module that defines the structure of the repository content.
- `ui.config`: FileVault content package. Contains OSGi configurations for the application.
- `ui.content`: FileVault content package. Contains the mutable content for the application, such as the initial site structure, templates, sample assets.
- `ui.frontend`: Frontend module built with Webpack. Compiles TypeScript/JavaScript and Sass/SCSS. During the build it's copied to the `ui.apps` module as client libraries. Uses Node.js, npm, and webpack.
- `it.tests`: Integration tests module. Uses the AEM Testing clients to run tests against running AEM instances. 
- `ui.tests`: UI tests module. Uses Cypress to run end-to-end tests against running AEM instances. 
- `all`: FileVault content package. Includes all other FileVault packages for easy deployment.

## Build

The project uses Maven as the build tool. The following commands are commonly used:

- full build: `mvn clean install`
- build and deploy to a local Quickstart: `mvn clean install -PautoInstallSinglePackage`
- build and deploy a single FileVault content package: `mvn clean install -pl <module> -PautoInstallPackage`
- build and deploy a single OSGi bundle: `mvn clean install -pl <module> -PautoInstallBundle`
- build frontend only: `cd ui.frontend && npm run build`
- develop frontend locally: `cd ui.frontend && npm start`

## Important resources

Note: there are significant architectural differences between AEM as a Cloud Service and Adobe Managed Services/on-prem deployments. The resources below have been curated to ensure they apply to this project. Use them in preference to generic AEM resources.
- [Core Concepts](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/developing/introduction/the-basics)
- [AEM Technical Foundations](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/deploying/introduction/platform)
- [AEM Development Guidelines](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/developing/introduction/dev-guidelines-bestpractices)
- [Java API Best Practices](https://experienceleague.adobe.com/en/docs/experience-manager-learn/foundation/development/understand-java-api-best-practices)
- [Sling Adapters](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/developing/platform/sling-adapters)
- [Sling Resource Merger](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/developing/platform/sling-resource-merger)
- [Getting Started with HTL](https://experienceleague.adobe.com/en/docs/experience-manager-htl/content/getting-started)
- [Overlays](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/developing/platform/overlays)
- [Templates](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/developing/platform/templates/templates)
- [Core Components Introduction](https://experienceleague.adobe.com/en/docs/experience-manager-core-components/using/introduction)
- [Deprecated and Removed Features and APIs](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/release-notes/deprecated-removed-features)
- [Using Client-Side Libraries](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/developing/introduction/clientlibs)
- [Universal Editor in AEM](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/developing/headless/universal-editor/introduction)
- [Content Fragments](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/assets/content-fragments/content-fragments)
- [Experience Fragments](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/sites/authoring/authoring/experience-fragments)
- [Developing and Extending Workflows](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/developing/extending-aem/extending-workflows/workflows)
- [Replication](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/deploying/configuring/replication)
- [Content Search and Indexing](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/deploying/deploying/queries-and-indexing)
- [Deployment and Maintenance](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/deploying/deploying/deploy)
- [API Reference Materials](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/developing/introduction/reference-materials)
- [WCM.io AEM Mocks](https://wcm.io/testing/aem-mock/)
- [Sling Mocks](https://sling.apache.org/documentation/development/sling-mock.html)

