package com.sismics.music.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sismics.music.R;
import com.sismics.music.event.PlaylistOpenedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Fragment displaying the playlists.
 *
 * @author jtremeaux
 */
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
//        EventBus.getDefault().post(new MyMusicMenuVisibilityChangedEvent(menuVisible));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the view
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        if (savedInstanceState == null) {
            // Do first time initialization, add initial fragment
            Fragment newFragment = PlaylistListFragment.newInstance();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.playlistContent, newFragment);
            ft.commit();
        }

        EventBus.getDefault().register(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    /**
     * Open an playlist details.
     * @param event Event
     */
    @Subscribe
    public void onEvent(PlaylistOpenedEvent event) {
        if (getFragmentManager().findFragmentByTag("PlaylistDetailFragment") == null) {
            // Instantiate a new fragment
            Fragment newFragment = PlaylistDetailFragment.newInstance(event.getPlaylist());

            // Add the fragment to the activity, pushing this transaction on to the back stack
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.playlistContent, newFragment, "PlaylistDetailFragment");
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.addToBackStack(null);
            ft.commit();
        }
    }
}