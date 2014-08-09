package com.sismics.util;


import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test of the lyrics utilities.
 * 
 * @author bgamard
 */
public class TestLyricUtil {

    @Test
    public void getLyrics() throws Exception {
        String lyrics = LyricUtil.getLyrics("John Lennon", "Imagine");
        Assert.assertTrue(lyrics.contains("And no religion too"));
        
        lyrics = LyricUtil.getLyrics("Perfume", "Spring of Life");
        Assert.assertTrue(lyrics.contains("思い出は空白のままで"));
        Assert.assertTrue(lyrics.contains("Omoide wa kuuhaku no mama de"));
        
        try {
            lyrics = LyricUtil.getLyrics("Bob and Alice", "Cryptomusic");
            Assert.fail();
        } catch (IOException e) {
            // NOP
        }
    }
}
