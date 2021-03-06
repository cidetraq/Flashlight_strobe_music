package com.cidetraq.flashlight_strobe_music

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import android.widget.ToggleButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
class MainActivity() : AppCompatActivity() {

    object AudioPermissionHelper {
        private const val AUDIO_PERMISSION_CODE = 0
        private const val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
        private const val WRITE_FILE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE

        fun requestAudioPermission(activity: Activity){
            ActivityCompat.requestPermissions(
                    activity, arrayOf(RECORD_AUDIO_PERMISSION, WRITE_FILE_PERMISSION), AUDIO_PERMISSION_CODE)
        }

        fun hasAudioPermissions(activity: Activity): Boolean{
            return (ContextCompat.checkSelfPermission(activity, AudioPermissionHelper.RECORD_AUDIO_PERMISSION) == PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(activity, AudioPermissionHelper.WRITE_FILE_PERMISSION) == PackageManager.PERMISSION_GRANTED)
        }

        fun shouldShowRequestPermissionRationale(activity: Activity): Boolean {
            return (ActivityCompat.shouldShowRequestPermissionRationale(activity, AudioPermissionHelper.RECORD_AUDIO_PERMISSION)) &&
                    (ActivityCompat.shouldShowRequestPermissionRationale(activity, AudioPermissionHelper.WRITE_FILE_PERMISSION))
        }

        fun launchPermissionSettings(activity: Activity) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", activity.packageName, null)
            activity.startActivity(intent)
        }


    }
    var cameraID = ""

    /** Helper to ask camera permission.  */
    object CameraPermissionHelper {
        private const val CAMERA_PERMISSION_CODE = 0
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA

        /** Check to see we have the necessary permissions for this app.  */
        fun hasCameraPermission(activity: Activity): Boolean {
            return ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED
        }

        /** Check to see we have the necessary permissions for this app, and ask for them if we don't.  */
        fun requestCameraPermission(activity: Activity) {
            ActivityCompat.requestPermissions(
                    activity, arrayOf(CAMERA_PERMISSION), CAMERA_PERMISSION_CODE)
        }

        /** Check to see if we need to show the rationale for this permission.  */
        fun shouldShowRequestPermissionRationale(activity: Activity): Boolean {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, CAMERA_PERMISSION)
        }

        /** Launch Application Setting to grant permission.  */
        fun launchPermissionSettings(activity: Activity) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", activity.packageName, null)
            activity.startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this)
            }
        }
        if (!AudioPermissionHelper.hasAudioPermissions(this)) {
            Toast.makeText(this, "Audio permissions needed to run this application", Toast.LENGTH_LONG)
                    .show()
            if (!AudioPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                AudioPermissionHelper.launchPermissionSettings(this)
            }
        }

        finish()

        recreate()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this)
            return
        }
        if (!AudioPermissionHelper.hasAudioPermissions(this)) {
            AudioPermissionHelper.requestAudioPermission(this)
            return
        }
        var cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT)
        var auRecorder = AudioRecord.Builder().setBufferSizeInBytes(bufferSize).setAudioSource(MediaRecorder.AudioSource.MIC).setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(44100).setChannelMask(AudioFormat.CHANNEL_IN_DEFAULT).build()).build()
        var auRecordByteArray = ByteArray(bufferSize)
//        var audioTrack = AudioTrack(3, AudioFormat.SAMPLE_RATE_UNSPECIFIED, AudioFormat.CHANNEL_OUT_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, bufferSize, 1 )
        var audioTrack = AudioTrack.Builder().build()
//        var visualizer = Visualizer(0)
        println("Before recording")
        var noiseLevel = 0.0
        cameraID = cameraManager.cameraIdList[0]
        val toggle: ToggleButton = findViewById(R.id.torchToggle)
        toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (noiseLevel > 40.0)
                    torchOn(cameraManager)
//                auRecorder.startRecording()
            } else {
                torchOff(cameraManager)
                //evaluate audio stream
                var noiseRecorder = NoiseRecorder(auRecorder, auRecordByteArray.size)
                noiseLevel = noiseRecorder.noiseLevel
            }
        }
        println("End of oncreate")
//        var auRecorder = AudioRecord.Builder().build()



//        var recorder = MediaRecorder()
//        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_2_TS);
//        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
////        recorder.setOutputFile(PATH_NAME);
//        recorder.prepare();
//        recorder.start();   // Recording is now started
//        recorder.stop();
//        recorder.reset();   // You can reuse the object by going back to setAudioSource() step
//        recorder.release(); // Now the object cannot be reused
    }

    fun setAudioSessionId(audiosessionId: Int) {

    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun torchOn(cameraManager: CameraManager) {
        cameraManager.setTorchMode(cameraID, true)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun torchOff(cameraManager: CameraManager) {
        cameraManager.setTorchMode(cameraID, false)
    }


}