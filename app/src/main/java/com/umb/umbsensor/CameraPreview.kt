package com.umb.umbsensor

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import java.io.IOException


class CameraPreview(context: Context?, var mSurfaceView: SurfaceView) :
    ViewGroup(context), SurfaceHolder.Callback {
    private val TAG = "Preview"
    var mHolder: SurfaceHolder
    var mPreviewSize: Camera.Size? = null
    var mSupportedPreviewSizes: List<Camera.Size>? = null
    lateinit var mCamera: Camera
    fun setCamera(camera: Camera) {
        mCamera = camera
        mSupportedPreviewSizes = mCamera!!.parameters.supportedPreviewSizes
        requestLayout()
        camera.setDisplayOrientation(90);


        // get Camera parameters
        val params = mCamera!!.parameters
        val focusModes = params.supportedFocusModes
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            // set the focus mode
            params.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
            // set Camera parameters
            mCamera!!.parameters = params
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        val width = resolveSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = resolveSize(suggestedMinimumHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (changed && childCount > 0) {
            val child = getChildAt(0)
            val width = r - l
            val height = b - t
            var previewWidth = width
            var previewHeight = height


            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                val scaledChildWidth = previewWidth * height / previewHeight
                child.layout(
                    (width - scaledChildWidth) / 2, 0,
                    (width + scaledChildWidth) / 2, height
                )
            } else {
                val scaledChildHeight = previewHeight * width / previewWidth
                child.layout(
                    0, (height - scaledChildHeight) / 2,
                    width, (height + scaledChildHeight) / 2
                )
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        try {
            if (mCamera != null) {
                mCamera!!.setPreviewDisplay(holder)
            }
        } catch (exception: IOException) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            mCamera!!.stopPreview()
        }
    }

    private fun getOptimalPreviewSize(sizes: List<Camera.Size>?, w: Int, h: Int): Camera.Size? {
        val ASPECT_TOLERANCE = 0.2
        val targetRatio = w.toDouble() / h
        if (sizes == null) return null
        var optimalSize: Camera.Size? = null
        var minDiff = Double.MAX_VALUE
        for (size in sizes) {
            val ratio = size.width / size.height
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size
                minDiff = Math.abs(size.height - h).toDouble()
            }
        }
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE
            for (size in sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size
                    minDiff = Math.abs(size.height - h).toDouble()
                }
            }
        }
        return optimalSize
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        val parameters = mCamera.parameters
        parameters.setPreviewSize(mPreviewSize!!.width, mPreviewSize!!.height)
        requestLayout()
        mCamera.startPreview()
    }

    init {
        //        addView(mSurfaceView);
        mHolder = mSurfaceView.holder
        mHolder.addCallback(this)
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }
}