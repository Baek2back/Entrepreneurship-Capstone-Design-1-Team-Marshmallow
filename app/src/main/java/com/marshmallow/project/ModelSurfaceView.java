package com.marshmallow.project;

import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

public class ModelSurfaceView extends GLSurfaceView {

    private NavigationActivity parent;
    private ModelRenderer mRenderer;

    public ModelSurfaceView(NavigationActivity parent) {
        super(parent);

        // parent component
        this.parent = parent;

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);
        setZOrderOnTop(true);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mRenderer = new ModelRenderer(this);
        setRenderer(mRenderer);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }

    public NavigationActivity getNavigationActivity() {
        return parent;
    }

    public ModelRenderer getModelRenderer() { return mRenderer; }
}


