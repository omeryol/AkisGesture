package com.openswipe

import android.app.Application
import com.openswipe.gesture.GestureConfig

class OpenSwipeApp : Application() {

    val gestureConfig: GestureConfig = GestureConfig()

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        private lateinit var instance: OpenSwipeApp
        fun getInstance(): OpenSwipeApp = instance
    }
}
