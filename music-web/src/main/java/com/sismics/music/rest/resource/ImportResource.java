package com.sismics.music.rest.resource;

import com.google.common.collect.Lists;
import com.sismics.music.core.dao.dbi.DirectoryDao;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Directory;
import com.sismics.music.core.service.importaudio.ImportAudio;
import com.sismics.music.core.service.importaudio.ImportAudioFile;
import com.sismics.rest.FormDataUtil;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.Validation;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.File;
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
        Validation.required(quality, "quality");
        Validation.required(format, "format");
        if (!Lists.newArrayList("128K", "192K", "256K").contains(quality)) {
            throw new ClientException("ValidationError", "quality must be 128K, 192K or 256K");
        }
        if (!Lists.newArrayList("mp3", "aac", "vorbis").contains(format)) {
            throw new ClientException("ValidationError", "format must be mp3, aac or vorbis");
        }
        
        // Add the URL to the import queue
        AppContext.getInstance().getImportAudioService().downloadAudio(urlList, quality, format);
        
        // Always return OK
        return okJson();
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
        return renderJson(response);
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

        return renderJson(response);
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
        return okJson();
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
        return okJson();
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
        return okJson();
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
        Validation.required(fileBodyPart, "file");
        Validation.required(fileBodyPart.getFormDataContentDisposition().getFileName(), "filename");

        File importFile = null;
        try {
            importFile = FormDataUtil.getAsTempFile(fileBodyPart);
            AppContext.getInstance().getImportAudioService().importFile(importFile);
        } catch (Exception e) {
            throw new ServerException("ImportError", e.getMessage(), e);
        } finally {
            if (importFile != null) {
                importFile.delete();
            }
        }

        // Always return OK
        return okJson();
    }

    /**
     * List imported tracks, and suggest some tags.
     * 
     * @return Response
     */
    @GET
    public Response list() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        List<ImportAudioFile> importedFileList = AppContext.getInstance().getImportAudioService().getImportedFileList();
        importedFileList.sort((file1, file2) -> {
            long result = file1.getFile().lastModified() - file2.getFile().lastModified();
            return result < 0L ? -1 : (result > 0L ? 1 : 0);
        });
        
        JsonObjectBuilder response = Json.createObjectBuilder();
        JsonArrayBuilder items = Json.createArrayBuilder();
        for (ImportAudioFile importedFile : importedFileList) {
            JsonObjectBuilder item = Json.createObjectBuilder()
                    .add("file", importedFile.getFile().getName());
            if (importedFile.getTitle() != null) {
                item.add("title", importedFile.getTitle());
            }
            if (importedFile.getAlbum() != null) {
                item.add("album", importedFile.getAlbum());
            }
            if (importedFile.getArtist() != null) {
                item.add("artist", importedFile.getArtist());
            }
            if (importedFile.getAlbumArtist() != null) {
                item.add("albumArtist", importedFile.getAlbumArtist());
            }
            if (importedFile.getOrder() != null) {
                item.add("order", importedFile.getOrder());
            }
            if (importedFile.getYear() != null) {
                item.add("year", importedFile.getYear());
            }
            items.add(item);
        }
        response.add("files", items);
        
        return renderJson(response);
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
            @FormParam("albumArtist") String albumArtist,
            @FormParam("album") String album,
            @FormParam("title") String title,
            @FormParam("directory") String directoryId) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input
        Validation.required(fileName, "file");
        artist = Validation.length(artist, "artist", 1, 1000);
        albumArtist = Validation.length(albumArtist, "album_artist", 0, 1000, true);
        album = Validation.length(album, "album", 1, 1000);
        title = Validation.length(title, "title", 1, 2000);
        Integer order = null;
        if (orderStr != null) {
            order = Validation.integer(orderStr, "order");
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
        return okJson();
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
        Validation.required(fileName, "file");
        
        // Retrieve the file from imported files
        File file = null;
        for (ImportAudioFile importedFile : AppContext.getInstance().getImportAudioService().getImportedFileList()) {
            if (importedFile.getFile().getName().equals(fileName)) {
                file = importedFile.getFile();
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
        return okJson();
    }
}
