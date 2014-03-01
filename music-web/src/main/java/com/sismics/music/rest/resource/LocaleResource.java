package com.sismics.music.rest.resource;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sismics.music.core.dao.dbi.LocaleDao;
import com.sismics.music.core.model.dbi.Locale;

/**
 * Locale REST resources.
 * 
 * @author jtremeaux
 */
@Path("/locale")
public class LocaleResource extends BaseResource {
    /**
     * Returns the list of all locales.
     * 
     * @return Response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() {
        LocaleDao localeDao = new LocaleDao();
        List<Locale> localeList = localeDao.findAll();
        JsonObjectBuilder response = Json.createObjectBuilder();
        JsonArrayBuilder items = Json.createArrayBuilder();
        for (Locale locale : localeList) {
            items.add(Json.createObjectBuilder()
                    .add("id", locale.getId()));
        }
        response.add("locales", items);
        return Response.ok().entity(response.build()).build();
    }
}
