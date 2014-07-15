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
    @Ignore // youtube-dl is not installed on Travis
    public void testCheckPrerequisites() throws Exception {
        Assert.assertEquals("2014.07.11.3", AppContext.getInstance().getImportAudioService().checkPrerequisites());
    }
}
