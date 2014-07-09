package com.sismics.music.core.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Test;

import com.sismics.music.core.util.ImageUtil.FileType;

public class TestImageUtil {
    @Test
    public void testBitmap() throws Exception {
        File file = new File(getClass().getResource("/albumart/image.bmp").toURI());
        FileType fileType = ImageUtil.getFileFormat(file);
        Assert.assertEquals(FileType.BMP, fileType);
    }
    
    @Test
    public void testMakeMosaic() throws Exception {
        File dir = new File(getClass().getResource("/artistart/").toURI());
        List<BufferedImage> imageList = new ArrayList<>();
        for (File file : dir.listFiles()) {
            imageList.add(ImageIO.read(file));
        }
        
        Assert.assertNotNull(ImageUtil.makeMosaic(imageList.subList(0, 1), 330));
        Assert.assertNotNull(ImageUtil.makeMosaic(imageList.subList(0, 2), 330));
        Assert.assertNotNull(ImageUtil.makeMosaic(imageList.subList(0, 3), 330));
        Assert.assertNotNull(ImageUtil.makeMosaic(imageList.subList(0, 4), 330));
    }
}
