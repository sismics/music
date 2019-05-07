package com.sismics.music.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.androidquery.AQuery;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.music.R;
import com.sismics.music.adapter.PlaylistListAdapter;
import com.sismics.music.event.MyMusicMenuVisibilityChangedEvent;
import com.sismics.music.event.OfflineModeChangedEvent;
import com.sismics.music.event.PlaylistOpenedEvent;
import com.sismics.music.event.TrackCacheStatusChangedEvent;
import com.sismics.music.model.Playlist;
import com.sismics.music.resource.PlaylistResource;
import com.sismics.music.util.CacheUtil;
import com.sismics.music.util.PreferenceUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Playlists list fragments.
 *
 * @author jtremeaux
 */
public class PlaylistListFragment extends Fragment {

    private AQuery aq;
    private AsyncTask cacheTask;
    private boolean offlineMode;
    private PlaylistListAdapter adapter;

    /**
     * Returns a new instance of this fragment.
     */
    public static PlaylistListFragment newInstance() {
        PlaylistListFragment fragment = new PlaylistListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.my_music, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                refreshPlaylistList();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the view
        View view = inflater.inflate(R.layout.fragment_playlist_list, container, false);
        offlineMode = PreferenceUtil.getBooleanPreference(getActivity(), PreferenceUtil.Pref.OFFLINE_MODE, false);
        aq = new AQuery(view);

        adapter = new PlaylistListAdapter(getActivity(), new ArrayList<>(), new ArrayList<>(), offlineMode);
        ListView listView = aq.id(R.id.listPlaylist).getListView();
        // TODO Add "empty cache" and "no results" empty views
        // listView.setEmptyView(view.findViewById(R.id.progress));
        listView.setAdapter(adapter);

        // Clear the search input
        aq.id(R.id.clearSearch).clicked(v -> aq.id(R.id.search).text(""));

        // Filter the playlists when the search input changes
        aq.id(R.id.search).getEditText()
                .addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (offlineMode) {
                            // Client-side filtering
                            adapter.getFilter().filter(s);
                        } else {
                            // Server-side filtering
                            refreshPlaylistList();
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void afterTextChanged(Editable s) {}
                });

        // Load more playlists when scrolling in online mode
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int savedLastVisibleIndex = -1;

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                final int lastVisibleItemIndex = firstVisibleItem + visibleItemCount;
                if (visibleItemCount > 0 && (lastVisibleItemIndex + 1) == totalItemCount) {
                    if (lastVisibleItemIndex != savedLastVisibleIndex) {
                        savedLastVisibleIndex = lastVisibleItemIndex;
                        loadOnlinePlaylists();
                    }
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}
        });

        // Open the playlist details on click
        aq.id(R.id.listPlaylist).itemClicked((parent, view1, position, id) -> {
            PlaylistListAdapter adapter = (PlaylistListAdapter) aq.id(R.id.listPlaylist).getListView().getAdapter();
            Playlist playlist = adapter.getItem(position);
            EventBus.getDefault().post(new PlaylistOpenedEvent(playlist));
        });

        refreshPlaylistList();
        EventBus.getDefault().register(this);
        return view;
    }

    /**
     * Refresh playlist list.
     */
    private void refreshPlaylistList() {
        // Grab the data from the cache asynchronously
        adapter.resetOnlinePlaylists();
        cacheTask = new AsyncTask<Void, Void, List<Playlist>>() {
            @Override
            protected List<Playlist> doInBackground(Void... params) {
                return CacheUtil.getCachedPlaylistList(getContext());
            }

            @Override
            protected void onPostExecute(List<Playlist> cachedPlaylistList) {
                adapter.setCachedPlaylists(cachedPlaylistList);
                adapter.getFilter().filter(aq.id(R.id.search).getText());

                // Download the playlist list from server
                loadOnlinePlaylists();
            }
        }.execute();
    }

    private void loadOnlinePlaylists() {
        if (offlineMode) {
            return;
        }

        PlaylistResource.list(getActivity(),
                adapter.getCount(),
                aq.id(R.id.search).getText().toString(), new JsonHttpResponseHandler() {
            public void onSuccess(final JSONObject json) {
                if (getActivity() == null || offlineMode) {
                    // The activity is dead, and this fragment has been detached
                    // or the user switched to offline mode during the request
                    return;
                }

                // Extract the playlists list
                JSONArray playlists = json.optJSONArray("items");
                List<Playlist> playlistList = new ArrayList<>();
                for (int i = 0; i < playlists.length(); i++) {
                    Playlist playlist = new Playlist(playlists.optJSONObject(i));
                    playlistList.add(playlist);
                }

                // Publish the new playlists to the adapter
                adapter.addOnlinePlaylists(playlistList);
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (cacheTask != null) {
            cacheTask.cancel(true);
        }
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    /**
     * My music menu visibility fragment has changed.
     * @param event Event
     */
    @Subscribe
    public void onEvent(MyMusicMenuVisibilityChangedEvent event) {
        setMenuVisibility(event.isMenuVisible());
    }

    /**
     * Offline mode has changed.
     * @param event Event
     */
    @Subscribe
    public void onEvent(OfflineModeChangedEvent event) {
        offlineMode = event.isOfflineMode();
        adapter.setOfflineMode(offlineMode);
        refreshPlaylistList();
    }

    /**
     * A track cache status has changed.
     * @param event Event
     */
    @Subscribe
    public void onEvent(TrackCacheStatusChangedEvent event) {
        adapter.notifyDataSetChanged();
    }
}