package com.umb.umbsensor

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.CompoundButtonCompat
import com.gelitenight.waveview.library.WaveView


class MainActivity : AppCompatActivity() {
    private var mWaveHelper: WaveHelper? = null

    private var mBorderColor: Int = Color.parseColor("#44FFFFFF")
    private var mBorderWidth = 10

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val waveView = findViewById<WaveView>(R.id.wave)
        waveView.setBorder(mBorderWidth, mBorderColor)
        mWaveHelper = WaveHelper(waveView, sensorManager)
        (findViewById<View>(R.id.shapeChoice) as RadioGroup)
            .setOnCheckedChangeListener { radioGroup, i ->
                when (i) {
                    R.id.shapeCircle -> waveView.setShapeType(WaveView.ShapeType.CIRCLE)
                    R.id.shapeSquare -> waveView.setShapeType(WaveView.ShapeType.SQUARE)
                    else -> {
                    }
                }
            }
        (findViewById<View>(R.id.seekBar) as SeekBar)
            .setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    mBorderWidth = i
                    waveView.setBorder(mBorderWidth, mBorderColor)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        CompoundButtonCompat.setButtonTintList(
            (findViewById<RadioButton>(R.id.colorDefault)),
            resources.getColorStateList(R.color.white)
        )
        CompoundButtonCompat.setButtonTintList(
            (findViewById<RadioButton>(R.id.colorRed)),
            resources.getColorStateList(R.color.red)
        )
        CompoundButtonCompat.setButtonTintList(
            (findViewById<RadioButton>(R.id.colorGreen)),
            resources.getColorStateList(R.color.green)
        )
        CompoundButtonCompat.setButtonTintList(
            (findViewById<RadioButton>(R.id.colorBlue)),
            resources.getColorStateList(R.color.blue)
        )

        val gyroscopeSensor =
            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        var gyroscopeSensorListener = object : SensorEventListener {
            override fun onSensorChanged(sensorEvent: SensorEvent) {
                if (sensorEvent.values[2] > 0.5f) { // anticlockwise
                    waveView.waveShiftRatio = 1F
                } else if (sensorEvent.values[2] < -0.5f) {
                    waveView.waveShiftRatio = 0.5F
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
        }

        sensorManager.registerListener(
            gyroscopeSensorListener,
            gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL
        )

        (findViewById<RadioGroup>(R.id.colorChoice))
            .setOnCheckedChangeListener { radioGroup, i ->
                when (i) {
                    R.id.colorRed -> {
                        waveView.setWaveColor(
                            Color.parseColor("#28f16d7a"),
                            Color.parseColor("#3cf16d7a")
                        )
                        mBorderColor = Color.parseColor("#44f16d7a")
                        waveView.setBorder(mBorderWidth, mBorderColor)
                    }
                    R.id.colorGreen -> {
                        waveView.setWaveColor(
                            Color.parseColor("#40b7d28d"),
                            Color.parseColor("#80b7d28d")
                        )
                        mBorderColor = Color.parseColor("#B0b7d28d")
                        waveView.setBorder(mBorderWidth, mBorderColor)
                    }
                    R.id.colorBlue -> {
                        waveView.setWaveColor(
                            Color.parseColor("#88b8f1ed"),
                            Color.parseColor("#b8f1ed")
                        )
                        mBorderColor = Color.parseColor("#b8f1ed")
                        waveView.setBorder(mBorderWidth, mBorderColor)
                    }
                    else -> {
                        waveView.setWaveColor(
                            WaveView.DEFAULT_BEHIND_WAVE_COLOR,
                            WaveView.DEFAULT_FRONT_WAVE_COLOR
                        )
                        mBorderColor = Color.parseColor("#44FFFFFF")
                        waveView.setBorder(mBorderWidth, mBorderColor)
                    }
                }
            }
    }

    override fun onPause() {
        super.onPause()
        mWaveHelper?.cancel()
    }

    override fun onResume() {
        super.onResume()
        mWaveHelper?.start()
    }
}