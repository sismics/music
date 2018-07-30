package com.sismics.music.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.androidquery.AQuery;
import com.mobeta.android.dslv.DragSortListView;
import com.sismics.music.R;
import com.sismics.music.adapter.PlaylistAdapter;
import com.sismics.music.db.dao.PlaylistDao;
import com.sismics.music.event.MediaPlayerSeekEvent;
import com.sismics.music.event.MediaPlayerStateChangedEvent;
import com.sismics.music.event.PlaylistChangedEvent;
import com.sismics.music.event.TrackCacheStatusChangedEvent;
import com.sismics.music.event.TrackLikedChangedEvent;
import com.sismics.music.model.ApplicationContext;
import com.sismics.music.model.PlaylistTrack;
import com.sismics.music.service.MusicService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

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
                ApplicationContext.getInstance().getPlaylistService().clear(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the view
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        aq = new AQuery(view);
        DragSortListView listTracks = (DragSortListView) aq.id(R.id.listTracks).getView();
        seekBar = aq.id(R.id.seekBar).getSeekBar();
        aq.id(R.id.playlistPause).gone();

        // Create a new playlist adapter
        playlistAdapter = new PlaylistAdapter(getActivity(), listTracks);

        // Configure the tracks list
        aq.id(R.id.listTracks)
                .adapter(playlistAdapter)
                .itemClicked((parent, view1, position, id) -> {
                    ApplicationContext.getInstance().getPlaylistService().change(position - 1);
                    Intent intent = new Intent(MusicService.ACTION_PLAY, null, getActivity(), MusicService.class);
                    intent.putExtra(MusicService.EXTRA_FORCE, true);
                    getActivity().startService(intent);
                })
                .getListView()
                .setEmptyView(view.findViewById(R.id.emptyPlaylist));

        // Play button
        aq.id(R.id.playlistPlay).clicked(v -> {
            Intent intent = new Intent(MusicService.ACTION_PLAY, null, getActivity(), MusicService.class);
            getActivity().startService(intent);
        });

        // Pause button
        aq.id(R.id.playlistPause).clicked(v -> {
            Intent intent = new Intent(MusicService.ACTION_PAUSE, null, getActivity(), MusicService.class);
            getActivity().startService(intent);
        });

        // Stop button
        aq.id(R.id.playlistStop).clicked(v -> {
            Intent intent = new Intent(MusicService.ACTION_STOP, null, getActivity(), MusicService.class);
            getActivity().startService(intent);
        });

        // Track removed
        listTracks.setRemoveListener(position -> {
            if (ApplicationContext.getInstance().getPlaylistService().getCurrentTrackIndex() == position) {
                Intent intent = new Intent(MusicService.ACTION_STOP, null, getActivity(), MusicService.class);
                getActivity().startService(intent);
            }
            ApplicationContext.getInstance().getPlaylistService().remove(position);
        });

        // Track moved
        listTracks.setDropListener(ApplicationContext.getInstance().getPlaylistService()::move);

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
    @Subscribe
    public void onEvent(PlaylistChangedEvent event) {
        playlistAdapter.notifyDataSetChanged();

        // Save the current playlist
        List<PlaylistDao.PlaylistItem> playlistItemList = new ArrayList<>();
        for (PlaylistTrack playlistTrack : ApplicationContext.getInstance().getPlaylistService().getPlaylistTrackList()) {
            playlistItemList.add(new PlaylistDao.PlaylistItem(playlistTrack.getTrack().getId(), playlistTrack.getAlbum().getId(), playlistTrack.getArtist().getId()));
        }
        PlaylistDao.savePlaylist(getContext(), playlistItemList);
    }

    /**
     * The cache status of a track has changed.
     * @param event Event
     */
    @Subscribe
    public void onEvent(TrackCacheStatusChangedEvent event) {
        playlistAdapter.notifyDataSetChanged();
    }

    /**
     * A track liked status has changed.
     * @param event Event
     */
    @Subscribe
    public void onEvent(TrackLikedChangedEvent event) {
        playlistAdapter.notifyDataSetChanged();
    }

    /**
     * Media player state has changed.
     * @param event Event
     */
    @Subscribe
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