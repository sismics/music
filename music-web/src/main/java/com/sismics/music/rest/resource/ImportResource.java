package com.sismics.music.rest.resource;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.sismics.music.core.dao.dbi.DirectoryDao;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Directory;
import com.sismics.music.core.service.importaudio.ImportAudio;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

/**
 * Import REST resources.
 * 
 * @author bgamard
 */
@Path("/import")
public class ImportResource extends BaseResource {
    /**
     * Import new tracks from external sources.
     * 
     * @param urlList URLs to import
     * @param quality Transcoding quality
     * @param format Output format
     * @return Response
     */
    @PUT
    public Response add(
            @FormParam("url") List<String> urlList,
            @FormParam("quality") String quality,
            @FormParam("format") String format) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input
        if (urlList == null || urlList.size() == 0) {
            throw new ClientException("ValidationError", "url must be set and not empty");
        }
        ValidationUtil.validateRequired(quality, "quality");
        ValidationUtil.validateRequired(format, "format");
        if (!Lists.newArrayList("128K", "192K", "256K").contains(quality)) {
            throw new ClientException("ValidationError", "quality must be 128K, 192K or 256K");
        }
        if (!Lists.newArrayList("mp3", "aac", "vorbis").contains(format)) {
            throw new ClientException("ValidationError", "format must be mp3, aac or vorbis");
        }
        
        // Add the URL to the import queue
        AppContext.getInstance().getImportAudioService().downloadAudio(urlList, quality, format);
        
