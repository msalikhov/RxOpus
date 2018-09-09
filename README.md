# RxOpus
Set of tools to work with Opus. Written on Kotlin and RxJava2

Based on https://github.com/Gagravarr/VorbisJava and https://github.com/martoreto/opuscodec

To use this you should add rxopus dir as module to your app

To simply record some audio from mic and encode it with opus codec - just use OpusFileRecorder class (see example app)

For more complicated cases you could use:

Encoder - to encode raw audio frame to opus packet
Decoder - to decode opus packet into raw audio
OpusFileHelper - to make .opus file from opus packets or to extract them from file
AudioRecorder - to simply read data from mic

Important things to notice:

frameLengthMsec may be any multiple of 2.5 ms, up to a maximum of 120 ms (recommended value 20ms)
channels count should be only 1 or 2
if you using lib classes directly without OpusFileRecorder - don't mess the frameLengthMsec and channelsCount (they should be the same across all classes)
