package com.sismics.rest.exception;

import javax.json.Json;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Jersey exception encapsulating an error from the client (BAD_REQUEST).
 *
 * @author jtremeaux
 */
public class ClientException extends WebApplicationException {
    /**
     * Serial UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor of ClientException.
     * 
     * @param type Error type (e.g. AlreadyExistingEmail, ValidationError)
     * @param message Human readable error message
     */
    public ClientException(String type, String message) {
        this(type, message, Status.BAD_REQUEST);
    }

    /**
     * Constructor of ClientException.
     * 
     * @param type Error type (e.g. AlreadyExistingEmail, ValidationError)
     * @param message Human readable error message
     */
    public ClientException(String type, String message, Status status) {
        super(Response.status(status).entity(Json.createObjectBuilder()
            .add("type", type)
            .add("message", message).build())
                .build());
    }
}
