package com.sismics.music.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.sismics.music.model.Playlist;

import java.io.File;

/**
 * @author bgamard.
 */
public class CacheUtil {

    public static File getMusicCacheDir(Context context) {
        File music = Environment.getExternalStoragePublicDirectory("SismicsMusic");
        File cache = new File(music, "cache");
        if (!cache.exists()) {
            cache.mkdirs();
        }
        return cache;
    }


    public static boolean isComplete(Context context, Playlist.Track track) {
        return getCompleteCacheFile(context, track).exists();
    }

    public static File getCompleteCacheFile(Context context, Playlist.Track track) {
        File cacheDir = getMusicCacheDir(context);
        return new File(cacheDir, track.getId() + ".complete");
    }

    public static File getIncompleteCacheFile(Context context,Playlist.Track track) {
        File cacheDir = getMusicCacheDir(context);
        return new File(cacheDir, track.getId());
    }

    public static boolean setComplete(File file) {
        return file.renameTo(new File(file.getAbsolutePath() + ".complete"));
    }
}
