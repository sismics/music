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
import com.sismics.music.adapter.AlbumAdapter;
import com.sismics.music.event.AlbumOpenedEvent;
import com.sismics.music.event.MyMusicMenuVisibilityChangedEvent;
import com.sismics.music.event.OfflineModeChangedEvent;
import com.sismics.music.event.TrackCacheStatusChangedEvent;
import com.sismics.music.model.Album;
import com.sismics.music.model.Artist;
import com.sismics.music.model.FullAlbum;
import com.sismics.music.resource.AlbumResource;
import com.sismics.music.util.CacheUtil;
import com.sismics.music.util.PreferenceUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Albums list fragments.
 *
 * @author bgamard
 */
public class AlbumListFragment extends Fragment {

    private AQuery aq;
    private AsyncTask cacheTask;
    private boolean offlineMode;
    private AlbumAdapter adapter;

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
                refreshAlbumList();
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

        adapter = new AlbumAdapter(getActivity(), new ArrayList<>(), new ArrayList<>(), offlineMode);
        ListView listView = aq.id(R.id.listAlbum).getListView();
        // TODO Add "empty cache" and "no results" empty views
        // listView.setEmptyView(view.findViewById(R.id.progress));
        listView.setAdapter(adapter);

        // Clear the search input
        aq.id(R.id.clearSearch).clicked(v -> aq.id(R.id.search).text(""));

        // Filter the albums when the search input changes
        aq.id(R.id.search).getEditText()
                .addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (offlineMode) {
                            // Client-side filtering
                            adapter.getFilter().filter(s);
                        } else {
                            // Server-side filtering
                            refreshAlbumList();
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void afterTextChanged(Editable s) {}
                });

        // Load more albums when scrolling in online mode
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int savedLastVisibleIndex = -1;

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                final int lastVisibleItemIndex = firstVisibleItem + visibleItemCount;
                if (visibleItemCount > 0 && (lastVisibleItemIndex + 1) == totalItemCount) {
                    if (lastVisibleItemIndex != savedLastVisibleIndex) {
                        savedLastVisibleIndex = lastVisibleItemIndex;
                        loadOnlineAlbums();
                    }
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}
        });

        // Open the album details on click
        aq.id(R.id.listAlbum).itemClicked((parent, view1, position, id) -> {
            AlbumAdapter adapter = (AlbumAdapter) aq.id(R.id.listAlbum).getListView().getAdapter();
            FullAlbum album = adapter.getItem(position);
            EventBus.getDefault().post(new AlbumOpenedEvent(album.getArtist(), album.getAlbum()));
        });

        refreshAlbumList();
        EventBus.getDefault().register(this);
        return view;
    }

    /**
     * Refresh album list.
     */
    private void refreshAlbumList() {
        // Grab the data from the cache asynchronously
        adapter.resetOnlineAlbums();
        cacheTask = new AsyncTask<Void, Void, List<FullAlbum>>() {
            @Override
            protected List<FullAlbum> doInBackground(Void... params) {
                return CacheUtil.getCachedAlbumList(getContext());
            }

            @Override
            protected void onPostExecute(List<FullAlbum> cachedAlbumList) {
                adapter.setCachedAlbums(cachedAlbumList);
                adapter.getFilter().filter(aq.id(R.id.search).getText());

                // Download the album list from server
                loadOnlineAlbums();
            }
        }.execute();
    }

    private void loadOnlineAlbums() {
        if (offlineMode) {
            return;
        }

        AlbumResource.list(getActivity(),
                adapter.getCount(),
                aq.id(R.id.search).getText().toString(), new JsonHttpResponseHandler() {
            public void onSuccess(final JSONObject json) {
                if (getActivity() == null || offlineMode) {
                    // The activity is dead, and this fragment has been detached
                    // or the user switched to offline mode during the request
                    return;
                }

                // Extract the albums list
                JSONArray albums = json.optJSONArray("albums");
                List<FullAlbum> albumList = new ArrayList<>();
                for (int i = 0; i < albums.length(); i++) {
                    Album album = new Album(albums.optJSONObject(i));
                    Artist artist = new Artist(albums.optJSONObject(i).optJSONObject("artist"));
                    albumList.add(new FullAlbum(artist, album));
                }

                // Publish the new albums to the adapter
                adapter.addOnlineAlbums(albumList);
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
        refreshAlbumList();
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