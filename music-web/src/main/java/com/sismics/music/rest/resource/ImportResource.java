package com.sismics.music.rest.resource;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.google.common.collect.Lists;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.service.importaudio.ImportAudio;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;

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
    public Response list() {
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
}
