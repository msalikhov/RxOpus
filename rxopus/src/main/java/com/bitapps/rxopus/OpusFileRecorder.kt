package com.bitapps.rxopus

import com.bitapps.rxopus.opus_coder.Encoder
import com.bitapps.rxopus.opus_file.OpusFileHelper
import com.bitapps.rxopus.recorder.AudioRecorder
import com.score.rahasak.utils.OpusEncoder
import io.reactivex.schedulers.Schedulers
import java.io.OutputStream

class OpusFileRecorder(
        outputStream: OutputStream,
        frameLengthMsec: Int = RxOpusDefaults.FRAME_LENGTH_MSEC,
        channelsCount: Int = 1,
        bitrate: Int = RxOpusDefaults.BIT_RATE,
        complexity: Int = RxOpusDefaults.COMPLEXITY,
        opusApplication: Int = OpusEncoder.OPUS_APPLICATION_VOIP
) : Initializable {

    private val encoder = Encoder(frameLengthMsec, channelsCount, bitrate, complexity, opusApplication)
    private val recorder = AudioRecorder(frameLengthMsec, channelsCount)
    private val opusFileHelper = OpusFileHelper(outputStream, channelsCount, encoder.frameSize)
    private val initializables = listOf(encoder, recorder, opusFileHelper)

    @Volatile
    private var needToStop = false

    override fun init() {
        initializables.forEach(Initializable::init)
    }

    fun startRecord() {
        recorder
                .record()
                .takeUntil {
                    needToStop
                }
                .map {
                    encoder.encodeFrame(it)
                }
                .subscribeOn(Schedulers.computation())
                .subscribe({
                    opusFileHelper.writePacket(it)
                }, {
                    it.printStackTrace()
                })
    }

    fun stopRecord() {
        needToStop = true
    }

    override fun release() {
        initializables.forEach(Initializable::release)
    }
}