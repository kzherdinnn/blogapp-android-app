package com.pam.blogapp;

import android.app.Application;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            // 1. Setup OkHttpClient dengan cache yang besar (100MB)
            File cacheDir = new File(getCacheDir(), "picasso-cache");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            
            Cache cache = new Cache(cacheDir, 100 * 1024 * 1024); // 100MB
            
            OkHttpClient client = new OkHttpClient.Builder()
                    .cache(cache)
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            // 2. Inisialisasi Picasso singleton dengan downloader custom
            Picasso picasso = new Picasso.Builder(this)
                    .downloader(new OkHttp3Downloader(client))
                    .loggingEnabled(true) 
                    .listener((picasso1, uri, exception) -> {
                        android.util.Log.e("DEBUG_PICASSO", "FAILED URI: " + uri);
                        android.util.Log.e("DEBUG_PICASSO", "REASON: " + exception.getMessage());
                    })
                    .build();

            Picasso.setSingletonInstance(picasso);
        } catch (Exception e) {
            // Jika singleton sudah di-set (biasanya saat terjebak di memory), abaikan
            e.printStackTrace();
        }
    }
}
