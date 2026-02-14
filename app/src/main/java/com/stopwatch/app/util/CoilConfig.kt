package com.stopwatch.app.util

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy

/**
 * Global Coil configuration for optimized image loading
 * Eliminates flashing by aggressive caching and preloading
 */
object CoilConfig {

    fun createImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            // Memory cache: Keep 25% of app's memory for images
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .strongReferencesEnabled(true)
                    .build()
            }
            // Disk cache: 100MB for persistent image caching
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024) // 100MB
                    .build()
            }
            // Always use caching
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            // Crossfade for smooth transitions
            .crossfade(300)
            // Respect cache headers
            .respectCacheHeaders(false) // Assets don't have cache headers
            .build()
    }
}
