package com.sismics.music.activity

import android.app.*
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v13.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import com.loopj.android.http.JsonHttpResponseHandler
import com.sismics.music.R
import com.sismics.music.event.OfflineModeChangedEvent
import com.sismics.music.fragment.MyMusicFragment
import com.sismics.music.fragment.PlaylistFragment
import com.sismics.music.model.ApplicationContext
import com.sismics.music.resource.UserResource
import com.sismics.music.util.PreferenceUtil
import com.sismics.music.util.ScrobbleUtil
import de.greenrobot.event.EventBus
import java.util.*

/**
 * Main activity.
 * @author bgamard
 */
class MainActivity : Activity(), ActionBar.TabListener {
    /**
     * The [ViewPager] that will host the tab contents.
     */
    private var viewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if logged in
        if (!ApplicationContext.isLoggedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Inflate the layout
        setContentView(R.layout.activity_main)

        // Set up the action bar
        val actionBar = actionBar
        actionBar!!.navigationMode = ActionBar.NAVIGATION_MODE_TABS

        // Create the adapter that will return a fragment for each tab
        val sectionsPagerAdapter = SectionsPagerAdapter(fragmentManager)

        // Set up the ViewPager with the sections adapter
        viewPager = findViewById(R.id.pager) as ViewPager
        viewPager!!.adapter = sectionsPagerAdapter

        // Keeps the tabs and the ViewPager in sync
        viewPager!!.setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                actionBar.setSelectedNavigationItem(position)
            }
        })

        // For each of the sections in the app, add a tab to the action bar
        for (i in 0..sectionsPagerAdapter.count - 1) {
            // Create a tab with text corresponding to the page title
            actionBar.addTab(
                    actionBar.newTab().setText(sectionsPagerAdapter.getPageTitle(i)).setTabListener(this))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present
        menuInflater.inflate(R.menu.main, menu)
        menu.findItem(R.id.offline_mode).setIcon(
                if (PreferenceUtil.getBooleanPreference(this, PreferenceUtil.Pref.OFFLINE_MODE, false))
                    R.drawable.ic_action_make_available_offline_dark
                else
                    R.drawable.ic_action_network_wifi)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> return true

            R.id.offline_mode -> {
                val offlineMode = PreferenceUtil.getBooleanPreference(this, PreferenceUtil.Pref.OFFLINE_MODE, false)
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                sharedPreferences.edit().putBoolean(PreferenceUtil.Pref.OFFLINE_MODE.toString(), !offlineMode).commit()
                item.setIcon(if (offlineMode) R.drawable.ic_action_network_wifi else R.drawable.ic_action_make_available_offline_dark)
                EventBus.getDefault().post(OfflineModeChangedEvent(!offlineMode))
                return true
            }

            R.id.remote_control -> {
                startActivity(Intent(this@MainActivity, RemoteActivity::class.java))
                return true
            }

            R.id.logout -> {
                UserResource.logout(applicationContext, object : JsonHttpResponseHandler() {
                    override fun onFinish() {
                        // Force logout in all cases, so the user is not stuck in case of network error
                        ApplicationContext.setUserInfo(applicationContext, null)
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                    }
                })
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onTabSelected(tab: ActionBar.Tab, fragmentTransaction: FragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager
        viewPager!!.currentItem = tab.position
    }

    override fun onTabUnselected(tab: ActionBar.Tab, fragmentTransaction: FragmentTransaction) {
    }

    override fun onTabReselected(tab: ActionBar.Tab, fragmentTransaction: FragmentTransaction) {
    }

    override fun onResume() {
        super.onResume()

        // The main activity is resumed, it's time to try to scrobble
        ScrobbleUtil.sync(this)
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to one of the tab.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment? {
            when (position) {
                0 -> return MyMusicFragment.newInstance()
                1 -> return PlaylistFragment.newInstance()
            }
            return null
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            val l = Locale.getDefault()
            when (position) {
                0 -> return getString(R.string.my_music).toUpperCase(l)
                1 -> return getString(R.string.now_playing).toUpperCase(l)
            }
            return null
        }
    }
}
