package com.bitapps.rxopus.opus_coder

import com.bitapps.rxopus.Initializable
import com.bitapps.rxopus.RxOpusDefaults
import com.bitapps.rxopus.calculateFrameSize
import com.bitapps.rxopus.log
import com.score.rahasak.utils.OpusDecoder

class Decoder(
        val frameLengthMsec: Int,
        val channelsCount: Int
) : Initializable {

    val frameSize = calculateFrameSize(RxOpusDefaults.SAMPLE_RATE, frameLengthMsec)

    private val opusDecoder = OpusDecoder()

    override fun init() {
        opusDecoder.init(RxOpusDefaults.SAMPLE_RATE, channelsCount)
        log("opus decoder initialized")
    }

    fun decodeFrame(income: ByteArray): ShortArray {
        val buffer = ShortArray(income.size * 10)
        val size = opusDecoder.decode(income, buffer, frameSize)
        return buffer.copyOfRange(0, size)
    }

    override fun release() {
        opusDecoder.close()
        log("opus decoder released")
    }
}