/*   
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sismics.music.service

import android.content.Context
import android.media.AudioManager

/**
 * Convenience class to deal with audio focus. This class deals with everything related to audio
 * focus: it can request and abandon focus, and will intercept focus change events and deliver
 * them to a MusicFocusable interface (which, in our case, is implemented by [MusicService]).
 * This class can only be used on SDK level 8 and above, since it uses API features that are not
 * available on previous SDK's.
 */
class AudioFocusHelper(ctx: Context, internal var mFocusable: MusicFocusable?) : AudioManager.OnAudioFocusChangeListener {
    internal var mAM: AudioManager

    init {
        mAM = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    /** Requests audio focus. Returns whether request was successful or not.  */
    fun requestFocus(): Boolean {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAM.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
    }

    /** Abandons audio focus. Returns whether request was successful or not.  */
    fun abandonFocus(): Boolean {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAM.abandonAudioFocus(this)
    }

    /**
     * Called by AudioManager on audio focus changes. We implement this by calling our
     * MusicFocusable appropriately to relay the message.
     */
    override fun onAudioFocusChange(focusChange: Int) {
        if (mFocusable == null) return
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> mFocusable!!.onGainedAudioFocus()
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> mFocusable!!.onLostAudioFocus(false)
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> mFocusable!!.onLostAudioFocus(true)
        }
    }
}
