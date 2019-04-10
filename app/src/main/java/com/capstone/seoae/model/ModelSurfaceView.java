package com.capstone.seoae.model;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.capstone.seoae.util.Util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ModelSurfaceView extends GLSurfaceView {

    @NonNull private ModelRenderer renderer;

    public ModelSurfaceView(@NonNull Context context, @Nullable Model model) {
        super(context);
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8,8,8,8,16,8);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        setZOrderOnTop(true);

        renderer = new ModelRenderer(model);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public ModelRenderer getRenderer() {return renderer;}
}
