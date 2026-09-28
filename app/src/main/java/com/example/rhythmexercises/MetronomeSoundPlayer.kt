package com.example.rhythmexercises

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

class MetronomeSoundPlayer(context: Context) {
    private val soundPool: SoundPool
    private var highSoundId: Int = 0
    private var lowSoundId: Int = 0

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()

        // raw フォルダの音源を読み込み（拡張子なしで指定します）
        highSoundId = soundPool.load(context, R.raw.tick_high, 1)
        lowSoundId = soundPool.load(context, R.raw.tick_low, 1)
    }

    // beatIndex: 0（1拍目: ピッ）, 1〜3（2〜4拍目: ポッ）
    fun playBeat(beatIndex: Int) {
        val soundId = if (beatIndex == 0) highSoundId else lowSoundId
        if (soundId != 0) {
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    fun release() {
        soundPool.release()
    }
}