package com.sismics.music.rest.resource;

import javax.ws.rs.Path;

/**
 * Theme REST resources.
 * 
 * @author jtremeaux
 */
@Path("/theme")
public class ThemeResource extends BaseResource {
//    /**
//     * Returns the list of all themes.
//     * 
//     * @return Response
//     * @throws JSONException
//     */
//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response list() throws JSONException {
//        ThemeDao themeDao = new ThemeDao();
//        List<String> themeList = null;
//        try {
//            themeList = themeDao.findAll(EnvironmentUtil.isUnitTest() ? null : request.getServletContext());
//        } catch (Exception e) {
//            throw new ServerException("UnknownError", "Error getting theme list", e);
//        }
//        JSONObject response = new JSONObject();
//        List<JSONObject> items = new ArrayList<JSONObject>();
//        for (String theme : themeList) {
//            JSONObject item = new JSONObject();
//            item.put("id", theme);
//            items.add(item);
//        }
//        response.put("themes", items);
//        return Response.ok().entity(response).build();
//    }
}
