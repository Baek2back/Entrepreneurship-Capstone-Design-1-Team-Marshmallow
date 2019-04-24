package com.capstone.seoae;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.capstone.seoae.model.Object3DData;
import com.capstone.seoae.util.*;
import com.capstone.seoae.manager.*;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private AssetManager assetManager;
    private FrameLayout frameMap;
    @Nullable private ModelSurfaceView gLView;
    private SceneLoader scene;

    private GpsManager gpsManager;
    private PathManager pathManager;
    private DirectionManager directionManager;
    private SensorManager sensorManager;
    private Sensor accSensor,magSensor;

    private TMapView tMapView;
    private TMapPoint destination;
    private boolean isNavigationMode = false;
    private ArrayList<String> markerId = new ArrayList<>();
    private Timer timer;
    private TimerTask timerTask;

    @Nullable
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /* assets 폴더에 저장되어 있는 모델 및 텍스쳐 Load를 위해 AssetManager 사용.
           사용하기 희망하는 파일 명을 HashMap에 등록하여 전달하게 된다. */
        assetManager = getAssets();
        try {
            ContentsUtils.providedModel.put("cowboy.dae",assetManager);
            ContentsUtils.providedModel.put("cowboy.png",assetManager);
        } catch (Exception e) {
            e.printStackTrace();
        }

        frameMap = findViewById(R.id.frameMap);
        frameMap.bringToFront();
        setTMapView();

        findViewById(R.id.moveCurrent).setOnClickListener((View v) -> moveToCurrentLocation());
        findViewById(R.id.requestRoute).setOnClickListener((View v) -> startNavigationMode());

    }

    @Override
    protected void onStart() {
        super.onStart();
        scene = new SceneLoader(this);
        scene.init();
        gLView = new ModelSurfaceView(this);
        addContentView(gLView,new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(gLView != null) {
            gLView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(gLView != null) {
            gLView.onResume();
        }
    }

    private void initSensor() {
        try {
            // initialize GPSManager
            GpsManager.init(this);
            gpsManager = GpsManager.getInstance();
            gpsManager.setOnLocationListener(locationListener);

            // initialize PathManager
            pathManager = PathManager.getInstance();

            // initialize DirectionManager
            directionManager = DirectionManager.getInstance();

            // initialize SensorManager
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

            sensorManager.registerListener(directionManager.sensorEventListener, accSensor, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(directionManager.sensorEventListener, magSensor, SensorManager.SENSOR_DELAY_UI);

            moveToCurrentLocation();
        }catch (Exception e) {}
    }

    private void setTMapView() {
        tMapView = new TMapView(this);
        tMapView.setSKTMapApiKey("60b0ad2d-ac33-4752-8ce0-0eab498ba23b");
        tMapView.setIconVisibility(true);
        tMapView.setZoomLevel(14);
        tMapView.setCompassMode(false);
        tMapView.setTrackingMode(true);
        // TODO : 현재는 대청마루를 목적지로 설정해 두었음. 추후에 경유지 혹은 목적지 추가 해야함.
        TMapPoint daecheong = new TMapPoint(37.561278, 126.997088);
        frameMap.addView(tMapView);
        setDestination(daecheong);
        initSensor();
    }

    private void startNavigationMode() {
        setNavigationMode(!isNavigationMode);
    }

    public LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            try {
                float distanceFromPrev = location.distanceTo(gpsManager.getLastLocation());
                if ((distanceFromPrev < 20.0f) || (distanceFromPrev > 100.0f)) {
                    gpsManager.setLastLocation(location);
                    tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
                    if (isNavigationMode) {
                        moveToCurrentLocation();
                        updateDirection();
                    }
                }
            } catch (Exception ex) {}
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {}

        @Override
        public void onProviderEnabled(String s) {}

        @Override
        public void onProviderDisabled(String s) {}
    };

    @SuppressLint("ClickableViewAccessibility")
    private void setNavigationMode(boolean isNavigationMode) {
        this.isNavigationMode = isNavigationMode;
        if (this.isNavigationMode)
        // Navigation Mode 일때의 동작.
        {
            try {
                findViewById(R.id.indicateDirection).setVisibility(View.VISIBLE);
                tMapView.setZoomLevel(18);
                TMapData tMapData = new TMapData();
                Location currentLocation = gpsManager.getCurrentLocation();
                final TMapPoint startPoint = new TMapPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                tMapData.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, startPoint, destination, new TMapData.FindPathDataListenerCallback() {
                    @Override
                    public void onFindPathData(TMapPolyLine tMapPolyLine) {
                        // tMapView에 현재 위치에서 목적지까지의 경로를 그려준다.
                        tMapView.addTMapPath(tMapPolyLine);
                        // Line Point ArrayList
                        ArrayList<TMapPoint> linePoints = tMapPolyLine.getLinePoint();
                        markerId.clear();

                        int i = 0;

                        tMapView.removeAllMarkerItem();

                        // Add Markers on TMapView
                        for (TMapPoint p : linePoints) {
                            TMapMarkerItem markerItem = new TMapMarkerItem();
                            markerItem.setTMapPoint(p);

                            String id = "l" + (i++);
                            markerItem.setID(id);
                            tMapView.addMarkerItem(id, markerItem);
                            markerId.add(id);
                        }
                        pathManager.setPolyLine(tMapPolyLine);
                    }
                });

                // disable User Scroll & Zoom
                tMapView.setUserScrollZoomEnable(true);

                // enable compass mode
                tMapView.setCompassMode(true);
                // always compass mode
                tMapView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        tMapView.setCompassMode(true);
                        return true;
                    }
                });

                moveToCurrentLocation();

                timer = new Timer(true);
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        updateDirection();
                    }
                };
                timer.schedule(timerTask, 1000, 1);
                //startGl();
            } catch (Exception ex) {
                Log.d("Exception:", ex.getMessage());
                setNavigationMode(false);
            }
        } else {
            // Navigation 모드가 아니거나 예외 발생 시 동작.
            tMapView.setUserScrollZoomEnable(true);
            tMapView.setCompassMode(false);

            // 위에서 항상 Compass Mode로 설정되도록 해제해 두었으므로 리스너를 해제한다.
            tMapView.setOnTouchListener(null);

            // 설정된 모든 경로와 마커를 지운다.
            tMapView.removeTMapPath();
            tMapView.removeAllMarkerItem();

            // 앞서 timer를 구동시켜 두었으므로 취소해준다.
            try {
                timer.cancel();
            } catch (Exception ex) {}
        }
    }

    private void setDestination(TMapPoint destination) {
        try {
            tMapView.removeMarkerItem("Destination");
        } catch (Exception ex) {}

        this.destination = destination;

        // 지도에 띄울 마커 생성 이후 지도에 출력 해준다.
        TMapMarkerItem marker = new TMapMarkerItem();
        marker.setID("Destination");
        marker.setTMapPoint(this.destination);
        tMapView.addMarkerItem("Destination", marker);
    }

    private void updateDirection() {
        // 마커와 현재 위치 사이의 거리가 10 m 이내가 되면 마커 위치에 도착했다고 간주한다.
        float distThreshold = 10.0f;
        TMapPoint nearestPoint = null;

        try {
            nearestPoint = pathManager.getNearestPoint();
            Location currentLocation = gpsManager.getCurrentLocation();
            Location nearestLocation = new Location("");
            nearestLocation.setLongitude(nearestPoint.getLongitude());
            nearestLocation.setLatitude(nearestPoint.getLatitude());

            double distance = directionManager.getDistanceBetween(currentLocation, nearestLocation);

            if (distance < distThreshold) {
                int nearestIndex = pathManager.getNearestIndex();

                // 가장 인접한 위치의 마커와 그 ID를 제거한다.
                String targetMarkerId = markerId.get(nearestIndex);
                tMapView.removeMarkerItem(targetMarkerId);
                markerId.remove(nearestIndex);

                if (pathManager.hasNext()) {
                    // 진행할 마커가 더 남아 있을 때는 가장 인접한 마커의 위치를 갱신한다.
                    nearestPoint = pathManager.getNearestPoint();
                } else {
                    // 모든 마커를 제거한 경우이므로 목적지에 도착한 케이스 이다.
                    nearestPoint = null;
                    setNavigationMode(!isNavigationMode);
                    return;
                }
            }

            nearestLocation.setLongitude(nearestPoint.getLongitude());
            nearestLocation.setLatitude(nearestPoint.getLatitude());
            float direction = Math.round(directionManager.getDirectionBetween(currentLocation, nearestLocation));
            scene.getObjects().get(0).setRotationZ(-direction+180f);
            findViewById(R.id.indicateDirection).setRotation(direction);
            findViewById(R.id.indicateDirection).bringToFront();
        } catch (Exception ex) {}
    }
    private void moveToCurrentLocation() {
        try {
            Location currentLocation = gpsManager.getCurrentLocation();
            tMapView.setLocationPoint(currentLocation.getLongitude(), currentLocation.getLatitude());
            tMapView.setCenterPoint(currentLocation.getLongitude(), currentLocation.getLatitude());
        } catch (Exception ex) {
            Toast.makeText(this, "Can't not find current location.", Toast.LENGTH_SHORT).show();
        }
    }

    public SceneLoader getScene() {
        return scene;
    }

    public ModelSurfaceView getGLView() {
        return gLView;
    }
}
