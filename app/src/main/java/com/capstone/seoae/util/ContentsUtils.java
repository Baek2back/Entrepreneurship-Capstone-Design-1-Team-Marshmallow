package com.capstone.seoae.util;

import android.app.Activity;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ContentsUtils {
    public static Map<String, AssetManager> providedModel = new HashMap<>();

    private static ThreadLocal<Activity> currentActivity = new ThreadLocal<>();

    public static InputStream getInputStream(String path) {
        return getUri(path);
    }
    public static void setThreadActivity(Activity currentActivity) {
        Log.i("ContentsUtils", "Current activity thread: " + Thread.currentThread().getName());
        ContentsUtils.currentActivity.set(currentActivity);
    }
    private static Activity getCurrentActivity() {
        return ContentsUtils.currentActivity.get();
    }
    public static InputStream getUri(String name) {
        try{
            return providedModel.get(name).open(name);
        }catch (Exception e) {}
        return null;
    }
    public static byte[] read(InputStream is) throws IOException {
        byte[] isData = new byte[512];
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        while ((nRead = is.read(isData, 0, isData.length)) != -1) {
            buffer.write(isData, 0, nRead);
        }
        return buffer.toByteArray();
    }
}
