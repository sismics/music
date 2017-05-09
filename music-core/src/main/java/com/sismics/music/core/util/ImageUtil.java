package com.sismics.music.core.util;

import org.imgscalr.Scalr;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class to manage image files.
 *
 * @author jtremeaux
 */
public class ImageUtil {
    /**
     * Supported file types.
     */
    public enum FileType {
        JPG,
        
        GIF,
        
        PNG,

        BMP
    };
    
    // Related to alpha channel removal
    private static final int[] RGB_MASKS = {0xFF0000, 0xFF00, 0xFF};
    private static final ColorModel RGB_OPAQUE = new DirectColorModel(32, RGB_MASKS[0], RGB_MASKS[1], RGB_MASKS[2]);
    
    /**
     * Detects the image format from its contents.
     * 
     * @param file Image file
     * @return File twpe
     */
    public static FileType getFileFormat(File file) throws Exception {
        // Load the file header
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            byte[] headerBytes = new byte[64];
            int readCount = is.read(headerBytes, 0, headerBytes.length);
            if (readCount <= 0) {
                throw new Exception("Cannot read input file");
            }
            String header = new String(headerBytes, "US-ASCII");
            
            if (header.startsWith("GIF87a") || header.startsWith("GIF89a")) {
                return FileType.GIF;
            }
            if (headerBytes[0] == ((byte) 0xff) && headerBytes[1] == ((byte) 0xd8)) {
                return FileType.JPG;
            }
            if (headerBytes[0] == ((byte) 0x89) && headerBytes[1] == ((byte) 0x50) && headerBytes[2] == ((byte) 0x4e) && headerBytes[3] == ((byte) 0x47) &&
                    headerBytes[4] == ((byte) 0x0d) && headerBytes[5] == ((byte) 0x0a) && headerBytes[6] == ((byte) 0x1a) && headerBytes[7] == ((byte) 0x0a)) {
                return FileType.PNG;
            }
            if (headerBytes[0] == ((byte) 0x42) && headerBytes[1] == ((byte) 0x4d)) {
                return FileType.BMP;
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return null;
    }

    /**
     * Resize an image to imageSize x imageSize.
     *
     * @param originalImage Image to resize
     * @param resizedImageMaxSize Target size
     * @return Resized image
     */
    public static BufferedImage resizeImage(BufferedImage originalImage, int resizedImageMaxSize) throws Exception {
        return resizeImage(originalImage, resizedImageMaxSize, null, null, null, null, 0);
    }

    /**
     * Crop and resize an image to imageSize x imageSize.
     * 
     * @param originalImage Image to resize
     * @param resizedImageMaxSize Target size
     * @param x X coordinate to crop from
     * @param y Y coordinate to crop from
     * @param w Width of the cropped image
     * @param h Height of the cropped image
     * @return Resized image
     */
    public static BufferedImage resizeImage(BufferedImage originalImage, Integer resizedImageMaxSize,
            Integer x, Integer y, Integer w, Integer h) throws Exception {
        return resizeImage(originalImage, resizedImageMaxSize, x, y, w, h, 0);
    }

    /**
     * Crop and resize an image to imageSize x imageSize, using the orienation from the EXIF format.
     *
     * @param originalImage Image to resize
     * @param resizedImageMaxSize Target size
     * @param x X coordinate to crop from
     * @param y Y coordinate to crop from
     * @param w Width of the cropped image
     * @param h Height of the cropped image
     * @param orientation Orientation in the EXIF format
     * @return Resized image
     */
    public static BufferedImage resizeImage(BufferedImage originalImage, Integer resizedImageMaxSize,
            Integer x, Integer y, Integer w, Integer h, int orientation) throws Exception {
        // Crop
        BufferedImage croppedImage = originalImage;
        if (x != null && y != null && w != null && h != null) {
            croppedImage = Scalr.crop(originalImage, x, y, w, h);
            originalImage.flush();
        }
        
        // Resize
        BufferedImage resizedImage = Scalr.resize(croppedImage, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, resizedImageMaxSize, Scalr.OP_ANTIALIAS);
        croppedImage.flush();
        
        // Reorient
        BufferedImage rotatedImage = resizedImage;
        if (orientation == 3) {
            rotatedImage = Scalr.rotate(resizedImage, Scalr.Rotation.CW_180);
        } else if (orientation == 6) {
            rotatedImage = Scalr.rotate(resizedImage, Scalr.Rotation.CW_90);
        } else if (orientation == 8) {
            rotatedImage = Scalr.rotate(resizedImage, Scalr.Rotation.CW_270);
        }
        
        if (rotatedImage != resizedImage) {
            resizedImage.flush();
        }
        
        return rotatedImage;
    }
    
    /**
     * Converts the image to a high quality JPEG.
     * 
     * @param image Image to convert
     * @param file Output file
     */
    public static void writeJpeg(BufferedImage image, File file) throws IOException {
        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = null;
        FileImageOutputStream output = null;
        try {
            writer = (ImageWriter) iter.next();
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(1.f);
            output = new FileImageOutputStream(file);
            writer.setOutput(output);
            IIOImage iioImage = new IIOImage(image, null, null);
            writer.write(null, iioImage, iwp);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (Exception inner) {
                    // NOP
                }
            }
            if (writer != null) {
                writer.dispose();
            }
        }
    }

