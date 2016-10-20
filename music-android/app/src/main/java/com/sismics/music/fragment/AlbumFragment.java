package com.sismics.music.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
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
import com.sismics.music.model.Track;
import com.sismics.music.resource.AlbumResource;
import com.sismics.music.service.MusicService;
import com.sismics.music.service.PlaylistService;
import com.sismics.music.util.CacheUtil;
import com.sismics.music.util.PreferenceUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Album details fragment.
 *
 * @author bgamard
 */
public class AlbumFragment extends Fragment {

    private static final String ARG_ALBUM = "album";
    private AsyncTask cacheTask;
    private EventBus eventBus;
    private TracksAdapter tracksAdapter;

    /**
     * Returns a new instance of this fragment.
     */
    public static AlbumFragment newInstance(Album album) {
        AlbumFragment fragment = new AlbumFragment();
        Bundle args = new Bundle();
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.getActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public void onDetach() {
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
        getActivity().getActionBar().setHomeButtonEnabled(false);
        super.onDetach();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().getFragmentManager().popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Album album = (Album) getArguments().getSerializable(ARG_ALBUM);

        // Inflate the view
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        final AQuery aq = new AQuery(view);

        // Populate the view with the given data
        aq.id(R.id.albumName).text(album.getName());
        aq.id(R.id.artistName).text(album.getArtistName());
        String coverUrl = PreferenceUtil.getServerUrl(getActivity()) + "/api/album/" + album.getId() + "/albumart/small";
        aq.id(R.id.imgCover).image(new BitmapAjaxCallback()
                .url(coverUrl)
                .animation(AQuery.FADE_IN_NETWORK)
                .cookie("auth_token", PreferenceUtil.getAuthToken(getActivity()))
        );

        // Set a new adapter to the tracks list, and attach the header to the ListView
        ListView listTracks =  aq.id(R.id.listTracks).getListView();
        listTracks.setEmptyView(view.findViewById(R.id.progress));
        View header = aq.id(R.id.header).getView();
        ((ViewGroup) header.getParent()).removeView(header);
        header.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        listTracks.addHeaderView(header, null, false);
        tracksAdapter = new TracksAdapter(getActivity(), album, new ArrayList<Track>());
        listTracks.setAdapter(tracksAdapter);

        // Add to queue on click
        listTracks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlaylistService.INSTANCE.add(album, tracksAdapter.getItem(position - 1));
                Toast.makeText(getActivity(), R.string.add_toast, Toast.LENGTH_SHORT).show();
            }
        });

        // Play all
        aq.id(R.id.btnPlayAll).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Track> trackList = tracksAdapter.getTracks();
                PlaylistService.INSTANCE.clear(false);
                PlaylistService.INSTANCE.addAll(album, trackList);
                Intent intent = new Intent(MusicService.ACTION_PLAY, null, getActivity(), MusicService.class);
                intent.putExtra(MusicService.EXTRA_FORCE, true);
                getActivity().startService(intent);
                Toast.makeText(getActivity(), R.string.play_all_toast, Toast.LENGTH_SHORT).show();
            }
        });

        // Add all
        aq.id(R.id.btnAddAll).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Track> trackList = tracksAdapter.getTracks();
                PlaylistService.INSTANCE.addAll(album, trackList);
                Toast.makeText(getActivity(), R.string.add_all_toast, Toast.LENGTH_SHORT).show();
            }
        });

        loadTracks();
        eventBus.register(this);
        return view;
    }

    /**
     * Load tracks from local device and/or server, depending on offline mode.
     */
    private void loadTracks() {
        boolean offlineMode = PreferenceUtil.getBooleanPreference(getActivity(), PreferenceUtil.Pref.OFFLINE_MODE, false);
        final Album album = (Album) getArguments().getSerializable(ARG_ALBUM);

        // Grab cached tracks for this album
        cacheTask = new AsyncTask<Album, Void, List<Track>>() {
            @Override
            protected List<Track> doInBackground(Album... params) {
                return CacheUtil.INSTANCE.getCachedTrack(params[0]);
            }

            @Override
            protected void onPostExecute(List<Track> tracks) {
                tracksAdapter.setTracks(tracks);
            }
        }.execute(album);

        if (!offlineMode) {
            // We are in online mode, download the album details from the server
            AlbumResource.Companion.info(getActivity(), album.getId(), new JsonHttpResponseHandler() {
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
    public void onEvent(TrackCacheStatusChangedEvent event) {
        tracksAdapter.notifyDataSetChanged();
    }

    /**
     * Offline mode has changed.
     * @param event Event
     */
    public void onEvent(OfflineModeChangedEvent event) {
        loadTracks();
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