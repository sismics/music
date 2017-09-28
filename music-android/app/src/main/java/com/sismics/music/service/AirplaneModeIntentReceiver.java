package com.sismics.music.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sismics.music.event.OfflineModeChangedEvent;
import com.sismics.music.util.PreferenceUtil;

import org.greenrobot.eventbus.EventBus;

public class AirplaneModeIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isAirplaneModeOn = intent.getBooleanExtra("state", false);
        if (isAirplaneModeOn) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.edit().putBoolean(PreferenceUtil.Pref.OFFLINE_MODE.toString(), true).commit();
            EventBus.getDefault().post(new OfflineModeChangedEvent(true));
        }
    }
}
