package com.sismics.music.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.androidquery.AQuery;
import com.mobeta.android.dslv.DragSortListView;
import com.sismics.music.R;
import com.sismics.music.event.PlaylistChangedEvent;
import com.sismics.music.event.TrackCacheStatusChangedEvent;
import com.sismics.music.service.PlaylistService;
import com.sismics.music.service.MusicService;
import com.sismics.music.adapter.PlaylistAdapter;

import de.greenrobot.event.EventBus;

/**
 * Playlist fragment.
 */
public class PlaylistFragment extends Fragment {

    private PlaylistAdapter playlistAdapter;
    private EventBus eventBus;

    /**
     * Returns a new instance of this fragment.
     */
    public static PlaylistFragment newInstance() {
        PlaylistFragment fragment = new PlaylistFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the view
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        AQuery aq = new AQuery(view);
        DragSortListView listTracks = (DragSortListView)aq.id(R.id.listTracks).getView();

        // Create a new playlist adapter
        playlistAdapter = new PlaylistAdapter(getActivity(), listTracks);

        // Configure the tracks list
        aq.id(R.id.listTracks)
                .adapter(playlistAdapter)
                .itemClicked(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        PlaylistService.change(position - 1);
                        Intent intent = new Intent(MusicService.ACTION_PLAY, null, getActivity(), MusicService.class);
                        intent.putExtra(MusicService.EXTRA_FORCE, true);
                        getActivity().startService(intent);
                    }
                });

        // Play button
        aq.id(R.id.playlistPlay).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MusicService.ACTION_PLAY, null, getActivity(), MusicService.class);
                getActivity().startService(intent);
            }
        });

        // Pause button
        aq.id(R.id.playlistPause).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MusicService.ACTION_PAUSE, null, getActivity(), MusicService.class);
                getActivity().startService(intent);
            }
        });

        // Stop button
        aq.id(R.id.playlistStop).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MusicService.ACTION_STOP, null, getActivity(), MusicService.class);
                getActivity().startService(intent);
            }
        });

        // Track removed
        listTracks.setRemoveListener(new DragSortListView.RemoveListener() {
            @Override
            public void remove(int position) {
                if (PlaylistService.getCurrentTrackIndex() == position) {
                    Intent intent = new Intent(MusicService.ACTION_STOP, null, getActivity(), MusicService.class);
                    getActivity().startService(intent);
                }
                PlaylistService.remove(position);
            }
        });

        // Track moved
        listTracks.setDropListener(new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                PlaylistService.move(from, to);
            }
        });

        eventBus = EventBus.getDefault();
        eventBus.register(this);
        return view;
    }

    /**
     * The playlist has changed.
     * @param event Event
     */
    public void onEvent(PlaylistChangedEvent event) {
        playlistAdapter.notifyDataSetChanged();
    }

    /**
     * The cache status of a track has changed.
     * @param event Event
     */
    public void onEvent(TrackCacheStatusChangedEvent event) {
        playlistAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        eventBus.unregister(this);
        super.onDestroyView();
    }
}