        // Always return OK
        return Response.ok()
                .entity(Json.createObjectBuilder().add("status", "ok").build())
                .build();
    }
    
    /**
     * Return dependencies versions.
     * 
     * @return Response
     */
    @GET
    @Path("dependencies")
    public Response dependencies() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Return youtube-dl and ffmpeg versions
        JsonObjectBuilder response = Json.createObjectBuilder();
        String version = AppContext.getInstance().getImportAudioService().getYoutubeDlVersion();
        if (version != null) {
            response.add("youtube-dl", version);
        }
        version = AppContext.getInstance().getImportAudioService().getFfmpegVersion();
        if (version != null) {
            response.add("ffmpeg", version);
        }
        return Response.ok()
                .entity(response.build())
                .build();
    }
    
    /**
     * List imports in progress from external sources.
     * 
     * @return Response
     */
    @GET
    @Path("progress")
    public Response progress() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        List<ImportAudio> importAudioList = AppContext.getInstance().getImportAudioService().getImportAudioList();
        JsonObjectBuilder response = Json.createObjectBuilder();
        JsonArrayBuilder items = Json.createArrayBuilder();
        for (ImportAudio importAudio : importAudioList) {
            items.add(Json.createObjectBuilder()
                    .add("id", importAudio.getId())
                    .add("url", importAudio.getUrl())
                    .add("download_speed", importAudio.getDownloadSpeed())
                    .add("total_size", importAudio.getTotalSize())
                    .add("message", importAudio.getMessage())
                    .add("progress", importAudio.getProgress())
                    .add("status", importAudio.getStatus().name()));
        }
        response.add("imports", items);

        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Retry a failed import from an external source.
     * 
     * @return Response
     */
    @POST
    @Path("progress/{id: [a-z0-9\\-]+}/retry")
    public Response retry(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        try {
            AppContext.getInstance().getImportAudioService().retryImportAudio(id);
        } catch (Exception e) {
            throw new ServerException("ImportError", e.getMessage(), e);
        }
        
        // Always return OK
        return Response.ok()
                .entity(Json.createObjectBuilder().add("status", "ok").build())
                .build();
    }
    
    @POST
    @Path("progress/{id: [a-z0-9\\-]+}/kill")
    public Response kill(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        try {
            AppContext.getInstance().getImportAudioService().killImportAudio(id);
        } catch (Exception e) {
            throw new ClientException("ImportError", e.getMessage());
        }
        
        // Always return OK
        return Response.ok()
                .entity(Json.createObjectBuilder().add("status", "ok").build())
                .build();
    }
    
    /**
     * Cleanup finished imports from external sources.
     * 
     * @return Response
     */
    @POST
    @Path("progress/cleanup")
    public Response cleanup() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        AppContext.getInstance().getImportAudioService().cleanup();
        
        // Always return OK
        return Response.ok()
                .entity(Json.createObjectBuilder().add("status", "ok").build())
                .build(); 
    }
    
    /**
     * Upload a track to the imported files.
     * 
     * @param fileBodyPart File to import
     * @return Response
     */
    @PUT
    @Path("upload")
    @Consumes("multipart/form-data")
    public Response upload(
            @FormDataParam("file") FormDataBodyPart fileBodyPart) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        ValidationUtil.validateRequired(fileBodyPart, "file");
        ValidationUtil.validateRequired(fileBodyPart.getFormDataContentDisposition().getFileName(), "filename");
        
        InputStream in = fileBodyPart.getValueAs(InputStream.class);
        File tempDir = null, importFile = null;
        try {
            // Copy the incoming stream content into a temporary file
            tempDir = Files.createTempDir();
            importFile = new File(tempDir.getAbsolutePath() + File.separator + fileBodyPart.getFormDataContentDisposition().getFileName());
            try (OutputStream os = new FileOutputStream(importFile)) {
                IOUtils.copy(in, os);
            }
            
            AppContext.getInstance().getImportAudioService().importFile(importFile);
        } catch (Exception e) {
            throw new ServerException("ImportError", e.getMessage(), e);
        } finally {
            if (importFile != null) {
                importFile.delete();
            }
            if (tempDir != null) {
                tempDir.delete();
            }
        }
        
        // Always return OK
        return Response.ok()
                .entity(Json.createObjectBuilder().add("status", "ok").build())
                .build(); 
    }
    
    /**
     * List imported tracks.
     * 
     * @return Response
     */
    @GET
    public Response list() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        List<File> importedFileList = AppContext.getInstance().getImportAudioService().getImportedFileList();
        Collections.sort(importedFileList, new LastModifiedFileComparator());
        
        JsonObjectBuilder response = Json.createObjectBuilder();
        JsonArrayBuilder items = Json.createArrayBuilder();
        for (File importedFile : importedFileList) {
            items.add(Json.createObjectBuilder()
                    .add("file", importedFile.getName()));
        }
        response.add("files", items);
        
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Tag & transfer an imported file.
     * 
     * @return Response
     */
    @POST
    public Response tag(
            @FormParam("file") String fileName,
            @FormParam("order") String orderStr,
            @FormParam("artist") String artist,
            @FormParam("album_artist") String albumArtist,
            @FormParam("album") String album,
            @FormParam("title") String title,
            @FormParam("directory") String directoryId) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input
        ValidationUtil.validateRequired(fileName, "file");
        artist = ValidationUtil.validateLength(artist, "artist", 1, 1000);
        albumArtist = ValidationUtil.validateLength(albumArtist, "album_artist", 0, 1000, true);
        album = ValidationUtil.validateLength(album, "album", 1, 1000);
        title = ValidationUtil.validateLength(title, "title", 1, 2000);
        Integer order = null;
        if (orderStr != null) {
            order = ValidationUtil.validateInteger(orderStr, "order");
        }
        
        if (albumArtist == null) {
            albumArtist = artist;
        }
        
        // Validate directory
        DirectoryDao directoryDao = new DirectoryDao();
        Directory directory = null;
        if (directoryId == null) {
            List<Directory> directoryList = directoryDao.findAllEnabled();
            if (directoryList.size() == 0) {
                throw new ClientException("NoDirectory", "No configured directory available");
            }
            directory = directoryList.iterator().next();
        } else {
            directory = directoryDao.getActiveById(directoryId);
            if (directory == null) {
                throw new ClientException("ValidationError", "This directory cannot be found");
            }
        }
        
        // Tag the file
        try {
            AppContext.getInstance().getImportAudioService().tagFile(fileName, order, title, album, artist, albumArtist, directory);
        } catch (Exception e) {
            throw new ServerException("TagError", e.getMessage(), e);
        }
        
        // Always return OK
        return Response.ok()
                .entity(Json.createObjectBuilder().add("status", "ok").build())
                .build();
    }
    
    /**
     * Delete an imported file.
     * 
     * @param fileName File to delete
     * @return Response
     */
    @DELETE
    public Response delete(
            @QueryParam("file") String fileName) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input
        ValidationUtil.validateRequired(fileName, "file");
        
        // Retrieve the file from imported files
        List<File> importedFileList = AppContext.getInstance().getImportAudioService().getImportedFileList();
        File file = null;
        for (File importedFile : importedFileList) {
            if (importedFile.getName().equals(fileName)) {
                file = importedFile;
                break;
            }
        }
        
        if (file == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        // Delete the file
        if (!file.delete()) {
            throw new ServerException("IOError", "Error deleting the file");
        }
        
        // Always return OK
        return Response.ok()
                .entity(Json.createObjectBuilder().add("status", "ok").build())
                .build(); 
    }
}
