package com.sismics.music.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.androidquery.AQuery;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.music.R;
import com.sismics.music.event.OpenAlbumEvent;
import com.sismics.music.model.Album;
import com.sismics.music.resource.AlbumResource;
import com.sismics.music.ui.adapter.AlbumAdapter;
import com.sismics.music.util.PreferenceUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import de.greenrobot.event.EventBus;

/**
 * Albums list fragments.
 *
 * @author bgamard
 */
public class AlbumListFragment extends Fragment {

    private EventBus eventBus;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the view
        View view = inflater.inflate(R.layout.fragment_album_list, container, false);
        eventBus = EventBus.getDefault();
        final AQuery aq = new AQuery(view);

        // Grab the data from the cache first
        JSONObject cache = PreferenceUtil.getCachedJson(getActivity(), PreferenceUtil.Pref.CACHED_ALBUMS_LIST_JSON);
        if (cache != null) {
            aq.id(R.id.listAlbum).adapter(new AlbumAdapter(getActivity(), cache.optJSONArray("albums")));
        }

        // Download the album list from server
        AlbumResource.list(getActivity(), new JsonHttpResponseHandler() {
            public void onSuccess(final JSONObject json) {
                if (getActivity() == null) {
                    // The activity is dead, and this fragment has been detached
                    return;
                }

                ListView listView = aq.id(R.id.listAlbum).getListView();
                JSONArray albums = json.optJSONArray("albums");
                PreferenceUtil.setCachedJson(getActivity(), PreferenceUtil.Pref.CACHED_ALBUMS_LIST_JSON, json);

                AlbumAdapter adapter = (AlbumAdapter) listView.getAdapter();
                if (adapter != null) {
                    adapter.setAlbums(albums);
                } else {
                    listView.setAdapter(new AlbumAdapter(getActivity(), albums));
                }
            }
        });

        // Open the album details on click
        aq.id(R.id.listAlbum).itemClicked(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlbumAdapter adapter = (AlbumAdapter) aq.id(R.id.listAlbum).getListView().getAdapter();
                eventBus.post(new OpenAlbumEvent(new Album(adapter.getItem(position))));
            }
        });

        return view;
    }
}