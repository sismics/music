package com.sismics.music.core.service.importaudio;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.sismics.music.core.constant.Constants;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Directory;
import com.sismics.music.core.service.importaudio.ImportAudio.Status;
import com.sismics.music.core.util.DirectoryUtil;
import com.sismics.util.FilenameUtil;
import com.sismics.util.mime.MimeType;
import com.sismics.util.mime.MimeTypeUtil;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.lang.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Import audio service.
 *
 * @author bgamard
 */
public class ImportAudioService extends AbstractExecutionThreadService {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ImportAudioService.class);
    
    /**
     * Blocking queue storing pending import audio.
     */
    private final BlockingQueue<ImportAudio> importAudioQueue = new LinkedBlockingQueue<>();
    
    /**
     * Imported audio status.
     */
    private final List<ImportAudio> importAudioList = Collections.synchronizedList(new ArrayList<ImportAudio>());
    
    /**
     * YouTube-DL progress pattern.
     */
    private Pattern PROGRESS_PATTERN = Pattern.compile("\\[download\\]\\s*([0-9\\.]*)%\\s*of\\s*([0-9\\.a-zA-Z]*)\\s*at\\s*([0-9\\.a-zA-Z/]*).*");
    
    /**
     * YouTube-DL working file pattern.
     */
    private Pattern WORKING_FILE_PATTERN = Pattern.compile("\\[[a-zA-Z]*\\] Destination: (.*)");
    
    /**
     * Single thread executor used to tag files.
     */
    private ExecutorService syncExecutor = Executors.newSingleThreadExecutor();
    
    public ImportAudioService() {
    }

    @Override
    protected void startUp() {
    }
    

    @Override
    protected void run() throws Exception {
        while (isRunning()) {
            // Wait for a new URL to import
            ImportAudio importAudio = importAudioQueue.take();
            
            try {
                log.info("Start importing a new URL: " + importAudio);
                
                // Starting YouTube-DL
                String output = DirectoryUtil.getImportAudioDirectory().getAbsolutePath() + File.separator + "%(title)s.%(ext)s";
                String command = "youtube-dl -v --prefer-ffmpeg --encoding UTF-8 --newline -f bestaudio -x --audio-format " + importAudio.getFormat()
                        + " --audio-quality " + importAudio.getQuality() + " -o";
                List<String> commandList = new LinkedList<>(Arrays.asList(StringUtils.split(command)));
                commandList.add(output);
                commandList.add(importAudio.getUrl());
                Process process = new ProcessBuilder(commandList)
                        .redirectErrorStream(true)
                        .start();
    
                // Import status is now in progress
                importAudio.setStatus(ImportAudio.Status.INPROGRESS);
                importAudio.setProcess(process);
                importAudioList.add(importAudio);
                
                // Reading standard output to update import status
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    for (String line = reader.readLine(); line != null && isProcessRunning(process); line = reader.readLine()) {
                        // Update progression
                        Matcher matcher = PROGRESS_PATTERN.matcher(line);
                        if (matcher.find()) {
                            importAudio.setProgress(Float.valueOf(matcher.group(1)));
                            importAudio.setTotalSize(matcher.group(2));
                            importAudio.setDownloadSpeed(matcher.group(3));
                        }
                        
                        // Check if the line is an error
                        if (line.contains("ERROR")) {
                            importAudio.setStatus(ImportAudio.Status.ERROR);
                        }
                        
                        // New working file
                        matcher = WORKING_FILE_PATTERN.matcher(line);
                        if (matcher.find()) {
                            File file = new File(matcher.group(1));
                            String name = file.getName();
                            if (!Strings.isNullOrEmpty(name)) {
                                importAudio.addWorkingFiles(name);
                            }
                        }
                        
                        // Debug output
                        importAudio.setMessage(importAudio.getMessage() + "\n" + line);
                    }
                }
                
                // The process has not been terminated properly
                if (process.waitFor() != 0) {
                    importAudio.setMessage(importAudio.getMessage() + "\nProcess exit with code: " + process.exitValue());
                    importAudio.setStatus(ImportAudio.Status.ERROR);
                }
                
                // Import is done
                if (importAudio.getStatus() != ImportAudio.Status.ERROR) {
                    importAudio.setStatus(ImportAudio.Status.DONE);
                } else {
                    // Remove working files if they still exist
                    for (String name : importAudio.getWorkingFiles()) {
                        File file = new File(DirectoryUtil.getImportAudioDirectory().getAbsolutePath() + File.separator + name);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                }
                
                process.destroy();
                importAudio.setProcess(null);
            } catch (Exception e) {
                // Error while importing
                importAudio.setStatus(ImportAudio.Status.ERROR);
                importAudio.setMessage(importAudio.getMessage() + "\n" + e.getMessage());
                log.error("Error importing: " + importAudio, e);
            }
        }
    }
    
    /**
     * Returns true if the process is running.
     * 
     * @param process Process
     * @return True if running
     */
    private boolean isProcessRunning(Process process) {
        try {
            process.exitValue();
            return false;
        } catch (Exception e) {
            return true;
        }
    }
    
    @Override
    protected void shutDown() {
    }

    /**
     * Return the version of Youtube-DL.
     * Return null if Youtube-DL is not present.
     * 
     * @return youtube-dl version
     */
    public String getYoutubeDlVersion() {
        try {
            Process process = Runtime.getRuntime().exec("youtube-dl --version");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                return reader.readLine();
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Return the version of FFMPEG.
     * Return null if FFMPEG is not present.
     * 
     * @return ffmpeg version
     */
    public String getFfmpegVersion() {
        try {
            Process process = Runtime.getRuntime().exec("ffmpeg -version");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String version = reader.readLine();
                return version.replace("ffmpeg version ", "");
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Add URL to import from external sources.
     * 
     * @param urlList URL list
     * @param quality Quality
     * @param format Format
     */
    public void downloadAudio(List<String> urlList, String quality, String format) {
        for (String url : urlList) {
            importAudioQueue.add(new ImportAudio(url, quality, format));
        }
    }
    
    /**
     * Return a copied version of the currently importing audio from external sources.
     * 
     * @return Currently importing audio
     */
    public List<ImportAudio> getImportAudioList() {
        List<ImportAudio> copy = new ArrayList<>();
        
        synchronized (importAudioQueue) {
            for (ImportAudio importAudio : importAudioQueue) {
                copy.add(new ImportAudio(importAudio));
            }
        }
        
        synchronized (importAudioList) {
            for (ImportAudio importAudio : importAudioList) {
                copy.add(new ImportAudio(importAudio));
            }
        }
        
        return copy;
    }

    /**
     * Cleanup finished imports from external sources.
     */
    public void cleanup() {
        synchronized (importAudioList) {
            for (Iterator<ImportAudio> it = importAudioList.iterator(); it.hasNext();) {
                ImportAudio importAudio = it.next();
                if (importAudio.getStatus() == ImportAudio.Status.DONE || importAudio.getStatus() == ImportAudio.Status.ERROR) {
                    it.remove();
                }
            }
        }
    }

    /**
     * Retry a failed import from an external source.
     * 
     * @param id ID
     */
    public void retryImportAudio(String id) throws Exception {
        synchronized (importAudioList) {
            for (ImportAudio importAudio : importAudioList) {
                if (importAudio.getId().equals(id)) {
                    if (importAudio.getStatus() != Status.ERROR) {
                        throw new Exception("Import not retryable");
                    }
                    
                    importAudioList.remove(importAudio);
                    importAudioQueue.add(
                            new ImportAudio(importAudio.getUrl(),
                                    importAudio.getQuality(),
                                    importAudio.getFormat()));
                    return;
                }
            }
        }
        
        throw new Exception("Import not found");
    }
    
    /**
     * Kill an in progess import from an external source.
     * 
     * @param id ID
     */
    public void killImportAudio(String id) throws Exception {
        synchronized (importAudioList) {
            for (ImportAudio importAudio : importAudioList) {
                if (importAudio.getId().equals(id)) {
                    if (importAudio.getStatus() != Status.INPROGRESS || importAudio.getProcess() == null) {
                        throw new Exception("Import not killable");
                    }
                    
                    importAudio.getProcess().destroy();
                    return;
                }
            }
        }
        
        throw new Exception("Import not found");
    }
    
    /**
     * Import a file. ZIP or single track are accepted.
     * 
     * @param file File
     */
    public void importFile(File file) throws Exception {
        String mimeType = MimeTypeUtil.guessMimeType(file);
        String ext = Files.getFileExtension(file.getName()).toLowerCase();
        String importDir = DirectoryUtil.getImportAudioDirectory().getAbsolutePath();
        
        if (MimeType.APPLICATION_ZIP.equals(mimeType)) {
            log.info("Importing a ZIP file");
            // It's a ZIP file, unzip accepted files in the import folder
            try (ZipArchiveInputStream archiveInputStream = new ZipArchiveInputStream(new FileInputStream(file), Charsets.UTF_8.name())) {
                ArchiveEntry archiveEntry = archiveInputStream.getNextEntry();
                while (archiveEntry != null) {
                    String archiveExt = Files.getFileExtension(archiveEntry.getName()).toLowerCase();
                    if (Constants.SUPPORTED_AUDIO_EXTENSIONS.contains(archiveExt)) {
                        log.info("Importing: " + archiveEntry.getName());
                        File outputFile = new File(importDir + File.separator + new File(archiveEntry.getName()).getName());
                        try (OutputStream fileOutputStream = new FileOutputStream(outputFile)) {
                            ByteStreams.copy(archiveInputStream, fileOutputStream);
                        }
                    }
                    archiveEntry = archiveInputStream.getNextEntry();
                }
            }
        } else if (Constants.SUPPORTED_AUDIO_EXTENSIONS.contains(ext)) {
            // It should be a single audio track
            File outputFile = new File(importDir + File.separator + file.getName());
            log.info("Importing a single track: " + outputFile);
            Files.copy(file, outputFile);
        } else {
            throw new Exception("File not supported");
        }
    }
    
    /**
     * Return imported files.
     * 
     * @return List of imported files
     */
    public List<ImportAudioFile> getImportedFileList() {
        // Grab all audio files
        List<File> fileList = Lists.newArrayList(DirectoryUtil.getImportAudioDirectory().listFiles((dir, fileName) -> {
            String extension = Files.getFileExtension(fileName);
            return Constants.SUPPORTED_AUDIO_EXTENSIONS.contains(extension);
        }));
        
        // Exclude working files
        synchronized (importAudioList) {
            for (Iterator<File> it = fileList.iterator(); it.hasNext();) {
                File file = it.next();
                for (ImportAudio importAudio : importAudioList) {
                    if (importAudio.getStatus() == Status.INPROGRESS && importAudio.getWorkingFiles().contains(file.getName())) {
                        it.remove();
                    }
                }
            }
        }

        return guessTags(fileList);
    }

    /**
     * Guess the tags from the list of imported files.
     *
     * @param fileList The list of imported files
     * @return The files with guessed tags
     */
    private List<ImportAudioFile> guessTags(List<File> fileList) {
        return fileList.stream()
                .map(this::guessTags)
                .collect(Collectors.toList());
    }

    /**
     * Guess the tags of a single track.
     *
     * @param file The file
     * @return The file with tags
     */
    private ImportAudioFile guessTags(File file) {
        ImportAudioFile importAudioFile = new ImportAudioFile(file);

        try {
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();

            // Read track year
            String year = tag.getFirst(FieldKey.YEAR);
            if (!Strings.isNullOrEmpty(year)) {
                try {
                    importAudioFile.setYear(Integer.valueOf(year));
                } catch (NumberFormatException e) {
                    // Ignore parsing errors
                }
            }

            // Read track title (can be empty string)
            importAudioFile.setTitle(StringUtils.substring(StringUtils.trimToEmpty(tag.getFirst(FieldKey.TITLE)), 0, 2000));

            // Read track artist (can be empty string)
            importAudioFile.setArtist(StringUtils.substring(StringUtils.trimToEmpty(tag.getFirst(FieldKey.ARTIST)), 0, 1000));

            // Read track album artist (can be empty string)
            importAudioFile.setAlbumArtist(StringUtils.abbreviate(tag.getFirst(FieldKey.ALBUM_ARTIST), 0, 1000));
            if (StringUtils.isEmpty(importAudioFile.getAlbumArtist())) {
                importAudioFile.setAlbumArtist(importAudioFile.getArtist());
            }

            // Read track album
            importAudioFile.setAlbum(StringUtils.abbreviate(tag.getFirst(FieldKey.ALBUM), 0, 1000));

            // Read track order
            String order = tag.getFirst(FieldKey.TRACK);
            if (!Strings.isNullOrEmpty(order)) {
                try {
                    importAudioFile.setOrder(Integer.valueOf(order));
                } catch (NumberFormatException e) {
                    // Ignore parsing errors
                }
            }
        } catch (Exception e) {
            // NOP
        }
        return importAudioFile;
    }

    /**
     * Tag and move a file in a directory. This method is thread-safe.
     * 
     * @param fileName File name
     * @param order Order
     * @param title Title
     * @param album Album
     * @param artist Artist
     * @param albumArtist Album artist
     * @param directory Directory
     */
    public void tagFile(final String fileName, final Integer order, final String title, final String album, final String artist,
            final String albumArtist, final Directory directory) throws Exception {
        syncExecutor.submit((Callable<Void>) () -> {
            // Retrieve the file from imported files
            List<ImportAudioFile> importedFileList = AppContext.getInstance().getImportAudioService().getImportedFileList();
            ImportAudioFile importAudioFile = importedFileList.stream()
                    .filter(e -> e.getFile().getName().equals(fileName))
                    .findFirst()
                    .orElseThrow(() -> new Exception("File not found: " + fileName));
            File file = importAudioFile.getFile();

            // Tag the file
            try {
                AudioFile audioFile = AudioFileIO.read(file);
                Tag tag = audioFile.getTagOrCreateAndSetDefault();
                if (order == null) {
                    tag.deleteField(FieldKey.TRACK);
                } else {
                    tag.setField(FieldKey.TRACK, order.toString());
                }
                tag.setField(FieldKey.TITLE, title);
                tag.setField(FieldKey.ALBUM, album);
                tag.setField(FieldKey.ARTIST, artist);
                tag.setField(FieldKey.ALBUM_ARTIST, albumArtist);
                AudioFileIO.write(audioFile);
            } catch (Exception e) {
                throw new Exception("Error tagging the file", e);
            }

            // Create album directory
            String albumDirectory = FilenameUtil.cleanFileName(albumArtist) + " - " + FilenameUtil.cleanFileName(album);
            Paths.get(directory.getLocation(), albumDirectory).toFile().mkdirs();

            // Move the file to the right place and let to collection watch service index it
            String extension = Files.getFileExtension(fileName);
            Path path = Paths.get(directory.getLocation(),
                    albumDirectory,
                    FilenameUtil.cleanFileName(title) + "." + extension);
            try {
                Files.move(file, path.toFile());
            } catch (IOException e) {
                throw new Exception("Cannot move the imported file to the music directory", e);
            }

            return null;
        }).get();
    }
}
