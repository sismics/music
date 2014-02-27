package com.sismics.util;

import java.io.File;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 *
 * @author jtremeaux
 */
public class TestJaudioTagger {
    @Test
    public void jaudioTaggerTest() throws Exception {
        File file = new File(getClass().getResource("/music/01 The Revolution Will Not Be Televised.mp3").toURI().getPath());
        AudioFile f = AudioFileIO.read(file);
        Tag tag = f.getTag();
        AudioHeader header = f.getAudioHeader();
        Assert.assertNotNull(header.getTrackLength());
        Assert.assertNotNull(header.getSampleRateAsNumber());
        Assert.assertEquals("Gil Scott-Heron", tag.getFirst(FieldKey.ARTIST));
    }
}
