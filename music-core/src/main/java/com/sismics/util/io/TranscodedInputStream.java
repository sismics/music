package com.sismics.util.io;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /**
     * Constructor of TranscodedInputStream.
     *
     * @param processBuilder Builder to create the transcoder process
     * @throws IOException
     */
    public TranscodedInputStream(ProcessBuilder processBuilder) throws IOException {
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("Starting transcoder: ");
            for (String s : processBuilder.command()) {
                sb.append(s).append(" ");
            }
            log.info(sb.toString());
        }

        // Start the transcoding process process
        process = processBuilder.start();
        processInputStream = process.getInputStream();

        // Consume the transcoder process error stream
        final String commandName = processBuilder.command().get(0);
        new InputStreamReaderThread(process.getErrorStream(), commandName).start();
    }

    @Override
    public int read() throws IOException {
        return processInputStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return processInputStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return processInputStream.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        if (process != null) {
            // Destroying the process closes the streams
            process.destroy();
        }
    }
}
