package com.sismics.music.util

import android.app.ProgressDialog
import android.content.Context
import android.widget.Toast
import com.loopj.android.http.JsonHttpResponseHandler
import com.sismics.music.R
import com.sismics.music.resource.PlayerResource
import org.apache.http.Header
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Utility class for remote controlling.

 * @author bgamard.
 */
object RemoteControlUtil {
    enum class Command {
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

     * @param context Context
     * *
     * @param token Token
     */
    fun connect(context: Context, token: String) {
        val progressDialog = ProgressDialog(context)
        progressDialog.isIndeterminate = true
        progressDialog.setMessage(context.getString(R.string.connecting_player))
        progressDialog.show()

        PlayerResource.command(context, token, buildCommand(Command.HELLO), object : JsonHttpResponseHandler() {
            override fun onSuccess(json: JSONObject?) {
                PreferenceUtil.setPlayerToken(context, token)
                Toast.makeText(context, R.string.success_connecting_player, Toast.LENGTH_LONG).show()
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>, responseBytes: ByteArray?, throwable: Throwable) {
                Toast.makeText(context, context.getString(R.string.fail_connecting_player) + ": " + String(responseBytes!!), Toast.LENGTH_LONG).show()
            }

            override fun onFinish() {
                progressDialog.dismiss()
            }
        })
    }

    /**
     * Build a command.

     * @param command Command
     * @param data Additionnal data
     * @return Built command
     */
    fun buildCommand(command: Command, vararg data: Any): String {
        try {
            val json = JSONObject()
            json.put("command", command.name)
            if (data.size > 0) {
                val dataArray = JSONArray()
                for (dataItem in data) {
                    dataArray.put(dataItem)
                }
                json.put("data", dataArray)
            }
            return json.toString()
        } catch (e: JSONException) {
            throw RuntimeException("Error building command", e)
        }
    }

    /**
     * Send a command.
     * @param context Context
     * @param command Command
     * @param resId Success string ID
     */
    fun sendCommand(context: Context, command: String, resId: Int) {
        val token = PreferenceUtil.getStringPreference(context, PreferenceUtil.Pref.PLAYER_TOKEN)
        if (token == null || token.isEmpty()) {
            Toast.makeText(context, R.string.no_player_connected, Toast.LENGTH_LONG).show()
            return
        }

        PlayerResource.command(context, token, command, object : JsonHttpResponseHandler() {
            override fun onSuccess(json: JSONObject?) {
                if (resId != 0) {
                    Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>, responseBytes: ByteArray?, throwable: Throwable) {
                Toast.makeText(context, R.string.fail_sending_command, Toast.LENGTH_LONG).show()
            }
        })
    }
}
