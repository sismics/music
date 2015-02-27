package com.sismics.music.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.music.R;
import com.sismics.music.resource.PlayerResource;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class for remote controlling.
 *
 * @author bgamard.
 */
public class RemoteControlUtil {
    public static enum Command {
        HELLO,
        PLAY_TRACK,
        PLAY_TRACKS,
        PLAY,
        PAUSE,
        NEXT,
        PREVIOUS,
        VOLUME
    }

    /**
     * "Connect" to a web player.
     * If fact, send a hello command, and save the token.
     *
     * @param context Context
     * @param token Token
     */
    public static void connect(final Context context, final String token) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(context.getString(R.string.connecting_player));
        progressDialog.show();

        PlayerResource.command(context, token, buildCommand(Command.HELLO), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject json) {
                PreferenceUtil.setPlayerToken(context, token);
                Toast.makeText(context, R.string.success_connecting_player, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(final int statusCode, final Header[] headers, final byte[] responseBytes, final Throwable throwable) {
                Toast.makeText(context, R.string.fail_connecting_player, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFinish() {
                progressDialog.dismiss();
            }
        });
    }

    /**
     * Build a command.
     *
     * @param command Command
     * @param data Additionnal data
     * @return Built command
     */
    public static String buildCommand(Command command, Object... data) {
        try {
            JSONObject json = new JSONObject();
            json.put("command", command.name());
            if (data != null && data.length > 0) {
                JSONArray dataArray = new JSONArray();
                for (Object dataItem : data) {
                    dataArray.put(dataItem);
                }
                json.put("data", dataArray);
            }
            return json.toString();
        } catch (JSONException e) {
            Log.e("RemoteControlUtil", "Error building command", e);
        }

        return null;
    }

    /**
     * Send a command.
     *
     * @param context Context
     * @param command Command
     * @param resId Success string ID
     */
    public static void sendCommand(final Context context, String command, final int resId) {
        String token = PreferenceUtil.getStringPreference(context, PreferenceUtil.Pref.PLAYER_TOKEN);
        if (token == null || token.isEmpty()) {
            Toast.makeText(context, R.string.no_player_connected, Toast.LENGTH_LONG).show();
            return;
        }

        PlayerResource.command(context, token, command, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject json) {
                if (resId != 0) {
                    Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(final int statusCode, final Header[] headers, final byte[] responseBytes, final Throwable throwable) {
                Toast.makeText(context, R.string.fail_sending_command, Toast.LENGTH_LONG).show();
            }
        });
    }
}
