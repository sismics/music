package com.sismics.util;


import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Test of the lyrics utilities.
 * 
 * @author bgamard
 */
public class TestLyricUtil {

    @Test
    @Ignore
    public void getLyrics() throws Exception {
        List<String> lyrics = LyricUtil.getLyrics("John Lennon", "Imagine");
        Assert.assertEquals(1, lyrics.size());
        Assert.assertTrue(lyrics.get(0).contains("And no religion too"));
        
        lyrics = LyricUtil.getLyrics("Taylor Swift", "I Knew You Were Trouble");
        Assert.assertEquals(1, lyrics.size());
        Assert.assertTrue(lyrics.get(0).contains("Once upon a time, a few mistakes ago"));
        
        lyrics = LyricUtil.getLyrics("Perfume", "Spring of Life");
        Assert.assertEquals(1, lyrics.size());
        Assert.assertTrue(lyrics.get(0).contains("思い出は空白のままで"));

        try {
            lyrics = LyricUtil.getLyrics("Bob and Alice", "Cryptomusic");
            Assert.fail();
        } catch (IOException e) {
            // NOP
        }
    }
}
