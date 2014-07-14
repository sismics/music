package com.sismics.music.rest.resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

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
import com.sismics.util.FilenameUtil;

/**
 * Import REST resources.
 * 
 * @author bgamard
 */
@Path("/import")
public class ImportResource extends BaseResource {
    /**
     * Import new tracks.
     * 
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
        
        // Check if youtube-dl is present
        if (!AppContext.getInstance().getImportAudioService().checkPrerequisites()) {
            throw new ServerException("DependencyError", "youtube-dl is necessary to import audio. Download and install it at: http://rg3.github.io/youtube-dl/");
        }
        
        // Add the URL to the import queue
        AppContext.getInstance().getImportAudioService().downloadAudio(urlList, quality, format);
        
        // Always return OK
        return Response.ok()
                .entity(Json.createObjectBuilder().add("status", "ok").build())
                .build();
    }
    
    
    /**
     * List imports in progress.
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
        ValidationUtil.validateLength(artist, "artist", 1, 1000);
        ValidationUtil.validateLength(albumArtist, "album_artist", 1, 1000);
        ValidationUtil.validateLength(album, "album", 1, 1000);
        ValidationUtil.validateLength(title, "title", 1, 2000);
        
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
        
        // Tag the file
        try {
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTagOrCreateAndSetDefault();
            tag.setField(FieldKey.TITLE, title);
            tag.setField(FieldKey.ALBUM, album);
            tag.setField(FieldKey.ARTIST, artist);
            tag.setField(FieldKey.ALBUM_ARTIST, albumArtist);
            AudioFileIO.write(audioFile);
        } catch (Exception e) {
            throw new ServerException("TagError", "Error tagging the file", e);
        }
        
        // Create album directory
        String albumDirectory = FilenameUtil.cleanFileName(albumArtist) + " - " + FilenameUtil.cleanFileName(album);
        Paths.get(directory.getLocation(), albumDirectory).toFile().mkdirs();
        
        // Move the file to the right place and let to collection watch service index it
        String extension = Files.getFileExtension(fileName);
        java.nio.file.Path path = Paths.get(directory.getLocation(),
                albumDirectory,
                FilenameUtil.cleanFileName(title) + "." + extension);
        try {
            Files.move(file, path.toFile());
        } catch (IOException e) {
            throw new ServerException("FileError", "Cannot move the imported file to the music directory", e);
        }
        
        // Always return OK
        return Response.ok()
                .entity(Json.createObjectBuilder().add("status", "ok").build())
                .build();
    }
    
    /**
     * Cleanup finished imports.
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
    };
    
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
