package com.capstone.seoae;

import android.content.res.AssetManager;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.capstone.seoae.animation.Animator;
import com.capstone.seoae.collision.CollisionDetection;
import com.capstone.seoae.model.Object3DData;
import com.capstone.seoae.model.SceneCamera;
import com.capstone.seoae.service.LoaderTask;
import com.capstone.seoae.service.Object3DBuilder;
import com.capstone.seoae.service.collada.ColladaLoaderTask;
import com.capstone.seoae.util.ContentsUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SceneLoader implements LoaderTask.Callback {
    /**
     * Default model color: yellow
     */
    private static float[] DEFAULT_COLOR = {1.0f, 1.0f, 0, 1.0f};
    /**
     * Parent component
     */
    protected final MainActivity parent;
    /**
     * List of data objects containing info for building the opengl objects
     */
    private List<Object3DData> objects = new ArrayList<Object3DData>();
    /**
     * Point of view camera2
     */
    private SceneCamera sceneCamera;
    /**
     * Whether to draw objects as wireframes
     */
    private boolean drawWireframe = false;
    /**
     * Whether to draw using points
     */
    private boolean drawingPoints = false;
    /**
     * Whether to draw bounding boxes around objects
     */
    private boolean drawBoundingBox = false;
    /**
     * Whether to draw face normals. Normally used to debug models
     */
    private boolean drawNormals = false;
    /**
     * Whether to draw using textures
     */
    private boolean drawTextures = true;
    /**
     * Light toggle feature: we have 3 states: no light, light, light + rotation
     */
    private boolean rotatingLight = true;
    /**
     * Light toggle feature: whether to draw using lights
     */
    private boolean drawLighting = true;
    /**
     * Animate model (dae only) or not
     */
    private boolean animateModel = true;
    /**
     * Draw skeleton or not
     */
    private boolean drawSkeleton = false;
    /**
     * Toggle collision detection
     */
    private boolean isCollision = false;
    /**
     * Toggle 3d anaglyph
     */
    private boolean isAnaglyph = false;
    /**
     * Object selected by the user
     */
    private Object3DData selectedObject = null;
    /**
     * Initial light position
     */
    private final float[] lightPosition = new float[]{0, 0, 6, 1};
    /**
     * Light bulb 3d data
     */
    private final Object3DData lightPoint = Object3DBuilder.buildPoint(lightPosition).setId("light");
    /**
     * Animator
     */
    private Animator animator = new Animator();
    /**
     * Did the user touched the model for the first time?
     */
    private boolean userHasInteracted;
    /**
     * time when model loading has started (for stats)
     */
    private long startTime;

    public SceneLoader(MainActivity main) {
        this.parent = main;
    }

    public void init() {

        // Camera2 to show a point of view
        sceneCamera = new SceneCamera();


        startTime = SystemClock.uptimeMillis();
        try {
            new ColladaLoaderTask(parent, ContentsUtils.providedModel.get("cowboy.dae"), this).execute();
        }catch (Exception e) {}
    }

    public SceneCamera getSceneCamera() {
        return sceneCamera;
    }

    private void makeToastText(final String text, final int toastDuration) {
        parent.runOnUiThread(() -> Toast.makeText(parent.getApplicationContext(), text, toastDuration).show());
    }

    public Object3DData getLightBulb() {
        return lightPoint;
    }

    public float[] getLightPosition() {
        return lightPosition;
    }

    /**
     * Hook for animating the objects before the rendering
     */
    public void onDrawFrame() {

        animateLight();

        // smooth camera2 transition
        sceneCamera.animate();

        // initial camera2 animation. animate if user didn't touch the screen
        if (!userHasInteracted) {
            animateCamera();
        }

        if (objects.isEmpty()) return;

        if (animateModel) {
            for (int i=0; i<objects.size(); i++) {
                Object3DData obj = objects.get(i);
                animator.update(obj);
            }
        }
    }

    private void animateLight() {
        if (!rotatingLight) return;

        // animate light - Do a complete rotation every 5 seconds.
        long time = SystemClock.uptimeMillis() % 5000L;
        float angleInDegrees = (360.0f / 5000.0f) * ((int) time);
        lightPoint.setRotationY(angleInDegrees);
    }

    private void animateCamera(){
        sceneCamera.translateCamera(0f, 0f);
    }

    synchronized void addObject(Object3DData obj) {
        List<Object3DData> newList = new ArrayList<Object3DData>(objects);
        newList.add(obj);
        this.objects = newList;
        requestRender();
    }

    private void requestRender() {
        // request render only if GL view is already initialized
        if (parent.getGLView() != null) {
            parent.getGLView().requestRender();
        }
    }

    public synchronized List<Object3DData> getObjects() {
        return objects;
    }

    public boolean isDrawWireframe() {
        return this.drawWireframe;
    }

    public boolean isDrawPoints() {
        return this.drawingPoints;
    }

    public void toggleBoundingBox() {
        this.drawBoundingBox = !drawBoundingBox;
        requestRender();
    }

    public boolean isDrawBoundingBox() {
        return drawBoundingBox;
    }

    public boolean isDrawNormals() {
        return drawNormals;
    }


    public boolean isDrawAnimation() {
        return animateModel;
    }

    public boolean isDrawTextures() {
        return drawTextures;
    }

    public boolean isDrawLighting() {
        return drawLighting;
    }

    public boolean isDrawSkeleton() {
        return drawSkeleton;
    }

    public boolean isCollision() {
        return isCollision;
    }

    public boolean isAnaglyph() {
        return isAnaglyph;
    }

    @Override
    public void onStart(){
        ContentsUtils.setThreadActivity(parent);
    }

    @Override
    public void onLoadComplete(List<Object3DData> datas) {
        // TODO: move texture load to LoaderTask
        for (Object3DData data : datas) {
            if (data.getTextureData() == null && data.getTextureFile() != null) {
                Log.i("LoaderTask","Loading texture... "+data.getTextureFile());
                try (InputStream stream = ContentsUtils.getInputStream(data.getTextureFile())){
                    if (stream != null) {
                        data.setTextureData(ContentsUtils.read(stream));
                    }
                } catch (IOException ex) {
                    data.addError("Problem loading texture " + data.getTextureFile());
                }
            }
        }
        // TODO: move error alert to LoaderTask
        List<String> allErrors = new ArrayList<>();
        for (Object3DData data : datas) {
            addObject(data);
            allErrors.addAll(data.getErrors());
        }
        if (!allErrors.isEmpty()){
            makeToastText(allErrors.toString(), Toast.LENGTH_LONG);
        }
        final String elapsed = (SystemClock.uptimeMillis() - startTime) / 1000 + " secs";
        makeToastText("Build complete (" + elapsed + ")", Toast.LENGTH_LONG);
        //ContentsUtils.setThreadActivity(null);
    }

    @Override
    public void onLoadError(Exception ex) {
        Log.e("SceneLoader", ex.getMessage(), ex);
        makeToastText("There was a problem building the model: " + ex.getMessage(), Toast.LENGTH_LONG);
        //ContentsUtils.setThreadActivity(null);
    }

    public Object3DData getSelectedObject() {
        return selectedObject;
    }

    private void setSelectedObject(Object3DData selectedObject) {
        this.selectedObject = selectedObject;
    }

    public void loadTexture(Object3DData obj, AssetManager.AssetInputStream inputStream) throws IOException {
        if (obj == null && objects.size() != 1) {
            makeToastText("Unavailable", Toast.LENGTH_SHORT);
            return;
        }
        obj = obj != null ? obj : objects.get(0);
        obj.setTextureData(ContentsUtils.read(inputStream));
        this.drawTextures = true;
    }

    public void processTouch(float x, float y) {
        ModelRenderer mr = parent.getGLView().getModelRenderer();
        Object3DData objectToSelect = CollisionDetection.getBoxIntersection(getObjects(), mr.getWidth(), mr.getHeight
                (), mr.getModelViewMatrix(), mr.getModelProjectionMatrix(), x, y);
        if (objectToSelect != null) {
            if (getSelectedObject() == objectToSelect) {
                Log.i("SceneLoader", "Unselected object " + objectToSelect.getId());
                setSelectedObject(null);
            } else {
                Log.i("SceneLoader", "Selected object " + objectToSelect.getId());
                setSelectedObject(objectToSelect);
            }
            if (isCollision()) {
                Log.d("SceneLoader", "Detecting collision...");

                float[] point = CollisionDetection.getTriangleIntersection(getObjects(), mr.getWidth(), mr.getHeight
                        (), mr.getModelViewMatrix(), mr.getModelProjectionMatrix(), x, y);
                if (point != null) {
                    Log.i("SceneLoader", "Drawing intersection point: " + Arrays.toString(point));
                    addObject(Object3DBuilder.buildPoint(point).setColor(new float[]{1.0f, 0f, 0f, 1f}));
                }
            }
        }
    }

    public void processMove(float dx1, float dy1) {
        userHasInteracted = true;
    }
}
