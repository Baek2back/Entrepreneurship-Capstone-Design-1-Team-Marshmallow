package com.capstone.seoae;

import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class ModelSurfaceView extends GLSurfaceView {

    private MainActivity parent;
    private ModelRenderer mRenderer;

    public ModelSurfaceView(MainActivity parent) {
        super(parent);

        // parent component
        this.parent = parent;

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);
        setZOrderOnTop(true);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mRenderer = new ModelRenderer(this);
        setRenderer(mRenderer);
        getHolder().setFormat(PixelFormat.RGBA_8888);


        // This is the actual renderer of the 3D space

        // Render the view only when there is a change in the drawing data
        // TODO: enable this?
        // setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }

    public MainActivity getMainActivity() {
        return parent;
    }

    public ModelRenderer getModelRenderer() { return mRenderer; }
}


