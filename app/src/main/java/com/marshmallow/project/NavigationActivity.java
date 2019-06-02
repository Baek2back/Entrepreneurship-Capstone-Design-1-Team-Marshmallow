package com.marshmallow.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.BannerInstructions;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback;
import com.mapbox.services.android.navigation.ui.v5.camera.NavigationCamera;
import com.mapbox.services.android.navigation.ui.v5.listeners.BannerInstructionsListener;
import com.mapbox.services.android.navigation.ui.v5.listeners.InstructionListListener;
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener;
import com.mapbox.services.android.navigation.ui.v5.listeners.SpeechAnnouncementListener;
import com.mapbox.services.android.navigation.ui.v5.map.NavigationMapboxMap;
import com.mapbox.services.android.navigation.ui.v5.voice.SpeechAnnouncement;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;

import com.mapbox.services.android.navigation.v5.utils.RouteUtils;
import com.marshmallow.project.manager.DirectionManager;
import com.marshmallow.project.util.*;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;

public class NavigationActivity extends AppCompatActivity
        implements OnNavigationReadyCallback, NavigationListener, ProgressChangeListener,
        InstructionListListener, SpeechAnnouncementListener, BannerInstructionsListener {
    private Point ORIGIN;
    private Point DESTINATION = Point.fromLngLat(127.024890,37.603401);
    private static final int INITIAL_ZOOM = 16;

    private NavigationMapboxMap mapboxMap;
    private NavigationView navigationView;
    private View spacer;
    private TextView speedWidget;
    private FloatingActionButton fabNightModeToggle;

    private boolean bottomSheetVisible = true;
    private boolean instructionListShown = false;

    private AssetManager assetManager;
    @Nullable private ModelSurfaceView gLView;
    private SceneLoader scene;

    private DirectionManager directionManager;
    private SensorManager sensorManager;
    private Sensor accSensor,magSensor;

    private float bearing;
    private String destination;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        /* assets 폴더에 저장되어 있는 모델 및 텍스쳐 Load를 위해 AssetManager 사용.
           사용하기 희망하는 파일 명을 HashMap에 등록하여 전달하게 된다. */
        assetManager = getAssets();
        try {
            ContentsUtils.providedModel.put("cowboy.dae",assetManager);
            ContentsUtils.providedModel.put("cowboy.png",assetManager);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = getIntent();
        Log.d("test",Integer.toString(intent.getIntExtra("type",1)));
        ORIGIN =  (Point)intent.getSerializableExtra("currentPoint");
        DESTINATION = Point.fromLngLat(126.993550, 37.559998);
        //ORIGIN = Point.fromLngLat(126.993550,37.561008);
        /*destination = intent.getStringExtra("place");
        switch(destination){
            case "SEOAE-RO":
                Log.d("test","seoae");
                DESTINATION = Point.fromLngLat(126.993550, 37.559998);
                break;
            case "HOME OF YOO SEONG-RYONG":
                Log.d("test","yooseongryong");
                DESTINATION = Point.fromLngLat(126.993550, 37.559998);
                break;
            case "KOREA HOUSE":
                Log.d("test","koreahouse");
                DESTINATION = Point.fromLngLat(126.993550, 37.559998);
                break;
            case "NAMSANGOL VILLAGE":
                Log.d("test","namsanvillage");
                DESTINATION = Point.fromLngLat(126.993550, 37.559998);
                break;
            case "DAEHAN CINEMA":
                Log.d("test","daehancinema");
                DESTINATION = Point.fromLngLat(126.993550, 37.559998);
                break;
            case "OHZEMI FILM STUDIO":
                Log.d("test","ohzemi");
                DESTINATION = Point.fromLngLat(126.993550, 37.559998);
                break;
            case "THE WHITE PUB":
                Log.d("test","whitepub");
                DESTINATION = Point.fromLngLat(126.993550, 37.559998);
                break;
        }*/


        setTheme(R.style.Theme_AppCompat_Light_NoActionBar);
        initNightMode();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        navigationView = findViewById(R.id.navigationView);
        fabNightModeToggle = findViewById(R.id.fabToggleNightMode);
        speedWidget = findViewById(R.id.speed_limit);
        spacer = findViewById(R.id.spacer);
        setSpeedWidgetAnchor(R.id.summaryBottomSheet);

        CameraPosition initialPosition = new CameraPosition.Builder()
                .target(new LatLng(ORIGIN.latitude(), ORIGIN.longitude()))
                .zoom(INITIAL_ZOOM)
                .build();
        navigationView.onCreate(savedInstanceState);
        navigationView.initialize(this, initialPosition);

    }

    @Override
    public void onNavigationReady(boolean isRunning) {
        fetchRoute();
    }

    @Override
    public void onStart() {
        super.onStart();
        navigationView.onStart();
        scene = new SceneLoader(this);
        scene.init();
        gLView = new ModelSurfaceView(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        addContentView(gLView, new FrameLayout.LayoutParams(layoutParams));
        initSensor();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(gLView != null) gLView.onResume();
        navigationView.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        navigationView.onLowMemory();
    }

    @Override
    public void onBackPressed() {
// If the navigation view didn't need to do anything, call super
        if (!navigationView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        navigationView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        navigationView.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        navigationView.onPause();
        if(gLView != null) gLView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        navigationView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        navigationView.onDestroy();
        if (isFinishing()) {
            saveNightModeToPreferences(AppCompatDelegate.MODE_NIGHT_AUTO);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
        }
    }

    @Override
    public void onCancelNavigation() {
// Navigation canceled, finish the activity
        finish();
    }

    @Override
    public void onNavigationFinished() {
// Intentionally empty
    }

    @Override
    public void onNavigationRunning() {
// Intentionally empty
    }

    @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {
        setSpeed(location);
        setBearing(location);
        RouteUtils routeUtils = new RouteUtils();
        if(routeUtils.isArrivalEvent(routeProgress))
        {
            // 도착했을 때 발생하는 이벤트입니다.
            Toast.makeText(this, "You have arrived!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onInstructionListVisibilityChanged(boolean shown) {
        instructionListShown = shown;
        speedWidget.setVisibility(shown ? View.GONE : View.VISIBLE);
        if (instructionListShown) {
            fabNightModeToggle.hide();
        } else if (bottomSheetVisible) {
            fabNightModeToggle.show();
        }
    }

    @Override
    public SpeechAnnouncement willVoice(SpeechAnnouncement announcement) {
        return SpeechAnnouncement.builder().announcement(announcement.announcement()).build();
    }

    @Override
    public BannerInstructions willDisplay(BannerInstructions instructions) {
        return instructions;
    }

    private void initSensor() {
        try{
            // initialize DirectionManager
            directionManager = DirectionManager.getInstance();

            // initialize SensorManager
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

            sensorManager.registerListener(directionManager.sensorEventListener, accSensor, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(directionManager.sensorEventListener, magSensor, SensorManager.SENSOR_DELAY_UI);

        }catch (Exception e) {}
    }
    private void startNavigation(DirectionsRoute directionsRoute) {
        NavigationViewOptions.Builder options =
                NavigationViewOptions.builder()
                        //.shouldSimulateRoute(true)
                        .navigationListener(this)
                        .directionsRoute(directionsRoute)
                        .progressChangeListener(this)
                        .instructionListListener(this)
                        .speechAnnouncementListener(this)
                        .bannerInstructionsListener(this);
        setBottomSheetCallback(options);
        setupNightModeFab();
        navigationView.startNavigation(options.build());
    }

    private void fetchRoute() {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .profile(DirectionsCriteria.PROFILE_WALKING)
                .origin(ORIGIN)
                .destination(DESTINATION)
                .language(Locale.ENGLISH)
                .alternatives(false)
                //.alternatives(true) 바꾼 부분.
                .build()
                .getRoute(new SimplifiedCallback() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        DirectionsRoute directionsRoute = response.body().routes().get(0);
                        startNavigation(directionsRoute);
                    }
                });
        mapboxMap = navigationView.retrieveNavigationMapboxMap();
        try {
            mapboxMap.updateLocationLayerRenderMode(RenderMode.GPS);
            mapboxMap.updateCameraTrackingMode(NavigationCamera.NAVIGATION_TRACKING_MODE_GPS);
        }catch(Exception e) {}

    }

    /**
     * Sets the anchor of the spacer for the speed widget, thus setting the anchor for the speed widget
     * (The speed widget is anchored to the spacer, which is there because padding between items and
     * their anchors in CoordinatorLayouts is finicky.
     *
     * @param res resource for view of which to anchor the spacer
     */
    private void setSpeedWidgetAnchor(@IdRes int res) {
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) spacer.getLayoutParams();
        layoutParams.setAnchorId(res);
        spacer.setLayoutParams(layoutParams);
    }

    private void setBottomSheetCallback(NavigationViewOptions.Builder options) {
        options.bottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        bottomSheetVisible = false;
                        fabNightModeToggle.hide();
                        setSpeedWidgetAnchor(R.id.recenterBtn);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        bottomSheetVisible = true;
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        if (!bottomSheetVisible) {
// View needs to be anchored to the bottom sheet before it is finished expanding
// because of the animation
                            fabNightModeToggle.show();
                            setSpeedWidgetAnchor(R.id.summaryBottomSheet);
                        }
                        break;
                    default:
                        return;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
    }

    private void setupNightModeFab() {
        fabNightModeToggle.setOnClickListener(view -> toggleNightMode());
    }

    private void toggleNightMode() {
        int currentNightMode = getCurrentNightMode();
        alternateNightMode(currentNightMode);
    }

    private void initNightMode() {
        int nightMode = retrieveNightModeFromPreferences();
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }
    private int getCurrentNightMode() {
        return getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
    }

    private void alternateNightMode(int currentNightMode) {
        int newNightMode;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            newNightMode = AppCompatDelegate.MODE_NIGHT_NO;
        } else {
            newNightMode = AppCompatDelegate.MODE_NIGHT_YES;
        }
        saveNightModeToPreferences(newNightMode);
        recreate();
    }

    private int retrieveNightModeFromPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getInt(getString(R.string.current_night_mode), AppCompatDelegate.MODE_NIGHT_AUTO);
    }

    private void saveNightModeToPreferences(int nightMode) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(getString(R.string.current_night_mode), nightMode);
        editor.apply();
    }

    private void setSpeed(Location location) {
        String string = String.format("%d\nkph", (int) (location.getSpeed() * 3.6));
        int kphTextSize = getResources().getDimensionPixelSize(R.dimen.kph_text_size);
        int speedTextSize = getResources().getDimensionPixelSize(R.dimen.speed_text_size);

        SpannableString spannableString = new SpannableString(string);
        spannableString.setSpan(new AbsoluteSizeSpan(kphTextSize),
                string.length() - 4, string.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        spannableString.setSpan(new AbsoluteSizeSpan(speedTextSize),
                0, string.length() - 3, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        speedWidget.setText(spannableString);
        if (!instructionListShown) {
            speedWidget.setVisibility(View.VISIBLE);
        }
    }

    private void setBearing(Location location) {
        bearing = directionManager.getmAzimut();
        scene.getObjects().get(0).setRotationZ(-bearing+180f);
        Log.d("bearing", String.valueOf(bearing));
    }
    public SceneLoader getScene() {
        return scene;
    }

    public ModelSurfaceView getGLView() {
        return gLView;
    }
}
