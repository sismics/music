package com.sismics.music.core.service.albumart;

import com.google.common.util.concurrent.AbstractService;
import com.sismics.music.core.util.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.UUID;

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
        BufferedImage originalImage = ImageIO.read(originalFile);
        String albumArtFileName = getAlbumArtFileName(id, albumArtSize);
        File albumArtFile = new File(getAlbumArtDir() + File.separator + albumArtFileName);

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
        return new File(getAlbumArtDir() + File.separator + albumArtFileName);
    }
    /**
     * Return the storage directory of album art files.
     * 
     * @return Album art directory
     */
    public File getAlbumArtDir() {
        // TODO configure album art dir
//        ConfigDao configDao = new ConfigDao();
//        Config albumArtDirConfig = configDao.getById(ConfigType.PROFILE_PICTURE_DIR);
        File albumArtDir = null;
//        if (albumArtDirConfig != null) {
//            // If the directory is specified in a configuration parameter, use this directory
//            albumArtDir = new File(albumArtDirConfig.getValue());
//        } else {
            String webappRoot = System.getProperty("webapp.root");
            if (webappRoot != null) {
                // Or else if we are in a Jetty context, use the root directory of Jetty
                albumArtDir = new File(webappRoot + File.separator + "albumart");
                if (!albumArtDir.isDirectory()) {
                    albumArtDir.mkdir();
                }
            } else {
                // Or else (for unit tests), use the temporary directory
                albumArtDir = new File(System.getProperty("java.io.tmpdir"));
            }
//        }
        
        return albumArtDir;
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
