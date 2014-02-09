package com.sismics.music.rest.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.music.core.dao.jpa.TrackDao;
import com.sismics.music.core.model.jpa.Track;
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
    public Response stream(
            @HeaderParam("Range") String range,
            @PathParam("id") String id) throws Exception {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        TrackDao trackDao = new TrackDao();
        Track track = trackDao.getActiveById(id);
        if (track == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final File file = new File(track.getFileName());
        if (!file.exists() || !file.canRead()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        TransactionUtil.commit();

        // Range not requested, serve the whole file
        if (range == null) {
            StreamingOutput streamer = new StreamingOutput() {
                @Override
                public void write(final OutputStream output) throws IOException, WebApplicationException {
                    @SuppressWarnings("resource")
                    final FileChannel inputChannel = new FileInputStream(file).getChannel();
                    final WritableByteChannel outputChannel = Channels.newChannel(output);
                    try {
                        inputChannel.transferTo(0, inputChannel.size(), outputChannel);
                    } finally {
                        // Closing the channels
                        inputChannel.close();
                        outputChannel.close();
                    }
                }
            };
            return Response.ok(streamer).status(200).header(HttpHeaders.CONTENT_LENGTH, file.length()).build();
        }

        // Range requested, send a 206 partial content
        String[] ranges = range.split("=")[1].split("-");
        final int from = Integer.parseInt(ranges[0]);

        // Don't chunk the file, send content to the end
        int to = (int) (file.length() - 1);

        final String responseRange = String.format("bytes %d-%d/%d", from, to, file.length());
        final RandomAccessFile raf = new RandomAccessFile(file, "r");
        raf.seek(from);

        final int len = to - from + 1;
        final MediaStreamer streamer = new MediaStreamer(len, raf);
        Response.ResponseBuilder res = Response.ok(streamer).status(206)
                .header("Accept-Ranges", "bytes")
                .header("Content-Range", responseRange)
                .header(HttpHeaders.CONTENT_LENGTH, streamer.getLength())
                .header(HttpHeaders.LAST_MODIFIED, new Date(file.lastModified()));
        return res.build();
    }

    /**
     * Like a track.
     *
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/like")
    @Produces(MediaType.APPLICATION_JSON)
    public Response like(
            @PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        TrackDao trackDao = new TrackDao();
        Track track = trackDao.getActiveById(id);
        if (track == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // TODO like track

        // Always return OK
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Unlike a track.
     *
     * @return Response
     * @throws JSONException
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}/like")
    @Produces(MediaType.APPLICATION_JSON)
    public Response unlike(
            @PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        TrackDao trackDao = new TrackDao();
        Track track = trackDao.getActiveById(id);
        if (track == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // TODO unlike track

        // Always return OK
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
