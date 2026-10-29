package com.demo.legacy.core.legacy;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * LEGACY: servlet bound to a fixed path under /bin, reading content with an administrative
 * resource resolver and an unbounded JCR-SQL2 query.
 */
@Component(service = Servlet.class, property = {
        "sling.servlet.paths=/bin/legacydemo/export",
        "sling.servlet.methods=GET"
})
public class ContentExportServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;

    @Reference
    private transient ResourceResolverFactory resolverFactory;

    @Override
    @SuppressWarnings("deprecation")
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        try (ResourceResolver admin = resolverFactory.getAdministrativeResourceResolver(null)) {
            Iterator<Resource> pages = admin.findResources(
                    "SELECT * FROM [cq:Page] WHERE ISDESCENDANTNODE('/content/legacydemo')", "JCR-SQL2");
            while (pages.hasNext()) {
                response.getWriter().println(pages.next().getPath());
            }
        } catch (LoginException e) {
            response.sendError(500, e.getMessage());
        }
    }
}
