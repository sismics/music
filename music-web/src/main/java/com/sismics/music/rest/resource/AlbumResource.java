package com.sismics.music.rest.resource;

import javax.ws.rs.Path;

/**
 * Album REST resources.
 * 
 * @author jtremeaux
 */
@Path("/album")
public class AlbumResource extends BaseResource {
//    /**
//     * Logger.
//     */
//    private static final Logger log = LoggerFactory.getLogger(AlbumResource.class);
//
//    /**
//     * Returns an album detail.
//     *
//     * @return Response
//     * @throws JSONException
//     */
//    @GET
//    @Path("{id: [a-z0-9\\-]+}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response detail(
//            @PathParam("id") String id) throws JSONException {
//        if (!authenticate()) {
//            throw new ForbiddenClientException();
//        }
//
//        // Get album info
//        AlbumDao albumDao = new AlbumDao();
//        List<AlbumDto> albumList = albumDao.findByCriteria(new AlbumCriteria().setId(id));
//        if (albumList.isEmpty()) {
//            return Response.status(Response.Status.NOT_FOUND).build();
//        }
//        AlbumDto album = albumList.iterator().next();
//
//        JSONObject response = new JSONObject();
//        response.put("id", album.getId());
//        response.put("name", album.getName());
//        response.put("albumart", album.getAlbumArt() != null);
//
//        JSONObject artistJson = new JSONObject();
//        artistJson.put("id", album.getArtistId());
//        artistJson.put("name", album.getArtistName());
//        response.put("artist", artistJson);
//
//        // Get track info
//        List<JSONObject> tracks = new ArrayList<JSONObject>();
//        TrackDao trackDao = new TrackDao();
//        List<TrackDto> trackList = trackDao.findByCriteria(new TrackCriteria()
//                .setAlbumId(album.getId())
//                .setUserId(principal.getId()));
//        int i = 1;
//        for (TrackDto trackDto : trackList) {
//            JSONObject track = new JSONObject();
//            track.put("order", i++);    // TODO use order from track
//            track.put("id", trackDto.getId());
//            track.put("title", trackDto.getTitle());
//            track.put("year", trackDto.getYear());
//            track.put("length", trackDto.getLength());
//            track.put("bitrate", trackDto.getBitrate());
//            track.put("vbr", trackDto.isVbr());
//            track.put("format", trackDto.getFormat());
//            track.put("filename", trackDto.getFileName());
//            track.put("play_count", trackDto.getUserTrackPlayCount());
//            track.put("liked", trackDto.isUserTrackLike());
//
//            JSONObject artist = new JSONObject();
//            artist.put("id", trackDto.getArtistId());
//            artist.put("name", trackDto.getArtistName());
//            track.put("artist", artist);
//
//            tracks.add(track);
//        }
//        response.put("tracks", tracks);
//
//        return Response.ok().entity(response).build();
//    }
//
//    /**
//     * Returns an album cover.
//     *
//     * @param id Album ID
//     * @param size Cover size
//     * @return Response
//     * @throws JSONException
//     */
//    @GET
//    @Path("{id: [a-z0-9\\-]+}/albumart/{size: [a-z]+}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response albumart(
//            @PathParam("id") String id,
//            @PathParam("size") String size) throws JSONException {
//        if (!authenticate()) {
//            throw new ForbiddenClientException();
//        }
//
//        // Get the album
//        AlbumDao albumDao = new AlbumDao();
//        Album album = albumDao.getActiveById(id);
//        if (album == null) {
//            return Response.status(Response.Status.NOT_FOUND).build();
//        }
//
//        // Get the album art size
//        AlbumArtSize albumArtSize = null;
//        try {
//            albumArtSize = AlbumArtSize.valueOf(size.toUpperCase());
//        } catch (Exception e) {
//            return Response.status(Response.Status.NOT_FOUND).build();
//        }
//
//        // Get the file
//        final AlbumArtService albumArtService = AppContext.getInstance().getAlbumArtService();
//        File file = albumArtService.getAlbumArtFile(album.getAlbumArt(), albumArtSize);
//        if (!file.exists() || !file.canRead()) {
//            if (log.isErrorEnabled()) {
//                log.error("Album art file cannot be read: " + file.getAbsolutePath());
//            }
//            return Response.status(Response.Status.NOT_FOUND).build();
//        }
//
//        return Response.ok(file, "image/jpeg")
//                .header("Expires", new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(new Date().getTime() + 3600000 * 24 * 7))
//                .build();
//    }
//    
//    /**
//     * Update an album cover.
//     *
//     * @param id Album ID
//     * @param url Image URL
//     * @return Response
//     * @throws JSONException
//     */
//    @POST
//    @Path("{id: [a-z0-9\\-]+}/albumart")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response updateAlbumart(
//            @PathParam("id") String id,
//            @FormParam("url") String url) throws JSONException {
//        if (!authenticate()) {
//            throw new ForbiddenClientException();
//        }
//
//        // Get the album
//        AlbumDao albumDao = new AlbumDao();
//        Album album = albumDao.getActiveById(id);
//        if (album == null) {
//            return Response.status(Response.Status.NOT_FOUND).build();
//        }
//
//        // Copy the remote URL to a temporary file
//        File imageFile = null;
//        try {
//            imageFile = File.createTempFile("music_albumart", null);
//            try (InputStream urlStream = new URL(url).openStream();
//                    OutputStream imageStream = new FileOutputStream(imageFile)) {
//                IOUtils.copy(urlStream, imageStream);
//            }
//        } catch (IOException e) {
//            throw new ClientException("IOError", "Error while reading the remote URL", e);
//        }
//            
//        // Update the album art
//        try {
//            final AlbumArtService albumArtService = AppContext.getInstance().getAlbumArtService();
//            String albumArtId = albumArtService.importAlbumArt(imageFile);
//            album.setAlbumArt(albumArtId);
//            albumDao.update(album);
//        } catch (Exception e) {
//            throw new ClientException("ImageError", "The provided URL is not an image", e);
//        }
//        
//        // TODO Delete the previous album art
//
//        // Always return OK
//        JSONObject response = new JSONObject();
//        response.put("status", "ok");
//        return Response.ok().entity(response).build();
//    }
//
//    /**
//     * Returns all active albums.
//     *
//     * @return Response
//     * @throws JSONException
//     */
//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response list(
//            @QueryParam("artist") String artistId) throws JSONException {
//        if (!authenticate()) {
//            throw new ForbiddenClientException();
//        }
//
//        AlbumDao albumDao = new AlbumDao();
//        List<AlbumDto> albumList = albumDao.findByCriteria(new AlbumCriteria().setArtistId(artistId));
//
//        JSONObject response = new JSONObject();
//        List<JSONObject> items = new ArrayList<JSONObject>();
//        for (AlbumDto album : albumList) {
//            JSONObject albumJson = new JSONObject();
//            albumJson.put("id", album.getId());
//            albumJson.put("name", album.getName());
//            albumJson.put("update_date", album.getUpdateDate().getTime());
//            albumJson.put("albumart", album.getAlbumArt() != null);
//
//            JSONObject artistJson = new JSONObject();
//            artistJson.put("id", album.getArtistId());
//            artistJson.put("name", album.getArtistName());
//            albumJson.put("artist", artistJson);
//            items.add(albumJson);
//        }
//        response.put("albums", items);
//
//        // TODO add stats
//
//        return Response.ok().entity(response).build();
//    }
}
