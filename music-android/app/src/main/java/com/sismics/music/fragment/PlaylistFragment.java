package com.sismics.music.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SeekBar;

import com.androidquery.AQuery;
import com.mobeta.android.dslv.DragSortListView;
import com.sismics.music.R;
import com.sismics.music.event.MediaPlayerSeekEvent;
import com.sismics.music.event.MediaPlayerStateChangedEvent;
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
    private SeekBar seekBar;
    private AQuery aq;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.playlist, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.empty_playlist:
                // Stop the music and clear the playlist
                Intent intent = new Intent(MusicService.ACTION_STOP, null, getActivity(), MusicService.class);
                getActivity().startService(intent);
                PlaylistService.clear(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the view
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        aq = new AQuery(view);
        DragSortListView listTracks = (DragSortListView)aq.id(R.id.listTracks).getView();
        seekBar = aq.id(R.id.seekBar).getSeekBar();
        aq.id(R.id.playlistPause).gone();

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
                })
                .getListView()
                .setEmptyView(view.findViewById(R.id.emptyPlaylist));

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

        // Seekbar moved
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    eventBus.post(new MediaPlayerSeekEvent(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
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

    /**
     * Media player state has changed.
     * @param event Event
     */
    public void onEvent(MediaPlayerStateChangedEvent event) {
        if (event.getState() == MusicService.State.Playing) {
            aq.id(R.id.playlistPause).visible();
            aq.id(R.id.playlistPlay).gone();
        } else {
            aq.id(R.id.playlistPause).gone();
            aq.id(R.id.playlistPlay).visible();
        }

        if (event.getState() == MusicService.State.Playing || event.getState() == MusicService.State.Paused) {
            seekBar.setEnabled(true);
        } else {
            seekBar.setEnabled(false);
            seekBar.setProgress(0);
        }

        if (event.getDuration() < 0) {
            return;
        }

        seekBar.setProgress(event.getCurrentPosition());
        seekBar.setMax(event.getDuration());
    }

    @Override
    public void onDestroyView() {
        eventBus.unregister(this);
        super.onDestroyView();
    }
}