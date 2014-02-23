package com.sismics.music.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.music.R;
import com.sismics.music.fragment.MyMusicFragment;
import com.sismics.music.fragment.PlaylistFragment;
import com.sismics.music.model.ApplicationContext;
import com.sismics.music.resource.UserResource;
import com.sismics.music.util.ScrobbleUtil;

import java.util.Locale;

/**
 * Main activity.
 *
 * @author bgamard
 */
public class MainActivity extends Activity implements ActionBar.TabListener {

    /**
     * Custom tab pager adapter.
     */
    SectionsPagerAdapter sectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the tab contents.
     */
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if logged in
        if (!ApplicationContext.getInstance().isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Inflate the layout
        setContentView(R.layout.activity_main);

        // Set up the action bar
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each tab
        sectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        // Keeps the tabs and the ViewPager in sync
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar
        for (int i = 0; i < sectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(sectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                return true;

            case R.id.logout:
                UserResource.logout(getApplicationContext(), new JsonHttpResponseHandler() {
                    @Override
                    public void onFinish() {
                        // Force logout in all cases, so the user is not stuck in case of network error
                        ApplicationContext.getInstance().setUserInfo(getApplicationContext(), null);
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    }
                });
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    protected void onResume() {
        super.onResume();

        // The main activity is resumed, it's time to try to scrobble
        ScrobbleUtil.sync(this);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the tab.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return MyMusicFragment.newInstance();
                case 1:
                    return PlaylistFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.my_music).toUpperCase(l);
                case 1:
                    return getString(R.string.now_playing).toUpperCase(l);
            }
            return null;
        }
    }
}
