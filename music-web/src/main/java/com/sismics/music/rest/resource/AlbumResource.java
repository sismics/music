package com.sismics.music.rest.resource;

import com.sismics.music.core.dao.jpa.AlbumDao;
import com.sismics.music.core.dao.jpa.DirectoryDao;
import com.sismics.music.core.dao.jpa.TrackDao;
import com.sismics.music.core.dao.jpa.criteria.AlbumCriteria;
import com.sismics.music.core.dao.jpa.criteria.TrackCriteria;
import com.sismics.music.core.dao.jpa.dto.AlbumDto;
import com.sismics.music.core.dao.jpa.dto.TrackDto;
import com.sismics.music.core.event.async.DirectoryCreatedAsyncEvent;
import com.sismics.music.core.event.async.DirectoryDeletedAsyncEvent;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.jpa.Directory;
import com.sismics.music.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Album REST resources.
 * 
 * @author jtremeaux
 */
@Path("/album")
public class AlbumResource extends BaseResource {
    /**
     * Returns an album detail.
     *
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response id(
            @PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get album info
        AlbumDao albumDao = new AlbumDao();
        List<AlbumDto> albumList = albumDao.findByCriteria(new AlbumCriteria().setId(id));
        if (albumList.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        AlbumDto album = albumList.iterator().next();

        JSONObject response = new JSONObject();
        response.put("id", album.getId());
        response.put("name", album.getName());

        JSONObject artistJson = new JSONObject();
        artistJson.put("id", album.getArtistId());
        artistJson.put("name", album.getArtistName());
        response.put("artist", artistJson);

        // Get track info
        List<JSONObject> tracks = new ArrayList<JSONObject>();
        TrackDao trackDao = new TrackDao();
        List<TrackDto> trackList = trackDao.findByCriteria(new TrackCriteria().setAlbumId(album.getId()));
        int i = 1;
        for (TrackDto trackDto : trackList) {
            JSONObject track = new JSONObject();
            track.put("order", i++);    // TODO use order from track
            track.put("id", trackDto.getId());
            track.put("title", trackDto.getTitle());
            track.put("year", trackDto.getYear());
            track.put("length", trackDto.getLength());
            track.put("bitrate", trackDto.getBitrate());
            track.put("vbr", trackDto.isVbr());
            track.put("format", trackDto.getFormat());

            JSONObject artist = new JSONObject();
            artist.put("id", trackDto.getArtistId());
            artist.put("name", trackDto.getArtistName());
            track.put("artist", artist);

            tracks.add(track);
        }
        response.put("tracks", tracks);

        return Response.ok().entity(response).build();
    }

    /**
     * Returns all active albums.
     *
     * @return Response
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        AlbumDao albumDao = new AlbumDao();
        List<AlbumDto> albumList = albumDao.findByCriteria(new AlbumCriteria());

        JSONObject response = new JSONObject();
        List<JSONObject> items = new ArrayList<JSONObject>();
        for (AlbumDto album : albumList) {
            JSONObject albumJson = new JSONObject();
            albumJson.put("id", album.getId());
            albumJson.put("name", album.getName());

            JSONObject artistJson = new JSONObject();
            artistJson.put("id", album.getArtistId());
            artistJson.put("name", album.getArtistName());
            albumJson.put("artist", artistJson);
            items.add(albumJson);
        }
        response.put("albums", items);

        // TODO add stats

        return Response.ok().entity(response).build();
    }
}
