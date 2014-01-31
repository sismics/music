package com.sismics.music.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
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
         * Returns a new instance of this fragment.
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

            if (savedInstanceState == null) {
                // Do first time initialization -- add initial fragment.
                Fragment newFragment = AlbumListFragment.newInstance(getId());
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.add(R.id.content, newFragment);
                ft.commit();
            }

            return view;
        }

    public void openAlbum(JSONObject item) {
        // Instantiate a new fragment.
        Fragment newFragment = AlbumFragment.newInstance(item);

        // Add the fragment to the activity, pushing this transaction
        // on to the back stack.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content, newFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }
}