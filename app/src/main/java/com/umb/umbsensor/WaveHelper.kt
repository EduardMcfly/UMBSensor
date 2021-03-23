package com.umb.umbsensor

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import com.gelitenight.waveview.library.WaveView
import java.util.*
import kotlin.math.abs


class WaveHelper(private val mWaveView: WaveView, private val sensorManager: SensorManager) {
    private var mAnimatorSet: AnimatorSet? = null
    private var mGData = FloatArray(3)
    private var mMData = FloatArray(3)
    private val mR = FloatArray(16)
    private val mI = FloatArray(16)
    private val mOrientation = FloatArray(3)
    var rotationAnim: ObjectAnimator = ObjectAnimator.ofFloat(
        mWaveView, "rotation", 0f
    )
    val amplitudeAnim = ObjectAnimator.ofFloat(
        mWaveView, "amplitudeRatio", 0.0001f, 0.05f
    )

    var rotationAnimState = false

    fun start() {
        mWaveView.isShowWave = true
        if (mAnimatorSet != null) {
            mAnimatorSet!!.start()
        }
    }

    var gyroscopeSensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val type: Int = event.sensor.type
            val data = event.values
            when (type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    mGData = data
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    mMData = data
                }
                else -> {
                    return
                }
            }
            SensorManager.getRotationMatrix(mR, mI, mGData, mMData)

            SensorManager.getOrientation(mR, mOrientation)
            val incl = SensorManager.getInclination(mI)
            val rad2deg = (180.0f / Math.PI).toFloat()
            val yaw = mOrientation[0] * rad2deg
            val pitch = mOrientation[1] * rad2deg
            var roll = (mOrientation[2] * rad2deg) * -1

            if (roll > -90 && roll < 90 && !rotationAnimState) {
                rotationAnimState = true
                val duration: Long = 1000
                val current = rotationAnim.animatedValue as Float
                val diff = abs(abs(current.toInt()) - abs(roll))
                if (diff > 20 && !amplitudeAnim.isRunning) {
                    amplitudeAnim.repeatCount = ValueAnimator.INFINITE
                    amplitudeAnim.repeatMode = ValueAnimator.REVERSE
                    amplitudeAnim.duration = duration * (diff / 10).toInt()
                    amplitudeAnim.start()
                }
                rotationAnim = ObjectAnimator.ofFloat(
                    mWaveView, "rotation", current, roll
                )
                rotationAnim.duration = duration
                rotationAnim.doOnEnd {
                    rotationAnimState = false
                }
                rotationAnim.start()
            }
            Log.d(
                "Compass", "yaw: " + (yaw).toInt() +
                        "  pitch: " + (pitch).toInt() +
                        "  roll: " + (roll).toInt() +
                        "  incl: " + (incl * rad2deg).toInt()
            )
        }

        override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
    }

    fun onPause() {
        sensorManager.unregisterListener(gyroscopeSensorListener)
    }

    private fun initAnimation() {
        val animators: MutableList<Animator> = ArrayList()

        val gsensor: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val msensor: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager.registerListener(
            gyroscopeSensorListener,
            gsensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager.registerListener(
            gyroscopeSensorListener,
            msensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )


        // horizontal animation.
        // wave waves infinitely.
        val waveShiftAnim = ObjectAnimator.ofFloat(
            mWaveView, "waveShiftRatio", 0f, 1f
        )
        waveShiftAnim.repeatCount = ValueAnimator.INFINITE
        waveShiftAnim.duration = 1000
        waveShiftAnim.interpolator = LinearInterpolator()
        animators.add(waveShiftAnim)

        // vertical animation.
        // water level increases from 0 to center of WaveView
        val waterLevelAnim = ObjectAnimator.ofFloat(
            mWaveView, "waterLevelRatio", 0f, 0.5f
        )
        waterLevelAnim.duration = 10000
        waterLevelAnim.interpolator = DecelerateInterpolator()
        animators.add(waterLevelAnim)

        // amplitude animation.
        // wave grows big then grows small, repeatedly
        val amplitudeAnim = ObjectAnimator.ofFloat(
            mWaveView, "amplitudeRatio", 0.0001f, 0.05f
        )
        amplitudeAnim.repeatCount = 1
        amplitudeAnim.repeatMode = ValueAnimator.REVERSE
        amplitudeAnim.duration = 5000
        amplitudeAnim.interpolator = LinearInterpolator()
        animators.add(amplitudeAnim)
        mAnimatorSet = AnimatorSet()
        mAnimatorSet!!.playTogether(animators)
    }

    fun cancel() {
        if (mAnimatorSet != null) {
//            mAnimatorSet.cancel();
            mAnimatorSet!!.end()
        }
    }

    init {
        initAnimation()
    }
}