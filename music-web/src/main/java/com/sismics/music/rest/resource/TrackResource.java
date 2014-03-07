package com.sismics.music.rest.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.sismics.music.core.dao.dbi.TrackDao;
import com.sismics.music.core.dao.dbi.UserDao;
import com.sismics.music.core.dao.dbi.UserTrackDao;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Track;
import com.sismics.music.core.model.dbi.User;
import com.sismics.music.core.service.lastfm.LastFmService;
import com.sismics.music.core.service.transcoder.TranscoderService;
import com.sismics.music.core.util.TransactionUtil;
import com.sismics.music.rest.util.MediaStreamer;
import com.sismics.rest.exception.ForbiddenClientException;

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

        // Start a new thread and release the I/O thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // TODO Transcode mp3 as well
                    final TranscoderService transcoderService = AppContext.getInstance().getTranscoderService();
                    if (!"mp3".equals(track.getFormat())) {
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
                        InputStream is = transcoderService.getTranscodedInputStream(track, seek);
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
                    } else {
                        // Range not requested, serve the whole file
                        if (range == null) {
                            StreamingOutput streamer = new StreamingOutput() {
                                @Override
                                public void write(final OutputStream output) throws IOException, WebApplicationException {
                                    final FileInputStream inputStream = new FileInputStream(file);
                                    final FileChannel inputChannel = inputStream.getChannel();
                                    final WritableByteChannel outputChannel = Channels.newChannel(output);
                                    try {
                                        inputChannel.transferTo(0, inputChannel.size(), outputChannel);
                                    } finally {
                                        // Closing the channels
                                        inputStream.close();
                                        inputChannel.close();
                                        outputChannel.close();
                                    }
                                }
                            };
                            asyncResponse.resume(Response.ok(streamer).status(200)
                                    .header(HttpHeaders.CONTENT_LENGTH, file.length())
                                    .build());
                            return;
                        }
    
                        // Range requested, send a 206 partial content
                        String[] ranges = range.split("=")[1].split("-");
                        final int from = Integer.parseInt(ranges[0]);
    
                        // Don't chunk the file, send content to the end
                        final int to = (int) (file.length() - 1);
    
                        final String responseRange = String.format("bytes %d-%d/%d", from, to, file.length());
                        final RandomAccessFile raf = new RandomAccessFile(file, "r");
                        raf.seek(from);
    
                        final int len = to - from + 1;
                        final MediaStreamer streamer = new MediaStreamer(len, raf);
                        asyncResponse.resume(Response.ok(streamer).status(206)
                                .header("Accept-Ranges", "bytes")
                                .header("Content-Range", responseRange)
                                .header(HttpHeaders.CONTENT_LENGTH, streamer.getLength())
                                .header(HttpHeaders.LAST_MODIFIED, new Date(file.lastModified()))
                                .build());
                    }
                } catch (Exception e) {
                    asyncResponse.resume(e);
                }
            }
        }).start();
    }

    /**
     * Like a track.
     *
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

        TrackDao trackDao = new TrackDao();
        Track track = trackDao.getActiveById(id);
        if (track == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        // TODO Update track in database/tags.
        
        // Always return OK
        return Response.ok()
                .entity(Json.createObjectBuilder().add("status", "ok").build())
                .build();
    }
}
