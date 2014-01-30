package com.sismics.music.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.androidquery.AQuery;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.music.R;
import com.sismics.music.resource.AlbumResource;
import com.sismics.music.ui.adapter.AlbumAdapter;
import com.sismics.music.util.PreferenceUtil;

import org.json.JSONArray;
import org.json.JSONObject;

public class MyMusicFragment extends Fragment {
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static MyMusicFragment newInstance() {
            MyMusicFragment fragment = new MyMusicFragment();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        public MyMusicFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_my_music, container, false);
            final AQuery aq = new AQuery(view);

            JSONObject cache = PreferenceUtil.getCachedJson(getActivity(), PreferenceUtil.Pref.CACHED_ALBUMS_LIST_JSON);
            if (cache != null) {
                aq.id(R.id.listAlbum).adapter(new AlbumAdapter(getActivity(), cache.optJSONArray("albums")));
            }

            AlbumResource.list(getActivity(), new JsonHttpResponseHandler() {
                public void onSuccess(final JSONObject json) {
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

            return view;
        }
    }