package com.sismics.music.core.service.importaudio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.lang.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    ExecutorService syncExecutor = Executors.newSingleThreadExecutor();
    
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
            log.info("Start importing a new URL: " + importAudio);
            
            // Starting YouTube-DL
            String output = DirectoryUtil.getImportAudioDirectory().getAbsolutePath() + File.separator + "%(title)s.%(ext)s";
            String command = "youtube-dl -v --prefer-ffmpeg --encoding UTF-8 --newline -f bestaudio -x --audio-format " + importAudio.getFormat()
                    + " --audio-quality " + importAudio.getQuality() + " -o";
            List<String> commandList = new LinkedList<String>(Arrays.asList(StringUtils.split(command)));
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
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
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
            if (process.exitValue() != 0) {
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
        }
    }
    
    @Override
    protected void shutDown() {
    }

    /**
     * Check import audio prerequisites to import from external sources.
     * 
     * @return youtube-dl version
     * @throws IOException 
     */
    public String checkPrerequisites() throws IOException {
        Process process = Runtime.getRuntime().exec("youtube-dl --version");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return reader.readLine();
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
     * @throws Exception 
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
     * @throws Exception 
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
     * @throws Exception 
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
    public List<File> getImportedFileList() {
        // Grab all audio files
        List<File> fileList = Lists.newArrayList(DirectoryUtil.getImportAudioDirectory().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String fileName) {
                String extension = Files.getFileExtension(fileName);
                return Constants.SUPPORTED_AUDIO_EXTENSIONS.contains(extension);
            }
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
        
        return fileList;
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
     * @throws Exception
     */
    public void tagFile(final String fileName, final Integer order, final String title, final String album, final String artist,
            final String albumArtist, final Directory directory) throws Exception {
        syncExecutor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // Retrieve the file from imported files
                List<File> importedFileList = AppContext.getInstance().getImportAudioService().getImportedFileList();
                File file = null;
                for (File importedFile : importedFileList) {
                    if (importedFile.getName().equals(fileName)) {
                        file = importedFile;
                        break;
                    }
                }
                
                if (file == null) {
                    throw new Exception("File not found: " + fileName);
                }
                
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
                java.nio.file.Path path = Paths.get(directory.getLocation(),
                        albumDirectory,
                        FilenameUtil.cleanFileName(title) + "." + extension);
                try {
                    Files.move(file, path.toFile());
                } catch (IOException e) {
                    throw new Exception("Cannot move the imported file to the music directory", e);
                }
                
                return null;
            }
        }).get();
    }
}
