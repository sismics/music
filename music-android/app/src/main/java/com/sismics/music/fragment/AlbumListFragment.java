package com.sismics.music.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

        refreshAlbumList();

        // Clear the search input
        aq.id(R.id.clearSearch).clicked(v -> aq.id(R.id.search).text(""));

        // Filter the albums when the search input changes
        // TODO Filtering
        /*aq.id(R.id.search).getEditText()
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
                });*/

        // Open the album details on click
        aq.id(R.id.listAlbum).itemClicked((parent, view1, position, id) -> {
            AlbumAdapter adapter = (AlbumAdapter) aq.id(R.id.listAlbum).getListView().getAdapter();
            FullAlbum album = adapter.getItem(position);
            EventBus.getDefault().post(new AlbumOpenedEvent(album.getArtist(), album.getAlbum()));
        });

        EventBus.getDefault().register(this);
        return view;
    }

    /**
     * Refresh album list.
     */
    private void refreshAlbumList() {
        // Get cached albums
        final List<FullAlbum> cachedAlbumList = CacheUtil.getCachedAlbumList(getContext());

        // Grab the data from the cache first
        AlbumAdapter adapter = new AlbumAdapter(getActivity(), new ArrayList<>(), cachedAlbumList, offlineMode);
        aq.id(R.id.listAlbum).adapter(adapter);
        // adapter.getFilter().filter(aq.id(R.id.search).getText());

        if (!offlineMode) {
            // Download the album list from server
            AlbumResource.list(getActivity(), new JsonHttpResponseHandler() {
                public void onSuccess(final JSONObject json) {
                    if (getActivity() == null || offlineMode) {
                        // The activity is dead, and this fragment has been detached
                        return;
                    }

                    // Extract the albums list
                    ListView listView = aq.id(R.id.listAlbum).getListView();
                    JSONArray albums = json.optJSONArray("albums");
                    List<FullAlbum> albumList = new ArrayList<>();
                    for (int i = 0; i < albums.length(); i++) {
                        Album album = new Album(albums.optJSONObject(i));
                        Artist artist = new Artist(albums.optJSONObject(i).optJSONObject("artist"));
                        albumList.add(new FullAlbum(artist, album));
                    }

                    // Publish the new albums to the adapter
                    AlbumAdapter adapter = (AlbumAdapter) listView.getAdapter();
                    if (adapter != null) {
                        adapter.setOnlineAlbums(albumList);
                    } else {
                        adapter = new AlbumAdapter(getActivity(), albumList, cachedAlbumList, offlineMode);
                        listView.setAdapter(adapter);
                    }

                    // Apply the filter on the new result set
                    // adapter.getFilter().filter(aq.id(R.id.search).getText());
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
        ListView listView = aq.id(R.id.listAlbum).getListView();
        AlbumAdapter adapter = (AlbumAdapter) listView.getAdapter();
        if (adapter != null) {
            adapter.setOfflineMode(offlineMode);
        }
        refreshAlbumList();
        aq.id(R.id.search).text("");
    }

    /**
     * A track cache status has changed.
     * @param event Event
     */
    @Subscribe
    public void onEvent(TrackCacheStatusChangedEvent event) {
        ListView listView = aq.id(R.id.listAlbum).getListView();
        AlbumAdapter adapter = (AlbumAdapter) listView.getAdapter();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}