    /**
     * Read an image and remove the alpha channel.
     * @param file Image file
     * @return Image without alpha channel
     */
    public static BufferedImage readImageWithoutAlphaChannel(File file) throws Exception {
        Image img = Toolkit.getDefaultToolkit().createImage(file.getAbsolutePath());
        PixelGrabber pg = new PixelGrabber(img, 0, 0, -1, -1, true);
        pg.grabPixels();
        int width = pg.getWidth(), height = pg.getHeight();
        DataBuffer buffer = new DataBufferInt((int[]) pg.getPixels(), pg.getWidth() * pg.getHeight());
        WritableRaster raster = Raster.createPackedRaster(buffer, width, height, width, RGB_MASKS, null);
        return new BufferedImage(RGB_OPAQUE, raster, false, null);
    }
    
    /**
     * Make a mosaic from 1 to 4 images.
     * @param imageList List of images
     * @param size Final size
     * @return Mosaic
     */
    public static BufferedImage makeMosaic(List<BufferedImage> imageList, int size) throws Exception {
        if (imageList.size() == 0) {
            // Return a 1x1 pixel transparent image
            BufferedImage mosaicImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D mosaicGraphic = mosaicImage.createGraphics();
            mosaicGraphic.setColor(new Color(0, 0, 0, 0));
            mosaicGraphic.fillRect(0, 0, 1, 1);
            return mosaicImage;
        }
        
        if (imageList.size() == 1) {
            return imageList.get(0);
        }
        
        if (imageList.size() > 4) {
            imageList = imageList.subList(0, 4);
        }
        
        BufferedImage mosaicImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D mosaicGraphic = mosaicImage.createGraphics();
        
        int i = 0;
        for (BufferedImage image : imageList) {
            if (i == 0 && imageList.size() == 3 || ((i == 0 || i == 1) && imageList.size() == 2)) {
                image = Scalr.crop(image, (image.getWidth() - size / 2) / 2, 0, size / 2, image.getHeight());
                mosaicGraphic.drawImage(image, null, size / 2 * i, 0);
            }
            if (imageList.size() == 4 || imageList.size() == 3 && (i == 1 || i == 2)) {
                image = Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_TO_WIDTH, size / 2, Scalr.OP_ANTIALIAS);
                mosaicGraphic.drawImage(image, null,
                        imageList.size() == 3 && i > 0 || imageList.size() == 4 && i > 1 ? size / 2 : 0,
                        imageList.size() == 3 && i == 1 || imageList.size() == 4 && i % 2 == 0 ? 0 : size / 2);
            }
            i++;
        }
        
        return mosaicImage;
    }
}
