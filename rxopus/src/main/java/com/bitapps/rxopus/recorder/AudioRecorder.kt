package com.bitapps.rxopus.recorder

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.bitapps.rxopus.Initializable
import com.bitapps.rxopus.RxOpusDefaults
import com.bitapps.rxopus.log
import io.reactivex.Observable

class AudioRecorder(
        val frameLengthMsec: Int,
        val channelsCount: Int
) : Initializable {
    private val readBufferSize = channelsCount * RxOpusDefaults.SAMPLE_RATE / 1000 * frameLengthMsec

    private var audioRecord: AudioRecord? = null

    override fun init() {
        val channelConfig = getChannelConfig()
        val audioSource = MediaRecorder.AudioSource.MIC
        val format = AudioFormat.ENCODING_PCM_16BIT
        val recordBufferSize = getRecordBufferSize(channelConfig, format)
        audioRecord = AudioRecord(
                audioSource,
                RxOpusDefaults.SAMPLE_RATE,
                channelConfig,
                format,
                recordBufferSize
        )
        checkInitialization()
    }

    fun record() = Observable
            .fromCallable {
                read()
            }
            .repeat()
            .filter {
                it.isNotEmpty()
            }
            .doOnSubscribe {
                audioRecord!!.startRecording()
                log("recording started")
            }
            .doFinally {
                audioRecord!!.stop()
                log("recording stopped")
            }

    override fun release() {
        audioRecord?.release()
        audioRecord = null
        log("recorder released")
    }

    private fun read(): ShortArray {
        val buffer = ShortArray(readBufferSize)
        val readSize = audioRecord!!.read(buffer, 0, readBufferSize)
        return if (readSize <= 0) shortArrayOf() else buffer
    }

    private fun getChannelConfig() = when (channelsCount) {
        1 -> AudioFormat.CHANNEL_IN_MONO
        2 -> AudioFormat.CHANNEL_IN_STEREO
        else -> throw IllegalArgumentException("supports only mono and stereo")
    }

    private fun getRecordBufferSize(channelConfig: Int, format: Int): Int {
        val minBufferSize = AudioRecord.getMinBufferSize(RxOpusDefaults.SAMPLE_RATE, channelConfig, format)

        if (minBufferSize <= 0) {
            throw Throwable("device can't record at target sample rate: (${RxOpusDefaults.SAMPLE_RATE})")
        }

        return minBufferSize * RxOpusDefaults.RECORD_BUFFER_SIZE_MULTIPLIER
    }

    private fun checkInitialization() = if (audioRecord!!.state == AudioRecord.STATE_INITIALIZED) {
        log("audio record initialized")
    } else {
        throw Throwable("audio record init failed")
    }
}