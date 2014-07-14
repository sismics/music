package com.sismics.music.rest.resource;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Date;

import javax.json.Json;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import com.sismics.music.core.dao.dbi.ArtistDao;
import com.sismics.music.core.dao.dbi.TrackDao;
import com.sismics.music.core.dao.dbi.UserDao;
import com.sismics.music.core.dao.dbi.UserTrackDao;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Artist;
import com.sismics.music.core.model.dbi.Track;
import com.sismics.music.core.model.dbi.Transcoder;
import com.sismics.music.core.model.dbi.User;
import com.sismics.music.core.service.lastfm.LastFmService;
import com.sismics.music.core.service.transcoder.TranscoderService;
import com.sismics.music.core.util.TransactionUtil;
import com.sismics.music.rest.util.MediaStreamer;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;

/**
 * Track REST resources.
 * 
 * @author jtremeaux
 */
@Path("/track")
public class TrackResource extends BaseResource {
    /**
     * Returns a track stream.
     *
     * @param id Track ID
     * @return Response
     * @throws Exception 
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}")
    @Produces("audio/mpeg")
    public void stream(
            @HeaderParam("Range") final String range,
            @PathParam("id") final String id,
            @Suspended final AsyncResponse asyncResponse) throws Exception {
        if (!authenticate()) {
            asyncResponse.resume(new ForbiddenClientException());
            return;
        }

        TrackDao trackDao = new TrackDao();
        final Track track = trackDao.getActiveById(id);
        TransactionUtil.commit();
        if (track == null) {
            asyncResponse.resume(Response.status(Response.Status.NOT_FOUND).build());
            return;
        }

        final File file = new File(track.getFileName());
        if (!file.exists() || !file.canRead()) {
            asyncResponse.resume(Response.status(Response.Status.NOT_FOUND).build());
            return;
        }
        
        // Select a suitable transcoder
        final TranscoderService transcoderService = AppContext.getInstance().getTranscoderService();
        final Transcoder transcoder = transcoderService.getSuitableTranscoder(track);

        // Start a new thread and release the I/O thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (transcoder == null) {
                        // Don't chunk the file, send content to the end
                        final RandomAccessFile raf = new RandomAccessFile(file, "r");
                        int from = 0, to = (int) (file.length() - 1);
                        String responseRange = null;
                        
                        if (range != null) {
                            // Range requested
                            String[] ranges = range.split("=")[1].split("-");
                            from = Integer.parseInt(ranges[0]);
                            
                            responseRange = String.format("bytes %d-%d/%d", from, to, file.length());
                            raf.seek(from);
                        }
    
                        final MediaStreamer streamer = new MediaStreamer(to - from + 1, raf);
                        asyncResponse.resume(Response.ok(streamer).status(range == null ? 200 : 206)
                                .header("Accept-Ranges", "bytes")
                                .header("Content-Range", responseRange)
                                .header(HttpHeaders.CONTENT_LENGTH, streamer.getLength())
                                .header(HttpHeaders.LAST_MODIFIED, new Date(file.lastModified()))
                                .build());
                    } else {
                        int seek = 0;
                        int from = 0;
                        int to = 0;
                        
                        if (range != null) {
                            // Range requested, send a 206 partial content
                            String[] ranges = range.split("=")[1].split("-");
                            from = Integer.parseInt(ranges[0]);
                            seek = from / (128 * 1000 / 8);
                        }
                        
                        int fileSize = track.getLength() * 128 * 1000 / 8;
                        InputStream is = transcoderService.getTranscodedInputStream(track, seek, transcoder);
                        Response.ResponseBuilder response = Response.ok(is);
                        
                        if (range != null) {
                            response = response.status(206);
                            final String responseRange = String.format("bytes %d-%d/%d", from, to, fileSize);
                            response.header("Accept-Ranges", "bytes");
                            response.header("Content-Range", responseRange);
                        }
                        
                        response.header(HttpHeaders.CONTENT_LENGTH, fileSize);
                        response.header(HttpHeaders.LAST_MODIFIED, new Date(file.lastModified()));
                        asyncResponse.resume(response.build());
                    }
                } catch (Exception e) {
                    asyncResponse.resume(e);
                }
            }
        }, "TrackAsyncResponse").start();
    }

    /**
     * Like a track.
     *
     * @param id Track ID
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/like")
    @Produces(MediaType.APPLICATION_JSON)
    public Response like(
            @PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        TrackDao trackDao = new TrackDao();
        Track track = trackDao.getActiveById(id);
        if (track == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Like the track locally
        UserTrackDao userTrackDao = new UserTrackDao();
        userTrackDao.like(principal.getId(), track.getId());

        // Love the track on Last.fm
        final User user = new UserDao().getActiveById(principal.getId());
        if (user != null && user.getLastFmSessionToken() != null) {
            final LastFmService lastFmService = AppContext.getInstance().getLastFmService();
            lastFmService.loveTrack(user, track);
        }

        // Always return OK
        return Response.ok()
                .entity(Json.createObjectBuilder().add("status", "ok").build())
                .build();
    }

    /**
     * Unlike a track.
     *
     * @param id Track ID
     * @return Response
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}/like")
    @Produces(MediaType.APPLICATION_JSON)
    public Response unlike(
            @PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        TrackDao trackDao = new TrackDao();
        Track track = trackDao.getActiveById(id);
        if (track == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Unlike the track locally
        UserTrackDao userTrackDao = new UserTrackDao();
        userTrackDao.unlike(principal.getId(), track.getId());

        // Unove the track on Last.fm
        final User user = new UserDao().getActiveById(principal.getId());
        if (user != null && user.getLastFmSessionToken() != null) {
            final LastFmService lastFmService = AppContext.getInstance().getLastFmService();
            lastFmService.unloveTrack(user, track);
        }

        // Always return OK
        return Response.ok()
                .entity(Json.createObjectBuilder().add("status", "ok").build())
                .build();
    }
    
    /**
     * Update a track.
     * 
     * @param id Track ID
     * @param order Order
     * @param title Title
     * @param album Album name
     * @param artist Artist name
     * @param albumArtist Album artist name
     * @param yearStr Year
     * @param genre Genre
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}")
    public Response update(
            @PathParam("id") String id,
            @FormParam("order") String orderStr,
            @FormParam("title") String title,
            @FormParam("album") String album,
            @FormParam("artist") String artist,
            @FormParam("album_artist") String albumArtist,
            @FormParam("year") String yearStr,
            @FormParam("genre") String genre) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        ValidationUtil.validateLength(title, "title", 1, 2000);
        ValidationUtil.validateLength(album, "album", 1, 1000);
        ValidationUtil.validateLength(artist, "artist", 1, 1000);
        ValidationUtil.validateLength(albumArtist, "album_artist", 1, 1000);
        Integer year = ValidationUtil.validateInteger(yearStr, "year");
        Integer order = ValidationUtil.validateInteger(orderStr, "order");

        TrackDao trackDao = new TrackDao();
        ArtistDao artistDao = new ArtistDao();
        Track trackDb = trackDao.getActiveById(id);
        if (trackDb == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        // Update tags on file
        final File file = new File(trackDb.getFileName());
        if (!file.exists() || !file.canWrite()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        try {
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();
            tag.setField(FieldKey.TRACK, orderStr);
            tag.setField(FieldKey.TITLE, title);
            tag.setField(FieldKey.ALBUM, album);
            tag.setField(FieldKey.ARTIST, artist);
            tag.setField(FieldKey.ALBUM_ARTIST, albumArtist);
            if (yearStr == null) {
                tag.deleteField(FieldKey.YEAR);
            } else {
                tag.setField(FieldKey.YEAR, yearStr);
            }
            if (genre == null) {
                tag.deleteField(FieldKey.GENRE);
            } else {
                tag.setField(FieldKey.GENRE, genre);
            }
            AudioFileIO.write(audioFile);
        } catch (Exception e) {
            throw new ServerException("TagError", "Error tagging the file: " + file, e);
        }
        
        // Tagging goes well, update the database
        trackDb.setTitle(title);
        trackDb.setYear(year);
        trackDb.setGenre(genre);
        trackDb.setOrder(order);
        
        // Set the new artist (and create it if necessary)
        Artist artistDb = artistDao.getActiveById(trackDb.getArtistId());
        if (!StringUtils.equals(artist, artistDb.getName())) {
            Artist newArtistDb = artistDao.getActiveByName(artist);
            if (newArtistDb == null) {
                newArtistDb = new Artist();
                newArtistDb.setName(artist);
                artistDao.create(newArtistDb);
            }
            
            trackDb.setArtistId(newArtistDb.getId());
        }
        
        trackDao.update(trackDb);
        artistDao.deleteEmptyArtist();
        
        // Always return OK
        return Response.ok()
                .entity(Json.createObjectBuilder().add("status", "ok").build())
                .build();
    }
}
