package com.sismics.music.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.androidquery.AQuery;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.music.R;
import com.sismics.music.adapter.AlbumAdapter;
import com.sismics.music.event.AlbumOpenedEvent;
import com.sismics.music.event.MyMusicMenuVisibilityChangedEvent;
import com.sismics.music.event.OfflineModeChangedEvent;
import com.sismics.music.event.TrackCacheStatusChangedEvent;
import com.sismics.music.model.Album;
import com.sismics.music.resource.AlbumResource;
import com.sismics.music.util.CacheUtil;
import com.sismics.music.util.PreferenceUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Set;

import de.greenrobot.event.EventBus;

/**
 * Albums list fragments.
 *
 * @author bgamard
 */
public class AlbumListFragment extends Fragment {

    private AQuery aq;

    private boolean offlineMode;

    /**
     * Returns a new instance of this fragment.
     */
    public static AlbumListFragment newInstance() {
        AlbumListFragment fragment = new AlbumListFragment();
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
                refreshAlbumList(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the view
        View view = inflater.inflate(R.layout.fragment_album_list, container, false);
        offlineMode = PreferenceUtil.getBooleanPreference(getActivity(), PreferenceUtil.Pref.OFFLINE_MODE, false);
        aq = new AQuery(view);

        refreshAlbumList(false);

        // Clear the search input
        aq.id(R.id.clearSearch).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aq.id(R.id.search).text("");
            }
        });

        // Filter the albums when the search input changes
        aq.id(R.id.search).getEditText()
                .addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        AlbumAdapter adapter = (AlbumAdapter) aq.id(R.id.listAlbum).getListView().getAdapter();
                        if (adapter != null) {
                            adapter.getFilter().filter(s);
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void afterTextChanged(Editable s) {}
                });

        // Open the album details on click
        aq.id(R.id.listAlbum).itemClicked(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlbumAdapter adapter = (AlbumAdapter) aq.id(R.id.listAlbum).getListView().getAdapter();
                EventBus.getDefault().post(new AlbumOpenedEvent(new Album(adapter.getItem(position))));
            }
        });

        EventBus.getDefault().register(this);
        return view;
    }

    /**
     * Refresh album list.
     * @param forceRefresh Force a refresh from the server even if there is cached data
     */
    private void refreshAlbumList(boolean forceRefresh) {
        // Get cached albums
        final Set<String> cachedAlbumSet = CacheUtil.getCachedAlbumSet();

        // Grab the data from the cache first
        JSONObject cache = PreferenceUtil.getCachedJson(getActivity(), PreferenceUtil.Pref.CACHED_ALBUMS_LIST_JSON);
        if (cache != null) {
            AlbumAdapter adapter = new AlbumAdapter(getActivity(), cache.optJSONArray("albums"), cachedAlbumSet, offlineMode);
            aq.id(R.id.listAlbum).adapter(adapter);
            adapter.getFilter().filter(aq.id(R.id.search).getText());
        }

        if (cache == null || forceRefresh) {
            // Download the album list from server
            AlbumResource.list(getActivity(), new JsonHttpResponseHandler() {
                public void onSuccess(final JSONObject json) {
                    if (getActivity() == null) {
                        // The activity is dead, and this fragment has been detached
                        return;
                    }

                    // Cache the albums list
                    ListView listView = aq.id(R.id.listAlbum).getListView();
                    JSONArray albums = json.optJSONArray("albums");
                    PreferenceUtil.setCachedJson(getActivity(), PreferenceUtil.Pref.CACHED_ALBUMS_LIST_JSON, json);

                    // Publish the new albums to the adapter
                    AlbumAdapter adapter = (AlbumAdapter) listView.getAdapter();
                    if (adapter != null) {
                        adapter.setAlbums(albums);
                    } else {
                        adapter = new AlbumAdapter(getActivity(), albums, cachedAlbumSet, offlineMode);
                        listView.setAdapter(adapter);
                    }

                    // Apply the filter on the new result set
                    adapter.getFilter().filter(aq.id(R.id.search).getText());
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    /**
     * My music menu visibility fragment has changed.
     * @param event Event
     */
    public void onEvent(MyMusicMenuVisibilityChangedEvent event) {
        setMenuVisibility(event.isMenuVisible());
    }

    /**
     * Offline mode has changed.
     * @param event Event
     */
    public void onEvent(OfflineModeChangedEvent event) {
        offlineMode = event.isOfflineMode();
        AlbumAdapter adapter = (AlbumAdapter) aq.id(R.id.listAlbum).getListView().getAdapter();
        if (adapter != null) {
            adapter.setOfflineMode(offlineMode);
        }
        aq.id(R.id.search).text("");
    }

    /**
     * A track cache status has changed.
     * @param event Event
     */
    public void onEvent(TrackCacheStatusChangedEvent event) {
        refreshAlbumList(false);
    }
}