package com.sismics.music.core.service.importaudio;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.sismics.music.core.model.context.AppContext;

/**
 * Test of the import audio service.
 * 
 * @author bgamard
 */
public class TestImportAudioService {
    @Test
    @Ignore // youtube-dl and ffmpeg are not installed on Travis
    public void testDependencies() throws Exception {
        Assert.assertEquals("2014.08.05", AppContext.getInstance().getImportAudioService().getYoutubeDlVersion());
        Assert.assertEquals("N-60899-ga8ad7e4", AppContext.getInstance().getImportAudioService().getFfmpegVersion());
    }
}
