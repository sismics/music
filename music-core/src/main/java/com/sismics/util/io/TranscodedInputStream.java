package com.sismics.util.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream that transcodes on the fly.
 * 
 * @author jtremeaux
 */
public class TranscodedInputStream extends InputStream {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(TranscodedInputStream.class);

    private InputStream processInputStream;

    private Process process;
    
    private int fileSize;

    private int readBytes;
    
    /**
     * Constructor of TranscodedInputStream.
     *
     * @param processBuilder Builder to create the transcoder process
     * @param fileSize Expected transcoded file size
     */
    public TranscodedInputStream(ProcessBuilder processBuilder, int fileSize) throws IOException {
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("Starting transcoder: ");
            for (String s : processBuilder.command()) {
                sb.append(s).append(" ");
            }
            log.debug(sb.toString());
        }
        
        this.fileSize = fileSize;
        this.readBytes = 0;

        // Start the transcoding process process
        process = processBuilder.start();
        processInputStream = process.getInputStream();

        // Consume the transcoder process error stream
        final String commandName = processBuilder.command().get(0);
        new InputStreamReaderThread(process.getErrorStream(), commandName).start();
    }

    @Override
    public int read() throws IOException {
        int b = processInputStream.read();
        if (b == -1 && readBytes < fileSize) {
            // Nothing more from the transcoder, but not enough bytes, send a zero
            readBytes++;
            return 0;
        }
        return b;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int n = processInputStream.read(b);
        if (n == -1 && readBytes < fileSize) {
            // Nothing more from the transcoder, but not enough bytes, send some zeros
            int i = 0;
            for (; i < b.length; i++) {
                if (readBytes < fileSize) {
                    readBytes++;
                    b[i] = 0;
                }
            }
            return i;
        }
        readBytes += n;
        return n;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = processInputStream.read(b, off, len);
        if (n == -1 && readBytes < fileSize) {
            // Nothing more from the transcoder, but not enough bytes, send some zeros
            int i = 0;
            for (; i < b.length; i++) {
                if (readBytes < fileSize) {
                    readBytes++;
                    b[i] = 0;
                }
            }
            return i;
        }
        readBytes += n;
        return n;
    }

    @Override
    public void close() throws IOException {
        if (process != null) {
            // Destroying the process closes the streams
            process.destroy();
        }
    }
}
