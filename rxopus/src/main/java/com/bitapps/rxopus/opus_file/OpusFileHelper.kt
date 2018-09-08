package com.bitapps.rxopus.opus_file

import com.bitapps.rxopus.Initializable
import com.bitapps.rxopus.RxOpusDefaults
import com.bitapps.rxopus.log
import org.gagravarr.ogg.OggFile
import org.gagravarr.opus.OpusAudioData
import org.gagravarr.opus.OpusFile
import java.io.ByteArrayInputStream
import java.io.OutputStream

class OpusFileHelper(
        val outputStream: OutputStream,
        val channelsCount: Int,
        val frameSize: Int
) : Initializable {

    private var opusFile: OpusFile? = null

    override fun init() {
        opusFile = OpusFile(outputStream).apply {
            tags.vendor = "M.Salikhov"
            info.setSampleRate(RxOpusDefaults.SAMPLE_RATE.toLong())
            info.numChannels = channelsCount
            info.preSkip = RxOpusDefaults.PRE_SKIP
            setFrameSize(frameSize)
        }
        log("opusfile created")
    }

    fun writePacket(packet: ByteArray) {
        opusFile!!.writeAudioData(OpusAudioData(packet))
    }

    fun setPackets(packets: List<ByteArray>) {
        val oad = packets.map { OpusAudioData(it) }
        opusFile!!.setAudioData(oad)
    }

    fun extractPacketsFromOpusFile(file: ByteArray) = ByteArrayInputStream(file)
            .let(::OggFile)
            .let(::OpusFile)
            .audioPackets as List<OpusAudioData>

    override fun release() {
        opusFile?.close()
        opusFile = null
        log("opusfile closed")
    }
}