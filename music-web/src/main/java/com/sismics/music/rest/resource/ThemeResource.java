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

import com.sismics.music.rest.dao.ThemeDao;
import com.sismics.rest.exception.ServerException;
import com.sismics.util.EnvironmentUtil;

/**
 * Theme REST resources.
 * 
 * @author jtremeaux
 */
@Path("/theme")
public class ThemeResource extends BaseResource {
    /**
     * Returns the list of all themes.
     * 
     * @return Response
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() {
        ThemeDao themeDao = new ThemeDao();
        List<String> themeList = null;
        try {
            themeList = themeDao.findAll(EnvironmentUtil.isUnitTest() ? null : request.getServletContext());
        } catch (Exception e) {
            throw new ServerException("UnknownError", "Error getting theme list", e);
        }
        JsonObjectBuilder response = Json.createObjectBuilder();
        JsonArrayBuilder items = Json.createArrayBuilder();
        for (String theme : themeList) {
            items.add(Json.createObjectBuilder().add("id", theme));
        }
        response.add("themes", items);
        return Response.ok().entity(response.build()).build();
    }
}
