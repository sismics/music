package com.sismics.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;

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

    private OutputStream processOutputStream;

    private Process process;

    private final Closer closer = Closer.create();

    /**
     * Constructor of TranscodedInputStream.
     *
     * @param processBuilder Builder to create the transcoder process
     * @param is Input stream to transcode
     * @throws IOException
     */
    public TranscodedInputStream(ProcessBuilder processBuilder, final InputStream is) throws IOException {
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("Starting transcoder: ");
            for (String s : processBuilder.command()) {
                sb.append(s).append(" ");
            }
            log.info(sb.toString());
        }

        // Start the transcoding process process
        process = processBuilder.start();
        processOutputStream = closer.register(process.getOutputStream());
        processInputStream = process.getInputStream();

        // Consume the transcoder process error stream
        final String commandName = processBuilder.command().get(0);
        new InputStreamReaderThread(process.getErrorStream(), commandName).start();

        // Consume the transcoder process output stream
        closer.register(is);
        new Thread(commandName + " TranscodedInputStream thread") {
            @Override
            public void run() {
                try {
                    ByteStreams.copy(is, processOutputStream);
                } catch (IOException e) {
                    // NOP
                } finally {
                    try {
                        closer.close();
                    } catch (Exception e) {
                        // NOP
                    }
                }
            }
        }.start();
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
            process.destroy();
        }
        
        try {
            closer.close();
        } catch (Exception e) {
            // NOP
        }
    }
}
