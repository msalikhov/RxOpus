package com.bitapps.rxopus.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.bitapps.rxopus.OpusFileRecorder
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(), View.OnClickListener, TextWatcher {

    companion object {
        private const val STATE_INIT = 0
        private const val STATE_WAITING = 1
        private const val STATE_RECORDING = 2

        private val neededPermissions = arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private var opusFileRecorder: OpusFileRecorder? = null
    private var state by Delegates.observable(STATE_INIT) { _, _, newValue ->
        onstateChanged(newValue)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        filename_field.addTextChangedListener(this)
        button_filename_done.setOnClickListener(this)
        button_save.setOnClickListener(this)
        button_start.setOnClickListener(this)
        button_stop.setOnClickListener(this)

        state = STATE_INIT
        afterTextChanged(filename_field.text)
    }

    override fun onClick(v: View) = when (v.id) {
        R.id.button_start -> startRecord()
        R.id.button_stop -> stopRecord()
        R.id.button_filename_done -> initFile()
        R.id.button_save -> saveFile()
        else -> throw IllegalArgumentException("unknown view: ${v.id}")
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
    override fun afterTextChanged(s: Editable) {
        button_filename_done.isEnabled = s.isNotEmpty()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (getRequiredPermissionNames(neededPermissions).isEmpty()) {
            initFile()
        }
    }

    fun onstateChanged(newState: Int) = when (newState) {
        STATE_INIT -> onStateInit()
        STATE_WAITING -> onStateWaiting()
        STATE_RECORDING -> onStateRecording()
        else -> throw IllegalArgumentException("unknown state")
    }

    private fun initFile() {
        val requiredPermissionNames = getRequiredPermissionNames(neededPermissions)
        if (requiredPermissionNames.isNotEmpty()) {
            requestPermissions(requiredPermissionNames.toTypedArray())
            return
        }
        val f = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                filename_field.text.toString() + ".opus")
        opusFileRecorder = OpusFileRecorder(
                outputStream = f.outputStream(),
                channelsCount = 2,
                bitrate = 131_072)
        opusFileRecorder!!.init()
        state = STATE_WAITING
    }

    private fun startRecord() {
        opusFileRecorder!!.startRecord()
        state = STATE_RECORDING
    }

    private fun stopRecord() {
        opusFileRecorder!!.stopRecord()
        state = STATE_WAITING
    }

    private fun saveFile() {
        opusFileRecorder!!.release()
        state = STATE_INIT
    }

    private fun onStateInit() {
        button_filename_done.visibility = View.VISIBLE
        filename_field.isEnabled = true
        button_stop.isEnabled = false
        button_start.isEnabled = false
        button_save.isEnabled = false
    }

    private fun onStateWaiting() {
        button_filename_done.visibility = View.INVISIBLE
        filename_field.isEnabled = false
        button_stop.isEnabled = false
        button_start.isEnabled = true
        button_save.isEnabled = true
    }

    private fun onStateRecording() {
        button_filename_done.visibility = View.INVISIBLE
        filename_field.isEnabled = false
        button_stop.isEnabled = true
        button_start.isEnabled = false
        button_save.isEnabled = false
    }

    private fun hasPermission(name: String) = ContextCompat.checkSelfPermission(this, name) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions(names: Array<String>) = ActivityCompat.requestPermissions(this, names, 300)

    private fun getRequiredPermissionNames(names: Array<String>): List<String> {
        val required = mutableListOf<String>()
        names.forEach {
            if (!hasPermission(it)) required.add(it)
        }
        return required
    }
}
