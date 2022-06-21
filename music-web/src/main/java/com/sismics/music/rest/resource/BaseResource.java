package com.sismics.music.rest.resource;

import com.sismics.music.rest.constant.Privilege;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.security.IPrincipal;
import com.sismics.security.UserPrincipal;
import com.sismics.util.filter.TokenBasedSecurityFilter;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.Principal;
import java.util.Set;

/**
 * Base class of REST resources.
 * 
 * @author jtremeaux
 */
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public abstract class BaseResource {
    /**
     * Injects the HTTP request.
     */
    @Context
    protected HttpServletRequest request;
    
    /**
     * Application key.
     */
    @QueryParam("app_key")
    protected String appKey;
    
    /**
     * Principal of the authenticated user.
     */
    protected IPrincipal principal;

    /**
     * This method is used to check if the user is authenticated.
     * 
     * @return True if the user is authenticated and not anonymous
     */
    protected boolean authenticate() {
        Principal principal = (Principal) request.getAttribute(TokenBasedSecurityFilter.PRINCIPAL_ATTRIBUTE);
        if (principal != null && principal instanceof IPrincipal) {
            this.principal = (IPrincipal) principal;
            return !this.principal.isAnonymous();
        } else {
            return false;
        }
    }
    
    /**
     * Checks if the user has a privilege. Throw an exception if the check fails.
     * 
     * @param privilege Privilege to check
     */
    protected void checkPrivilege(Privilege privilege) {
        if (!hasPrivilege(privilege)) {
            throw new ForbiddenClientException();
        }
    }
    
    /**
     * Checks if the user has a privilege.
     * 
     * @param privilege Privilege to check
     * @return True if the user has the privilege
     */
    protected boolean hasPrivilege(Privilege privilege) {
        if (principal == null || !(principal instanceof UserPrincipal)) {
            return false;
        }
        Set<String> privilegeSet = ((UserPrincipal) principal).getPrivilegeSet();
        return privilegeSet != null && privilegeSet.contains(privilege.name());
    }

    /**
     * Returns an JSON message.
     *
     * @return The JSON message
     */
    protected Response renderJson(JsonObjectBuilder response) {
        return Response.ok()
                .entity(response.build())
                .build();
    }

    /**
     * Returns an OK JSON message.
     *
     * @return The JSON message
     */
    protected Response okJson() {
        return renderJson(Json.createObjectBuilder().add("status", "ok"));
    }

    protected void notFoundIfNull(Object object, String message) {
        if (object == null) {
            throw new ClientException("NotFound", message, Response.Status.NOT_FOUND);
        }
    }
}
