package com.stopwatch.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.stopwatch.app.util.CoilConfig

/**
 * Application class for global initialization
 */
class WorkoutTimerApp : Application(), ImageLoaderFactory {

    /**
     * Create custom ImageLoader with optimized caching
     * This eliminates image flashing across the entire app
     */
    override fun newImageLoader(): ImageLoader {
        return CoilConfig.createImageLoader(this)
    }
}
