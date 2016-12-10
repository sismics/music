package com.sismics.music.fragment

import android.app.Fragment
import android.app.FragmentTransaction
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.sismics.music.R
import com.sismics.music.event.AlbumOpenedEvent
import com.sismics.music.event.MyMusicMenuVisibilityChangedEvent

import de.greenrobot.event.EventBus

/**
 * Fragment displaying the music collection.
 *
 * @author bgamard
 */
class MyMusicFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        EventBus.getDefault().post(MyMusicMenuVisibilityChangedEvent(menuVisible))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the view
        val view = inflater.inflate(R.layout.fragment_my_music, container, false)

        if (savedInstanceState == null) {
            // Do first time initialization, add initial fragment
            val newFragment = AlbumListFragment.newInstance()
            val ft = fragmentManager.beginTransaction()
            ft.add(R.id.content, newFragment)
            ft.commit()
        }

        EventBus.getDefault().register(this)
        return view
    }

    override fun onDestroyView() {
        EventBus.getDefault().unregister(this)
        super.onDestroyView()
    }

    /**
     * Open an album details.
     * @param event Event
     */
    fun onEvent(event: AlbumOpenedEvent) {
        // Instantiate a new fragment
        val newFragment = AlbumFragment.newInstance(event.album)

        // Add the fragment to the activity, pushing this transaction on to the back stack
        val ft = fragmentManager.beginTransaction()
        ft.replace(R.id.content, newFragment)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        ft.addToBackStack(null)
        ft.commit()
    }

    companion object {

        /**
         * Returns a new instance of this fragment.
         */
        fun newInstance(): MyMusicFragment {
            val fragment = MyMusicFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}