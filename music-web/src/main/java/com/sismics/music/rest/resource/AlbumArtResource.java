package com.sismics.music.rest.resource;

import com.google.common.base.Strings;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.service.lastfm.LastFmService;
import com.sismics.rest.exception.ForbiddenClientException;
import de.umass.lastfm.Album;
import de.umass.lastfm.ImageSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.Collection;

/**
 * Album art REST resources.
 * 
 * @author jtremeaux
 */
@Path("/albumart")
public class AlbumArtResource extends BaseResource {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AlbumArtResource.class);

    /**
     * Search album covers.
     *
     * @param query The query
     * @return Response
     */
    @GET
    @Path("search")
    public Response search(
            @QueryParam("query") String query) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the album arts
        JsonObjectBuilder response = Json.createObjectBuilder();
        JsonArrayBuilder items = Json.createArrayBuilder();
        final LastFmService lastFmService = AppContext.getInstance().getLastFmService();
        Collection<Album> albums = lastFmService.searchAlbumArt(query);
        for (Album album : albums) {
            String url = album.getImageURL(ImageSize.EXTRALARGE);
            if (!Strings.isNullOrEmpty(url)) {
                items.add(Json.createObjectBuilder()
                        .add("url", url)
                        .add("width", 300)
                        .add("height", 300)
                        .build());
            }
        }

        response.add("total", albums.size());
        response.add("albumArts", items);

        return renderJson(response);
    }
}
