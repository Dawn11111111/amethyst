package com.vitorpamplona.amethyst

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import com.vitorpamplona.amethyst.service.HttpClient

@UnstableApi object VideoCache {

    var exoPlayerCacheSize: Long = 90 * 1024 * 1024 // 90MB

    var leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(exoPlayerCacheSize)

    lateinit var exoDatabaseProvider: StandaloneDatabaseProvider
    lateinit var simpleCache: SimpleCache

    lateinit var cacheDataSourceFactory: CacheDataSource.Factory

    @Synchronized
    fun init(context: Context) {
        if (!this::simpleCache.isInitialized) {
            exoDatabaseProvider = StandaloneDatabaseProvider(context)

            simpleCache = SimpleCache(
                context.cacheDir,
                leastRecentlyUsedCacheEvictor,
                exoDatabaseProvider
            )

            cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(simpleCache)
                .setUpstreamDataSourceFactory(
                    OkHttpDataSource.Factory(HttpClient.getHttpClient())
                )
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        } else {
            cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(simpleCache)
                .setUpstreamDataSourceFactory(
                    OkHttpDataSource.Factory(HttpClient.getHttpClient())
                )
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        }
    }

    fun get(): CacheDataSource.Factory {
        return cacheDataSourceFactory
    }
}
