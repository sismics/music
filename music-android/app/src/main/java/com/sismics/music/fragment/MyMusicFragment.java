package com.sismics.music.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sismics.music.R;
import com.sismics.music.event.AlbumOpenedEvent;
import com.sismics.music.event.MyMusicMenuVisibilityChangedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Fragment displaying the music collection.
 *
 * @author bgamard
 */
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        EventBus.getDefault().post(new MyMusicMenuVisibilityChangedEvent(menuVisible));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the view
        View view = inflater.inflate(R.layout.fragment_my_music, container, false);

        if (savedInstanceState == null) {
            // Do first time initialization, add initial fragment
            Fragment newFragment = AlbumListFragment.newInstance();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.content, newFragment);
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
     * Open an album details.
     * @param event Event
     */
    @Subscribe
    public void onEvent(AlbumOpenedEvent event) {
        // Instantiate a new fragment
        Fragment newFragment = AlbumFragment.newInstance(event.getArtist(), event.getAlbum());

        // Add the fragment to the activity, pushing this transaction on to the back stack
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content, newFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }
}