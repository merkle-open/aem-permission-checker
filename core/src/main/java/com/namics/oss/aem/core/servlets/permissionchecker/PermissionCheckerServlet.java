package com.namics.oss.aem.core.servlets.permissionchecker;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Iterator;

/**
 * Servlet to check user permissions.
 * It implements a doPost() method which reads the received JSON configuration to perform permission checks using a {@link PermissionChecker} implementation.
 * See https://wiki.namics.com/display/VBSCMSIMPL/Permission+Checker+Servlet for further information.
 *
 * @author Mike Schmid
 */
@Slf4j
@Component(
        service = Servlet.class,
        immediate = true,
        property = {
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/permissionchecker",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=GET",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=POST"
        }
)
@Designate(ocd = PermissionCheckerServlet.Configuration.class)
public class PermissionCheckerServlet extends SlingAllMethodsServlet {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private boolean active;

    @Reference
    private ResourceResolverFactory resolverFactory;

    @ObjectClassDefinition(name = "Namics Permission Checker Servlet")
    public @interface Configuration {

        @AttributeDefinition(
                name = "Active",
                description = "Defines if the servlet is active on this instance"
        )
        boolean active() default false;
    }

    @Activate
    protected void activate(final Configuration config) {
        active = config.active();
        if (active) {
            log.info("Permission checker service started and active.");
        } else {
            log.info("Permission checker service started but deactivated by configuration");
        }
    }

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        if (active && isAuthorized(request)) {

            response.setContentType("application/json");

            try {
                final PermissionTestCase[] permissionTestCases = JSON_MAPPER.readValue(request.getInputStream(), PermissionTestCase[].class);
                final Session adminSession = resolverFactory.getAdministrativeResourceResolver(null).adaptTo(Session.class);

                final PermissionCheckerTestResults permissionCheckerTestResults = new PermissionChecker(adminSession)
                        .checkPermissions(permissionTestCases);

                if (!permissionCheckerTestResults.getAllTestsSuccessful()) {
                    response.setStatus(HttpStatus.SC_BAD_REQUEST);
                }

                response.getWriter().write(JSON_MAPPER.writeValueAsString(permissionCheckerTestResults));

            } catch (Exception e) {
                response.setStatus(HttpStatus.SC_BAD_REQUEST);
                response.getWriter().write(e.getMessage());
                log.error("An error in the permission checker occurred", e);
            }

        } else {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
        }

    }

    private boolean isAuthorized(SlingHttpServletRequest request) {
        final UserManager userManager = request.getResourceResolver().adaptTo(UserManager.class);

        try {

            if (userManager != null) {
                final Authorizable authorizable = userManager.getAuthorizable(request.getUserPrincipal());

                if (authorizable != null) {
                    if (StringUtils.equals(authorizable.getID(), "admin")) {
                        log.info("Permission checker called by user \"admin\"");
                        return true;
                    }

                    final Iterator<Group> groupIterator = authorizable.memberOf();
                    while (groupIterator.hasNext()) {
                        final Group group = groupIterator.next();
                        log.info("iterate group:{} of user: {}", group.getID(), authorizable.getID());
                        if (StringUtils.equals(group.getID(), "namics-permission-checker")) {
                            log.info("Permission checker called by user \"{}\"", authorizable.getID());
                            return true;
                        }
                    }
                    log.warn("An unauthorized user tried to call the permission checker: \"{}\"", authorizable.getID());
                }
            }

        } catch (RepositoryException e) {
            log.warn("An error occurred while checking if the current user is authorized to use the permission checker", e);
        }

        return false;
    }

}