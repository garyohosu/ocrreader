package com.garyohosu.ocrreader.audio

import android.media.AudioManager
import android.media.ToneGenerator
import com.garyohosu.ocrreader.domain.SoundEvent

class FeedbackSoundPlayer {

    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME)

    fun play(event: SoundEvent) {
        try {
            when (event) {
                SoundEvent.BEEP -> toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                SoundEvent.OK   -> toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 200)
                SoundEvent.NG   -> toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 500)
            }
        } catch (_: Exception) {
            // 音声が利用できない端末では無視
        }
    }

    fun release() {
        toneGenerator.release()
    }
}
