package com.umb.umbsensor

import android.hardware.Camera

/** A safe way to get an instance of the Camera object.  */
fun getCameraInstance(): Camera? {
    var c: Camera? = null
    try {
        c = Camera.open() // attempt to get a Camera instance
    } catch (e: Exception) {
        // Camera is not available (in use or does not exist)
    }
    return c // returns null if camera is unavailable
}