package com.capstone.seoae;

import android.app.Application;

import com.capstone.seoae.model.Model;

import androidx.annotation.Nullable;

public class SeoaeNaviApplication extends Application
{
    private static SeoaeNaviApplication INSTANCE;
    @Nullable private Model currentModel;

    public static SeoaeNaviApplication getInstance() {
        return INSTANCE;
    }

    @Nullable public Model getCurrentModel() {
        return currentModel;
    }

    public void setCurrentModel(@Nullable Model model) {
        currentModel = model;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        INSTANCE = this;
    }
}
