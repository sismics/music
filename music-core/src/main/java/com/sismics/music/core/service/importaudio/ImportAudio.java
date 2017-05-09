package com.sismics.music.core.service.importaudio;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.UUID;

/**
 * An audio import in progress.
 * 
 * @author bgamard
 */
public class ImportAudio {
    
    public static enum Status {
        PENDING,
        INPROGRESS,
        DONE,
        ERROR
    }
    
    /**
     * ID.
     */
    private String id;
    
    /**
     * URL.
     */
    private String url;
    
    /**
     * Working files.
     */
    private List<String> workingFiles = Lists.newArrayList();
    
    /**
     * Quality.
     */
    private String quality;
    
    /**
     * Format.
     */
    private String format;
    
    /**
     * Percentage progress.
     */
    private float progress = 0;
    
    /**
     * Total import size.
     */
    private String totalSize = "";
    
    /**
     * Download speed.
     */
    private String downloadSpeed = "";

    /**
     * Import status.
     */
    private Status status = Status.PENDING;
    
    /**
     * Download message.
     */
    private String message = "";
    
    /**
     * Import process.
     */
    private Process process;
    
    /**
     * Build a new ImportAudio by copy.
     * 
     * @param other ImportAudio to copy
     */
    public ImportAudio(ImportAudio other) {
        this.id = other.id;
        this.url = other.url;
        this.progress = other.progress;
        this.totalSize = other.totalSize;
        this.downloadSpeed = other.downloadSpeed;
        this.status = other.status;
        this.message = other.message;
        this.quality = other.quality;
        this.format = other.format;
        this.workingFiles.addAll(other.getWorkingFiles());
        this.process = other.process;
    }
    
    /**
     * An audio import in progress.
     */
    public ImportAudio(String url, String quality, String format) {
        this.id = UUID.randomUUID().toString();
        this.url = url;
        this.quality = quality;
        this.format = format;
    }

    /**
     * Getter of url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Getter of quality.
     *
     * @return the quality
     */
    public String getQuality() {
        return quality;
    }

    /**
     * Getter of format.
     *
     * @return the format
     */
    public String getFormat() {
        return format;
    }

    /**
     * Getter of progress.
     *
     * @return the progress
     */
    public float getProgress() {
        return progress;
    }

    /**
     * Setter of progress.
     *
     * @param progress progress
     */
    public void setProgress(float progress) {
        this.progress = progress;
    }

    /**
     * Getter of totalSize.
     *
     * @return the totalSize
     */
    public String getTotalSize() {
        return totalSize;
    }

    /**
     * Setter of totalSize.
     *
     * @param totalSize totalSize
     */
    public void setTotalSize(String totalSize) {
        this.totalSize = totalSize;
    }

    /**
     * Getter of downloadSpeed.
     *
     * @return the downloadSpeed
     */
    public String getDownloadSpeed() {
        return downloadSpeed;
    }

    /**
     * Setter of downloadSpeed.
     *
     * @param downloadSpeed downloadSpeed
     */
    public void setDownloadSpeed(String downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    /**
     * Getter of message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Setter of message.
     *
     * @param message message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Getter of status.
     *
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Setter of status.
     *
     * @param status status
     */
    public void setStatus(Status status) {
        this.status = status;
    }
    
    /**
     * Getter of workingFiles.
     *
     * @return the workingFiles
     */
    public List<String> getWorkingFiles() {
        return workingFiles;
    }

    /**
     * Add a working file.
     *
     * @param workingFile workingFiles
     */
    public void addWorkingFiles(String workingFile) {
        this.workingFiles.add(workingFile);
    }

    /**
     * Getter of id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }
    
    /**
     * Getter of process.
     *
     * @return the process
     */
    public Process getProcess() {
        return process;
    }

    /**
     * Setter of process.
     *
     * @param process process
     */
    public void setProcess(Process process) {
        this.process = process;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("url", url)
                .add("status", status)
                .toString();
    }
}
