package com.cidetraq.flashlight_strobe_music

import android.media.AudioRecord

class NoiseRecorder(audioRecord: AudioRecord, buffSize: Int) {
    var recorder: AudioRecord
    var bufferSize = 0
    private val TAG =
        "EXTERNALCODE - "//the value 51805.5336 can be derived from asuming that x=32767=0.6325 Pa and x=1 = 0.00002 Pa (the reference value)

    //
//        bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_DEFAULT,AudioFormat.ENCODING_PCM_16BIT);
    //making the buffer bigger....
    //        AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
//                44100, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
    @get:Throws(NoValidNoiseLevelException::class)
    val noiseLevel: Double
        //x=max;
        // calculating the pascal pressure based on the idea that the max amplitude (between 0 and 32767) is
        // relative to the pressure
        get() {
//
//        bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_DEFAULT,AudioFormat.ENCODING_PCM_16BIT);
            //making the buffer bigger....
//            bufferSize = bufferSize * 4
            //        AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
//                44100, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            bufferSize *= 4
            val data = ShortArray(bufferSize)
            var average = 0.0
            println(TAG + "start new recording process")
            recorder.startRecording()
            recorder.read(data, 0, bufferSize)
            recorder.stop()
            println(TAG + "stop recording process")
            for (s in data) {
                if (s > 0) {
                    average += Math.abs(s.toInt()).toDouble()
                } else {
                    bufferSize--
                }
            }
            //x=max;
            val x = average / bufferSize
            println(TAG + "" + x)
//            recorder.release()
            println(TAG + "getNoiseLevel() ")
            var db = 0.0
            if (x == 0.0) {
                val e = NoValidNoiseLevelException(x)
                throw e
            }
            // calculating the pascal pressure based on the idea that the max amplitude (between 0 and 32767) is
            // relative to the pressure
            val pressure =
                x / 51805.5336 //the value 51805.5336 can be derived from asuming that x=32767=0.6325 Pa and x=1 = 0.00002 Pa (the reference value)
            println(TAG + "x=" + pressure + " Pa")
            db = 20 * Math.log10(pressure / REFERENCE)
            println(TAG + "db=" + db)
            if (db > 0) {
                return db
            }
            val e = NoValidNoiseLevelException(x)
            throw e
        }

    companion object {
        var REFERENCE = 0.00002
    }

    init {
        bufferSize = buffSize
        recorder = audioRecord
    }
}