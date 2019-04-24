package com.capstone.seoae;

import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.capstone.seoae.animation.Animator;
import com.capstone.seoae.model.Object3DData;
import com.capstone.seoae.model.SceneCamera;
import com.capstone.seoae.service.LoaderTask;
import com.capstone.seoae.service.Object3DBuilder;
import com.capstone.seoae.service.collada.ColladaLoaderTask;
import com.capstone.seoae.util.ContentsUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SceneLoader implements LoaderTask.Callback {
    /**
     * Parent component
     */
    protected final MainActivity parent;
    /**
     * List of data objects containing info for building the opengl objects
     */
    private List<Object3DData> objects = new ArrayList<Object3DData>();
    /**
     * Point of view SceneCamera
     */
    private SceneCamera sceneCamera;
    /**
     * Whether to draw using points
     */
    private boolean drawingPoints = false;
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
    private boolean drawLighting = false;
    /**
     * Animate model (dae only) or not
     */
    private boolean animateModel = true;
    /**
     * Draw skeleton or not
     */
    private boolean drawSkeleton = false;
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
     * time when model loading has started (for stats)
     */
    private long startTime;

    public SceneLoader(MainActivity main) {
        this.parent = main;
    }

    public void init() {

        // SceneCamera to show a point of view
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

        // smooth SceneCamera transition
        sceneCamera.animate();
        // initial SceneCamera animation.
        // TODO : 카메라가 계속 일정 방향으로 움직이게 하는 부분 모델 회전부분만 참고하고 지워도 될것 같다.
        //animateCamera(0f,0f);

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

    protected void animateCamera(float dx, float dy){
        //sceneCamera.RotateCamera(10f,2,0,0);
        sceneCamera.translateCamera(dx, dy);
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

    public boolean isDrawPoints() {
        return this.drawingPoints;
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

    @Override
    public void onStart(){
        ContentsUtils.setThreadActivity(parent);
    }

    @Override
    public void onLoadComplete(List<Object3DData> datas) {
        // Load Texture Part.
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
    }

    @Override
    public void onLoadError(Exception ex) {
        Log.e("SceneLoader", ex.getMessage(), ex);
        makeToastText("There was a problem building the model: " + ex.getMessage(), Toast.LENGTH_LONG);
    }
}
