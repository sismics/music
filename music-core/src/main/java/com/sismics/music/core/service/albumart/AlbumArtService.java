package com.sismics.music.core.service.albumart;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractService;
import com.sismics.music.core.util.DirectoryUtil;
import com.sismics.music.core.util.ImageUtil;

/**
 * Album art service.
 *
 * @author jtremeaux
 */
public class AlbumArtService  {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AbstractService.class);

    /**
     * Import the album art into the application.
     * 
     * @param originalFile File to import
     * @return Album art ID
     * @throws Exception
     */
    public String importAlbumArt(File originalFile) throws Exception {
        String id = UUID.randomUUID().toString();
        for (AlbumArtSize albumArtSize : AlbumArtSize.values()) {
            importAlbumArt(id, originalFile, albumArtSize);
        }
        return id;
    }
    
    /**
     * Import the album art into the application.
     * 
     * @param id ID of the album art
     * @param originalFile File to import
     * @param albumArtSize Album art size
     * @throws Exception
     */
    protected void importAlbumArt(String id, File originalFile, AlbumArtSize albumArtSize) throws Exception {
        ImageUtil.FileType fileType = ImageUtil.getFileFormat(originalFile);
        if (fileType == null) {
            throw new Exception("Unknown file format for picture " + originalFile.getName());
        }
        BufferedImage originalImage = ImageUtil.readImageWithoutAlphaChannel(originalFile);
        String albumArtFileName = getAlbumArtFileName(id, albumArtSize);
        File albumArtFile = new File(DirectoryUtil.getAlbumArtDirectory() + File.separator + albumArtFileName);

        BufferedImage resizedImage = ImageUtil.resizeImage(originalImage, albumArtSize.getSize());
        ImageUtil.writeJpeg(resizedImage, albumArtFile);
    }
    
    /**
     * Delete all album art files with this ID.
     * 
     * @param id ID of the album art
     */
    public void deleteAlbumArt(String id) {
        for (AlbumArtSize albumArtSize : AlbumArtSize.values()) {
            deleteAlbumArt(id, albumArtSize);
        }
    }
    
    /**
     * Delete an album art file.
     *
     * @param id Album art ID
     * @param albumArtSize Album art ID
     */
    protected void deleteAlbumArt(String id, AlbumArtSize albumArtSize) {
        File albumArtFile = getAlbumArtFile(id, albumArtSize);
        try {
            albumArtFile.delete();
        } catch (Exception e) {
            log.error("Album art cannot be deleted: " + albumArtFile.getAbsolutePath());
        }
    }

    /**
     * Return an album art file.
     *
     * @param id Album art ID
     * @param albumArtSize Album art ID
     * @return Album art file
     */
    public File getAlbumArtFile(String id, AlbumArtSize albumArtSize) {
        String albumArtFileName = getAlbumArtFileName(id, albumArtSize);
        return new File(DirectoryUtil.getAlbumArtDirectory() + File.separator + albumArtFileName);
    }

    /**
     * Return the album art file name.
     *
     * @param id Album art ID
     * @param albumArtSize Album art size
     * @return File name
     */
    public String getAlbumArtFileName(String id, AlbumArtSize albumArtSize) {
        return id + "_" + albumArtSize.name().toLowerCase();
    }
}
