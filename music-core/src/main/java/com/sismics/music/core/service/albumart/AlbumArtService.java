package com.sismics.music.core.service.albumart;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AbstractService;
import com.sismics.music.core.dao.dbi.AlbumDao;
import com.sismics.music.core.dao.dbi.ArtistDao;
import com.sismics.music.core.dao.dbi.DirectoryDao;
import com.sismics.music.core.model.dbi.Album;
import com.sismics.music.core.model.dbi.Artist;
import com.sismics.music.core.model.dbi.Directory;
import com.sismics.music.core.util.DirectoryNameParser;
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
     * @param album Album
     * @param originalFile File to import
     * @param copyOriginal If true, copy the original file to the album directory
     * @return Album art ID
     * @throws Exception
     */
    public String importAlbumArt(Album album, File originalFile, boolean copyOriginal) throws Exception {
        ImageUtil.FileType fileType = ImageUtil.getFileFormat(originalFile);
        if (fileType == null) {
            throw new Exception("Unknown file format for picture " + originalFile.getName());
        }
        BufferedImage originalImage = ImageUtil.readImageWithoutAlphaChannel(originalFile);
        
        String id = UUID.randomUUID().toString();
        for (AlbumArtSize albumArtSize : AlbumArtSize.values()) {
            importAlbumArt(id, originalImage, albumArtSize);
        }
        
        if (copyOriginal) {
            // Copy the original file to the album directory
            ArtistDao artistDao = new ArtistDao();
            DirectoryDao directoryDao = new DirectoryDao();
            Artist artist = artistDao.getActiveById(album.getArtistId());
            Directory directory = directoryDao.getActiveById(album.getDirectoryId());
            Path albumArtPath = Paths.get(directory.getLocation(), new DirectoryNameParser(artist.getName(), album.getName()).getFileName(), "albumart.jpg");
            ImageUtil.writeJpeg(originalImage, albumArtPath.toFile());
        }
        
        return id;
    }
    
    /**
     * Import the album art into the application.
     * 
     * @param id ID of the album art
     * @param originalImage Original image
     * @param albumArtSize Album art size
     * @throws Exception
     */
    protected void importAlbumArt(String id, BufferedImage originalImage, AlbumArtSize albumArtSize) throws Exception {
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
    
    /**
     * Rebuilds all album art sizes.
     * 
     * @throws Exception 
     */
    public void rebuildAlbumArt() throws Exception {
        AlbumDao albumDao = new AlbumDao();
        ArtistDao artistDao = new ArtistDao();
        DirectoryDao directoryDao = new DirectoryDao();
        Set<String> albumArtIdSet = Sets.newHashSet();
        
        // List all album art IDs
        for (File file : DirectoryUtil.getAlbumArtDirectory().listFiles()) {
            String[] fileName = file.getName().split("_");
            albumArtIdSet.add(fileName[0]);
        }
        
        // For each album art, rebuild the smaller sizes from the large size
        // and copy the large file to the album directory
        for (String id : albumArtIdSet) {
            File largeFile = getAlbumArtFile(id, AlbumArtSize.LARGE);
            BufferedImage largeImage = ImageUtil.readImageWithoutAlphaChannel(largeFile);
            
            File mediumFile = getAlbumArtFile(id, AlbumArtSize.MEDIUM);
            if (mediumFile.exists()) {
                mediumFile.delete();
            }
            
            File smallFile = getAlbumArtFile(id, AlbumArtSize.SMALL);
            if (smallFile.exists()) {
                smallFile.delete();
            }
            
            importAlbumArt(id, largeImage, AlbumArtSize.MEDIUM);
            importAlbumArt(id, largeImage, AlbumArtSize.SMALL);
            
            // Copy to album directory
            Album album = albumDao.getActiveByAlbumArtId(id);
            if (album == null) {
                continue;
            }
            Artist artist = artistDao.getActiveById(album.getArtistId());
            Directory directory = directoryDao.getActiveById(album.getDirectoryId());
            Path albumArtPath = Paths.get(directory.getLocation(), new DirectoryNameParser(artist.getName(), album.getName()).getFileName(), "albumart.jpg");
            ImageUtil.writeJpeg(largeImage, albumArtPath.toFile());
        }
    }
}
