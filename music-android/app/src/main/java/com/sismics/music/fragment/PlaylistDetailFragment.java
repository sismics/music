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
import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.music.R;
import com.sismics.music.adapter.PlaylistDetailAdapter;
import com.sismics.music.event.OfflineModeChangedEvent;
import com.sismics.music.event.TrackCacheStatusChangedEvent;
import com.sismics.music.event.TrackLikedChangedEvent;
import com.sismics.music.model.Album;
import com.sismics.music.model.ApplicationContext;
import com.sismics.music.model.Artist;
import com.sismics.music.model.Playlist;
import com.sismics.music.model.PlaylistTrack;
import com.sismics.music.model.Track;
import com.sismics.music.resource.PlaylistResource;
import com.sismics.music.service.MusicService;
import com.sismics.music.util.PreferenceUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Playlist details fragment.
 *
 * @author jtremeaux
 */
public class PlaylistDetailFragment extends Fragment {

    private static final String ARG_PLAYLIST = "playlist";
    private AsyncTask cacheTask;
    private EventBus eventBus;
    private PlaylistDetailAdapter playlistAdapter;
    private AQuery aq;

    /**
     * Returns a new instance of this fragment.
     */
    public static PlaylistDetailFragment newInstance(Playlist playlist) {
        PlaylistDetailFragment fragment = new PlaylistDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PLAYLIST, playlist);
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
        final Playlist playlist = (Playlist) getArguments().getSerializable(ARG_PLAYLIST);

        // Inflate the view
        View view = inflater.inflate(R.layout.fragment_playlist_detail, container, false);
        aq = new AQuery(view);

        // Populate the view with the given data
        aq.id(R.id.playlistName).text(playlist.getName());
//        String coverUrl = PreferenceUtil.getServerUrl(getActivity()) + "/api/playlist/" + playlist.getId() + "/playlistart/small";
//        aq.id(R.id.imgCover).image(new BitmapAjaxCallback()
//                .url(coverUrl)
//                .animation(AQuery.FADE_IN_NETWORK)
//                .cookie("auth_token", PreferenceUtil.getAuthToken(getActivity()))
//        );

        // Set a new adapter to the tracks list, and attach the header to the ListView
        ListView listTracks =  aq.id(R.id.listTracks).getListView();
        View header = aq.id(R.id.header).getView();
        ((ViewGroup) header.getParent()).removeView(header);
        header.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        listTracks.addHeaderView(header, null, false);
        playlistAdapter = new PlaylistDetailAdapter(getActivity(), playlist, new ArrayList<>());
        listTracks.setAdapter(playlistAdapter);

        // Add to queue on click
        listTracks.setOnItemClickListener((parent, view1, position, id) -> {
            PlaylistTrack playlistTrack = playlistAdapter.getItem(position - 1);
            ApplicationContext.getInstance().getPlaylistService().add(getContext(), playlistTrack.getArtist(), playlistTrack.getAlbum(), playlistTrack.getTrack());
            Toast.makeText(getActivity(), R.string.add_toast, Toast.LENGTH_SHORT).show();
        });

        // Play all
        aq.id(R.id.btnPlayAll).clicked(v -> {
            List<PlaylistTrack> trackList = playlistAdapter.getPlaylistTracks();
            ApplicationContext.getInstance().getPlaylistService().clear(false);
            for (PlaylistTrack playlistTrack : trackList) {
                ApplicationContext.getInstance().getPlaylistService().add(getContext(), playlistTrack.getArtist(), playlistTrack.getAlbum(), playlistTrack.getTrack());
            }
            Intent intent = new Intent(MusicService.ACTION_PLAY, null, getActivity(), MusicService.class);
            intent.putExtra(MusicService.EXTRA_FORCE, true);
            getActivity().startService(intent);
            Toast.makeText(getActivity(), R.string.play_all_toast, Toast.LENGTH_SHORT).show();
        });

        // Add all
        aq.id(R.id.btnAddAll).clicked(v -> {
            List<PlaylistTrack> trackList = playlistAdapter.getPlaylistTracks();
            for (PlaylistTrack playlistTrack : trackList) {
                ApplicationContext.getInstance().getPlaylistService().add(getContext(), playlistTrack.getArtist(), playlistTrack.getAlbum(), playlistTrack.getTrack());
            }
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
        view.findViewById(R.id.notCachedView).setVisibility(View.GONE);
        boolean offlineMode = PreferenceUtil.getBooleanPreference(getActivity(), PreferenceUtil.Pref.OFFLINE_MODE, false);
        final Playlist playlist = (Playlist) getArguments().getSerializable(ARG_PLAYLIST);

        // Grab cached tracks for this playlist
        cacheTask = new AsyncTask<Object, Void, List<PlaylistTrack>>() {
            @Override
            protected List<PlaylistTrack> doInBackground(Object... params) {
                return new ArrayList<>();
//                return CacheUtil.getCachedTrackList(getContext(), (Artist) params[0], (Playlist) params[1]);
            }

            @Override
            protected void onPostExecute(List<PlaylistTrack> cachedTracks) {
                if (offlineMode) {
                    listTracks.setEmptyView(view.findViewById(R.id.notCachedView));
                    view.findViewById(R.id.progress).setVisibility(View.GONE);
                    playlistAdapter.setPlaylistTracks(cachedTracks);
                } else {
                    // We are in online mode, download the playlist details from the server
                    PlaylistResource.info(getActivity(), playlist.getId(), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(final JSONObject json) {
                            if (getActivity() == null) {
                                // The activity is dead, and this fragment has been detached
                                return;
                            }

                            // Assemble tracks
                            List<PlaylistTrack> playlistTracks = new ArrayList<>();
                            JSONArray tracksJson =  json.optJSONArray("tracks");
                            for (int i = 0; i < tracksJson.length(); i++) {
                                JSONObject trackJson = tracksJson.optJSONObject(i);
                                JSONObject albumJson = trackJson.optJSONObject("album");
                                JSONObject artistJson = trackJson.optJSONObject("artist");
                                Track track = new Track(trackJson);
                                Album album = new Album(albumJson.optString("id"), albumJson.optString("name"), artistJson.optString("id"));
                                Artist artist = new Artist(artistJson);
                                PlaylistTrack playlistTrack = new PlaylistTrack(getContext(), artist, album, track);
                                playlistTracks.add(playlistTrack);
                            }

                            // Populate the adapter
                            playlistAdapter.setPlaylistTracks(playlistTracks);
                        }
                    });
                }
            }
        }.execute(playlist);
    }

    /**
     * A track cache status has changed.
     * @param event Event
     */
    @Subscribe
    public void onEvent(TrackCacheStatusChangedEvent event) {
        playlistAdapter.notifyDataSetChanged();
    }

    /**
     * A track liked status has changed.
     * @param event Event
     */
    @Subscribe
    public void onEvent(TrackLikedChangedEvent event) {
        playlistAdapter.notifyDataSetChanged();
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