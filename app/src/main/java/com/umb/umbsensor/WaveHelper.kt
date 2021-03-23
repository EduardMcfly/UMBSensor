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

    fun onPause() {
        sensorManager.unregisterListener(gyroscopeSensorListener)
    }

    private fun initAnimation() {
        val animators: MutableList<Animator> = ArrayList()



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