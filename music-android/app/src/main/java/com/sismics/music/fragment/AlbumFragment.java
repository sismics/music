package com.sismics.music.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.androidquery.AQuery;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.music.R;
import com.sismics.music.resource.AlbumResource;
import com.sismics.music.ui.adapter.AlbumAdapter;
import com.sismics.music.ui.adapter.TracksAdapter;
import com.sismics.music.util.PreferenceUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AlbumFragment extends Fragment {
        /**
         * Returns a new instance of this fragment.
         */
        public static AlbumFragment newInstance(JSONObject item) {
            AlbumFragment fragment = new AlbumFragment();
            Bundle args = new Bundle();
            args.putString("item", item.toString());
            fragment.setArguments(args);
            return fragment;
        }

        public AlbumFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            String json = getArguments().getString("item");
            JSONObject album;
            try {
                album = new JSONObject(json);
            } catch (JSONException e) {
                return null;
            }

            View view = inflater.inflate(R.layout.fragment_album, container, false);
            final AQuery aq = new AQuery(view);

            aq.id(R.id.txtName).text(album.optString("name"));

            AlbumResource.info(getActivity(), album.optString("id"), new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(final JSONObject json) {
                    if (getActivity() == null) {
                        // The activity is dead, and this fragment has been detached
                        return;
                    }

                    ListView listView = aq.id(R.id.listTracks).getListView();
                    JSONArray tracks = json.optJSONArray("tracks");

                    listView.setAdapter(new TracksAdapter(getActivity(), tracks));
                }
            });

            return view;
        }
    }