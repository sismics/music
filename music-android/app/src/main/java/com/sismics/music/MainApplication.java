package com.sismics.music;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.androidquery.callback.BitmapAjaxCallback;
import com.sismics.music.db.dao.AlbumDao;
import com.sismics.music.db.dao.ArtistDao;
import com.sismics.music.db.dao.PlaylistDao;
import com.sismics.music.db.dao.TrackDao;
import com.sismics.music.model.Album;
import com.sismics.music.model.ApplicationContext;
import com.sismics.music.model.Artist;
import com.sismics.music.model.PlaylistTrack;
import com.sismics.music.model.Track;
import com.sismics.music.util.PreferenceUtil;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Main application.
 * 
 * @author bgamard
 */
@ReportsCrashes(
        formUri = "http://acraviz.sismics.com/api",
        formUriBasicAuthLogin = BuildConfig.APPLICATION_ID,
        formUriBasicAuthPassword = "TsEThfGJ6OvhfAN3xilxLbGU"
)
public class MainApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ACRA.init(this);
    }

    @Override
    public void onCreate() {
        if (ACRA.isACRASenderServiceProcess()) {
            return;
        }

        // Fetching /user from cache
        JSONObject json = PreferenceUtil.getCachedJson(getApplicationContext(), PreferenceUtil.Pref.CACHED_USER_INFO_JSON);
        ApplicationContext.getInstance().setUserInfo(getApplicationContext(), json);

        // Load saved playlist
        try {
            List<PlaylistTrack> playlistTrackList = new ArrayList<>();
            for (PlaylistDao.PlaylistItem playlistItem : PlaylistDao.getPlaylist(this)) {
                try {
                    Track track = TrackDao.getTrackById(this, playlistItem.trackId);
                    Album album = AlbumDao.getAlbumById(this, playlistItem.albumId);
                    Artist artist = ArtistDao.getArtistById(this, playlistItem.artistId);
                    playlistTrackList.add(new PlaylistTrack(this, artist, album, track));
                } catch (Exception e) {
                    Log.e("MainApplication", "Error loading track: " + playlistItem.trackId, e);
                }
            }
            ApplicationContext.getInstance().getPlaylistService().addAll(playlistTrackList);
        } catch (Exception e) {
            Log.e("MainApplication", "Error restoring playlist", e);
        }

        super.onCreate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        BitmapAjaxCallback.clearCache();
    }
}
