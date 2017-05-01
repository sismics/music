package com.sismics.music.rest.util;

import com.sismics.rest.exception.ClientException;
import com.sismics.rest.util.Validation;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the validations.
 *
 * @author jtremeaux 
 */
public class TestValidationUtil {
    @Test
    public void testValidateHttpUrlFail() throws Exception {
        Validation.httpUrl("http://www.google.com", "url");
        Validation.httpUrl("https://www.google.com", "url");
        Validation.httpUrl(" https://www.google.com ", "url");
        try {
            Validation.httpUrl("ftp://www.google.com", "url");
            Assert.fail();
        } catch (ClientException e) {
            // NOP
        }
        try {
            Validation.httpUrl("http://", "url");
            Assert.fail();
        } catch (ClientException e) {
            // NOP
        }
    }
}
