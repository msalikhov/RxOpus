package com.bitapps.rxopus

import android.util.Log

internal fun log(vararg args: Any?) = args.joinToString().let {
    Log.d("RxOpus", it)
    Unit
}

internal fun calculateFrameSize(sampleRate: Int, frameLengthMsec: Int) = sampleRate / 1000 * frameLengthMsec