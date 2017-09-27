package com.sismics.music.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.music.R;
import com.sismics.music.adapter.TracksAdapter;
import com.sismics.music.event.OfflineModeChangedEvent;
import com.sismics.music.event.TrackCacheStatusChangedEvent;
import com.sismics.music.model.Album;
import com.sismics.music.model.Artist;
import com.sismics.music.model.Track;
import com.sismics.music.resource.AlbumResource;
import com.sismics.music.service.MusicService;
import com.sismics.music.service.PlaylistService;
import com.sismics.music.util.CacheUtil;
import com.sismics.music.util.PreferenceUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Album details fragment.
 *
 * @author bgamard
 */
public class AlbumFragment extends Fragment {

    private static final String ARG_ARTIST = "artist";
    private static final String ARG_ALBUM = "album";
    private AsyncTask cacheTask;
    private EventBus eventBus;
    private TracksAdapter tracksAdapter;
    private AQuery aq;

    /**
     * Returns a new instance of this fragment.
     */
    public static AlbumFragment newInstance(Artist artist, Album album) {
        AlbumFragment fragment = new AlbumFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ARTIST, artist);
        args.putSerializable(ARG_ALBUM, album);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        eventBus = EventBus.getDefault();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public void onDetach() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        activity.getSupportActionBar().setHomeButtonEnabled(false);
        super.onDetach();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().getSupportFragmentManager().popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Artist artist = (Artist) getArguments().getSerializable(ARG_ARTIST);
        final Album album = (Album) getArguments().getSerializable(ARG_ALBUM);

        // Inflate the view
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        aq = new AQuery(view);

        // Populate the view with the given data
        aq.id(R.id.albumName).text(album.getName());
        aq.id(R.id.artistName).text(artist.getName());
        String coverUrl = PreferenceUtil.getServerUrl(getActivity()) + "/api/album/" + album.getId() + "/albumart/small";
        aq.id(R.id.imgCover).image(new BitmapAjaxCallback()
                .url(coverUrl)
                .animation(AQuery.FADE_IN_NETWORK)
                .cookie("auth_token", PreferenceUtil.getAuthToken(getActivity()))
        );

        // Set a new adapter to the tracks list, and attach the header to the ListView
        ListView listTracks =  aq.id(R.id.listTracks).getListView();
        View header = aq.id(R.id.header).getView();
        ((ViewGroup) header.getParent()).removeView(header);
        header.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        listTracks.addHeaderView(header, null, false);
        tracksAdapter = new TracksAdapter(getActivity(), artist, album, new ArrayList<>());
        listTracks.setAdapter(tracksAdapter);

        // Add to queue on click
        listTracks.setOnItemClickListener((parent, view1, position, id) -> {
            PlaylistService.add(getContext(), artist, album, tracksAdapter.getItem(position - 1));
            Toast.makeText(getActivity(), R.string.add_toast, Toast.LENGTH_SHORT).show();
        });

        // Play all
        aq.id(R.id.btnPlayAll).clicked(v -> {
            List<Track> trackList = tracksAdapter.getTracks();
            PlaylistService.clear(false);
            PlaylistService.addAll(getContext(), artist, album, trackList);
            Intent intent = new Intent(MusicService.ACTION_PLAY, null, getActivity(), MusicService.class);
            intent.putExtra(MusicService.EXTRA_FORCE, true);
            getActivity().startService(intent);
            Toast.makeText(getActivity(), R.string.play_all_toast, Toast.LENGTH_SHORT).show();
        });

        // Add all
        aq.id(R.id.btnAddAll).clicked(v -> {
            List<Track> trackList = tracksAdapter.getTracks();
            PlaylistService.addAll(getContext(), artist, album, trackList);
            Toast.makeText(getActivity(), R.string.add_all_toast, Toast.LENGTH_SHORT).show();
        });

        loadTracks(view);
        eventBus.register(this);
        return view;
    }

    /**
     * Load tracks from local device and/or server, depending on offline mode.
     */
    private void loadTracks(View view) {
        ListView listTracks =  aq.id(R.id.listTracks).getListView();
        listTracks.setEmptyView(view.findViewById(R.id.progress));
        boolean offlineMode = PreferenceUtil.getBooleanPreference(getActivity(), PreferenceUtil.Pref.OFFLINE_MODE, false);
        final Artist artist = (Artist) getArguments().getSerializable(ARG_ARTIST);
        final Album album = (Album) getArguments().getSerializable(ARG_ALBUM);

        // Grab cached tracks for this album
        cacheTask = new AsyncTask<Object, Void, List<Track>>() {
            @Override
            protected List<Track> doInBackground(Object... params) {
                return CacheUtil.getCachedTrack(getContext(), (Artist) params[0], (Album) params[1]);
            }

            @Override
            protected void onPostExecute(List<Track> tracks) {
                listTracks.setEmptyView(view.findViewById(R.id.notCachedView));
                tracksAdapter.setTracks(tracks);
            }
        }.execute(artist, album);

        if (!offlineMode) {
            // We are in online mode, download the album details from the server
            AlbumResource.info(getActivity(), album.getId(), new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(final JSONObject json) {
                    if (getActivity() == null) {
                        // The activity is dead, and this fragment has been detached
                        return;
                    }

                    // Cancel the cache request, should not happen
                    cacheTask.cancel(true);

                    // Assemble tracks
                    List<Track> tracks = new ArrayList<>();
                    JSONArray tracksJson =  json.optJSONArray("tracks");
                    for (int i = 0; i < tracksJson.length(); i++) {
                        tracks.add(new Track(tracksJson.optJSONObject(i)));
                    }

                    // Populate the adapter
                    tracksAdapter.setTracks(tracks);
                }
            });
        }
    };

    /**
     * A track cache status has changed.
     * @param event Event
     */
    @Subscribe
    public void onEvent(TrackCacheStatusChangedEvent event) {
        tracksAdapter.notifyDataSetChanged();
    }

    /**
     * Offline mode has changed.
     * @param event Event
     */
    @Subscribe
    public void onEvent(OfflineModeChangedEvent event) {
        loadTracks(getView());
    }

    @Override
    public void onDestroyView() {
        if (cacheTask != null) {
            cacheTask.cancel(true);
        }
        eventBus.unregister(this);
        super.onDestroyView();
    }
}