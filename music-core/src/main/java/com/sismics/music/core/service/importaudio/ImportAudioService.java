package com.sismics.music.core.service.importaudio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.sismics.music.core.constant.Constants;
import com.sismics.music.core.util.DirectoryUtil;

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
            String command = "youtube-dl -v --prefer-ffmpeg --newline -f bestaudio -x --audio-format " + importAudio.getFormat()
                    + " --audio-quality " + importAudio.getQuality() + " -o";
            List<String> commandList = new LinkedList<String>(Arrays.asList(StringUtils.split(command)));
            commandList.add(output);
            commandList.add(importAudio.getUrl());
            Process process = new ProcessBuilder(commandList)
                    .redirectErrorStream(true)
                    .start();

            // Import status is now in progress
            importAudio.setStatus(ImportAudio.Status.INPROGRESS);
            importAudioList.add(importAudio);
            
            // Reading standard output to update import status
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    Matcher matcher = PROGRESS_PATTERN.matcher(line);
                    if (matcher.find()) {
                        importAudio.setProgress(Float.valueOf(matcher.group(1)));
                        importAudio.setTotalSize(matcher.group(2));
                        importAudio.setDownloadSpeed(matcher.group(3));
                    }
                    
                    if (line.contains("ERROR")) {
                        importAudio.setStatus(ImportAudio.Status.ERROR);
                    }
                    
                    importAudio.setMessage(importAudio.getMessage() + "\n" + line);
                }
            }
            
            // Import is done
            if (importAudio.getStatus() != ImportAudio.Status.ERROR) {
                importAudio.setStatus(ImportAudio.Status.DONE);
            }
            
            process.destroy();
        }
    }
    
    @Override
    protected void shutDown() {
    }

    /**
     * Check import audio prerequisites.
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
     * Add URL to import.
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
     * Return a copied version of the currently importing audio.
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
     * Return imported files.
     * 
     * @return List of imported files
     */
    public List<File> getImportedFileList() {
        return Arrays.asList(DirectoryUtil.getImportAudioDirectory().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String fileName) {
                String extension = Files.getFileExtension(fileName);
                return Constants.SUPPORTED_AUDIO_EXTENSIONS.contains(extension);
            }
        }));
    }

    /**
     * Cleanup finished imports.
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
}
