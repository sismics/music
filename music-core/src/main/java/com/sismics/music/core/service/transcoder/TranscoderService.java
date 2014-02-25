package com.sismics.music.core.service.transcoder;

import com.sismics.music.core.model.dbi.Track;
import com.sismics.util.io.TranscodedInputStream;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Transcoder service.
 *
 * @author jtremeaux
 */
public class TranscoderService {
    /**
     * Returns a transcoded stream for a track.
     *
     * @param track Track to transcode
     * @param seek Time to seek (in seconds)
     * @return Transcoded input stream
     * @throws Exception
     */
    public InputStream getTranscodedInputStream(Track track, int seek) throws Exception {
        final File file = new File(track.getFileName());
        ProcessBuilder pb = getProcessBuilder(track, seek);
        return new TranscodedInputStream(pb, new FileInputStream(file), null);
    }

    /**
     * Returns a transcoded stream for a track.
     *
     * @param track Track to transcode
     * @return Transcoded input stream
     * @throws Exception
     */
    public InputStream getTranscodedInputStream(Track track) throws Exception {
        return getTranscodedInputStream(track, 0);
    }

    private ProcessBuilder getProcessBuilder(Track track, int seek) {
        // TODO Create process builder from settings
        String command = "ffmpeg -i %s -ab %bk -v 0 -f mp3 -ss %ss -";
        List<String> result = new LinkedList<String>(Arrays.asList(StringUtils.split(command)));
        //result.set(0, getTranscodeDirectory().getPath() + File.separatorChar + result.get(0));

        int maxBitRate = 128;
        for (int i = 1; i < result.size(); i++) {
            String cmd = result.get(i);
            if (cmd.contains("%b")) {
                cmd = cmd.replace("%b", String.valueOf(maxBitRate));
            }
            if (cmd.contains("%ss")) {
                cmd = cmd.replace("%ss", String.valueOf(seek));
            }
//            if (cmd.contains("%t")) {
//                cmd = cmd.replace("%t", title);
//            }
//            if (cmd.contains("%l")) {
//                cmd = cmd.replace("%l", album);
//            }
//            if (cmd.contains("%a")) {
//                cmd = cmd.replace("%a", artist);
//            }
//            if (cmd.contains("%o") && videoTranscodingSettings != null) {
//                cmd = cmd.replace("%o", String.valueOf(videoTranscodingSettings.getTimeOffset()));
//            }
//            if (cmd.contains("%d") && videoTranscodingSettings != null) {
//                cmd = cmd.replace("%d", String.valueOf(videoTranscodingSettings.getDuration()));
//            }
//            if (cmd.contains("%w") && videoTranscodingSettings != null) {
//                cmd = cmd.replace("%w", String.valueOf(videoTranscodingSettings.getWidth()));
//            }
//            if (cmd.contains("%h") && videoTranscodingSettings != null) {
//                cmd = cmd.replace("%h", String.valueOf(videoTranscodingSettings.getHeight()));
//            }
            if (cmd.contains("%s")) {

                // Work-around for filename character encoding problem on Windows.
                // Create temporary file, and feed this to the transcoder.
                String path = track.getFileName();
//                if (Util.isWindows() && !mediaFile.isVideo() && !StringUtils.isAsciiPrintable(path)) {
//                    tmpFile = File.createTempFile("subsonic", "." + FilenameUtils.getExtension(path));
//                    tmpFile.deleteOnExit();
//                    FileUtils.copyFile(new File(path), tmpFile);
//                    LOG.debug("Created tmp file: " + tmpFile);
//                    cmd = cmd.replace("%s", tmpFile.getPath());
//                } else {
                    cmd = cmd.replace("%s", path);
//                }
            }

            result.set(i, cmd);
        }
        return new ProcessBuilder(result);
    }
}
