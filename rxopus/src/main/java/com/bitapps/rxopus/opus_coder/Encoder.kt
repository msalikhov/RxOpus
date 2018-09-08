package com.bitapps.rxopus.opus_coder

import com.bitapps.rxopus.Initializable
import com.bitapps.rxopus.RxOpusDefaults
import com.bitapps.rxopus.calculateFrameSize
import com.bitapps.rxopus.log
import com.score.rahasak.utils.OpusEncoder

class Encoder(
        val frameLengthMsec: Int,
        val channelsCount: Int,
        val bitrate: Int,
        val complexity: Int,
        val opusApplication: Int
) : Initializable {

    val frameSize = calculateFrameSize(RxOpusDefaults.SAMPLE_RATE, frameLengthMsec)

    private val opusEncoder = OpusEncoder()

    override fun init() = with(opusEncoder) {
        init(RxOpusDefaults.SAMPLE_RATE, channelsCount, opusApplication)
        setBitrate(bitrate)
        setComplexity(complexity)
        log("opus encoder initialized")
    }

    fun encodeFrame(income: ShortArray): ByteArray {
        val buffer = ByteArray(income.size * 2)
        val size = opusEncoder.encode(income, frameSize, buffer)
        return buffer.copyOfRange(0, size)
    }

    override fun release() {
        opusEncoder.close()
        log("opus encoder released")
    }
}