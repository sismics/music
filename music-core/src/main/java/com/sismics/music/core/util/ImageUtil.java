package com.sismics.music.core.util;

import org.imgscalr.Scalr;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

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
        
        PNG

        // TODO add BMP
    };
    
    /**
     * Detects the image format from its contents.
     * 
     * @param file Image file
     * @return File twpe
     * @throws Exception
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
     * @throws Exception
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
     * @throws Exception
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
     * @throws Exception
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
     * @throws java.io.IOException
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
}
