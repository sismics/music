package com.sismics.music.core.util;

import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test directory name parser.
 * 
 * @author bgamard
 */
public class TestDirectoryNameParser {
    @Test
    public void testParsing() throws Exception {
        Assert.assertEquals("Capsule", new DirectoryNameParser(Paths.get("Capsule - World of Fantasy")).getArtistName());
        Assert.assertEquals("World of Fantasy", new DirectoryNameParser(Paths.get("Capsule - World of Fantasy")).getAlbumName());
        
        Assert.assertEquals("Berryz工房", new DirectoryNameParser(Paths.get("Berryz工房 - Berryz Mansion 9 Kai")).getArtistName());
        Assert.assertEquals("Berryz Mansion 9 Kai", new DirectoryNameParser(Paths.get("Berryz工房 - Berryz Mansion 9 Kai")).getAlbumName());
        
        Assert.assertEquals("La Rumeur", new DirectoryNameParser(Paths.get("La Rumeur - 2ème Volet - Le Franc Tireur")).getArtistName());
        Assert.assertEquals("2ème Volet - Le Franc Tireur", new DirectoryNameParser(Paths.get("La Rumeur - 2ème Volet - Le Franc Tireur")).getAlbumName());
        
        Assert.assertEquals("Unknown", new DirectoryNameParser(Paths.get("_divers")).getArtistName());
        Assert.assertEquals("_divers", new DirectoryNameParser(Paths.get("_divers")).getAlbumName());
        
        Assert.assertEquals("Unknown", new DirectoryNameParser(Paths.get("Ran-Dom")).getArtistName());
        Assert.assertEquals("Ran-Dom", new DirectoryNameParser(Paths.get("Ran-Dom")).getAlbumName());
    }
    
    @Test
    public void testRebuilding() throws Exception {
        Assert.assertEquals("Capsule - World of Fantasy", new DirectoryNameParser("Capsule", "World of Fantasy").getFileName());
        
        Assert.assertEquals("Berryz工房 - Berryz Mansion 9 Kai", new DirectoryNameParser("Berryz工房", "Berryz Mansion 9 Kai").getFileName());
        
        Assert.assertEquals("La Rumeur - 2ème Volet - Le Franc Tireur", new DirectoryNameParser("La Rumeur", "2ème Volet - Le Franc Tireur").getFileName());
        
        Assert.assertEquals("_divers", new DirectoryNameParser("Unknown", "_divers").getFileName());
        
        Assert.assertEquals("Ran-Dom", new DirectoryNameParser("Unknown", "Ran-Dom").getFileName());
    }
}
