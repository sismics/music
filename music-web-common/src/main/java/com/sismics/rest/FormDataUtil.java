package com.sismics.rest;

import com.sismics.rest.exception.ServerException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author jtremeaux
 */
public class FormDataUtil {
    /**
     * Get the body part as a temporary file.
     * Your must delete the temporary file after use.
     *
     * @param fileBodyPart The body part
     * @return The file
     */
    public static File getAsTempFile(FormDataBodyPart fileBodyPart) {
        InputStream in = fileBodyPart.getValueAs(InputStream.class);
        File uploadFile;
        try {
            uploadFile = File.createTempFile("upload", "." + FilenameUtils.getExtension(fileBodyPart.getFormDataContentDisposition().getFileName()));
            try (OutputStream os = new FileOutputStream(uploadFile)) {
                IOUtils.copy(in, os);
            }
        } catch (Exception e) {
            throw new ServerException("UploadError", e.getMessage(), e);
        }
        return uploadFile;
    }
}
