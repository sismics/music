package com.sismics.music.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.androidquery.AQuery;
import com.sismics.music.R;
import com.sismics.music.model.Playlist;
import com.sismics.music.service.MusicService;
import com.sismics.music.ui.adapter.PlaylistAdapter;

import org.json.JSONObject;

public class PlaylistFragment extends Fragment {
    /**
     * Returns a new instance of this fragment.
     */
    public static PlaylistFragment newInstance() {
        PlaylistFragment fragment = new PlaylistFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public PlaylistFragment() {
    }

    PlaylistAdapter playlistAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        AQuery aq = new AQuery(view);

        playlistAdapter = new PlaylistAdapter(getActivity());
        aq.id(R.id.listTracks)
                .adapter(playlistAdapter)
                .itemClicked(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Playlist.change(position - 1);
                        Intent intent = new Intent(MusicService.ACTION_PLAY, null, getActivity(), MusicService.class);
                        intent.putExtra(MusicService.EXTRA_FORCE, true);
                        getActivity().startService(intent);
                    }
                });

        aq.id(R.id.playlistPlay).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startService(new Intent(MusicService.ACTION_PLAY));
            }
        });

        aq.id(R.id.playlistPause).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startService(new Intent(MusicService.ACTION_PAUSE));
            }
        });

        aq.id(R.id.playlistStop).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startService(new Intent(MusicService.ACTION_STOP));
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        playlistAdapter.onDestroy();
        super.onDestroyView();
    }
}