package com.sismics.music.fragment;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.androidquery.AQuery;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.music.R;
import com.sismics.music.model.Album;
import com.sismics.music.model.Track;
import com.sismics.music.resource.AlbumResource;
import com.sismics.music.ui.adapter.AlbumAdapter;
import com.sismics.music.ui.adapter.TracksAdapter;
import com.sismics.music.util.CacheUtil;
import com.sismics.music.util.PreferenceUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Album details fragment.
 *
 * @author bgamard
 */
public class AlbumFragment extends Fragment {
    /**
     * Album JSON argument.
     */
    private static final String ARG_ALBUM = "album";

    /**
     * Async cache request.
     */
    private AsyncTask cacheTask;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Album album = (Album) getArguments().getSerializable(ARG_ALBUM);

        // Inflate the view
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        final AQuery aq = new AQuery(view);

        // Populate the view with the given data
        aq.id(R.id.txtName).text(album.getName());

        // Set a new adapter to the tracks list
        aq.id(R.id.listTracks).adapter(new TracksAdapter(getActivity(), album, new ArrayList<Track>()));

        // Grab cached tracks for this album
        cacheTask = new AsyncTask<Album, Void, List<Track>>() {
            @Override
            protected List<Track> doInBackground(Album... params) {
                return CacheUtil.getCachedTrack(params[0]);
            }

            @Override
            protected void onPostExecute(List<Track> tracks) {
                TracksAdapter adapter = (TracksAdapter) aq.id(R.id.listTracks).getListView().getAdapter();
                adapter.setTracks(tracks);
            }
        }.execute(album);

        // Download the album details from the server
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
                TracksAdapter adapter = (TracksAdapter) aq.id(R.id.listTracks).getListView().getAdapter();
                adapter.setTracks(tracks);
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        if (cacheTask != null) {
            cacheTask.cancel(true);
        }
        super.onDestroyView();
    }
}