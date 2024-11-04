package com.example.videoapp;

import android.content.Context;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import java.io.File;

public class SimpleCacheSingleton {
    private static SimpleCache simpleCache;

    public static synchronized SimpleCache getInstance(Context context) {
        if (simpleCache == null) {
            File cacheDir = new File(context.getCacheDir(), "videoCache");
            simpleCache = new SimpleCache(cacheDir, new LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024)); // 100MB cache
        }
        return simpleCache;
    }
}
