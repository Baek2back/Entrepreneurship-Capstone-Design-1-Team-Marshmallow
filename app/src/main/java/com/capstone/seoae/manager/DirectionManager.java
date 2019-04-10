package com.capstone.seoae.manager;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;

public class DirectionManager {
    private static DirectionManager instance = new DirectionManager();
    public static DirectionManager getInstance() { return instance; }

    private float[] mGravity = null;
    private float[] mGeomagnetic = null;

    private float mPitch = 0.0f;
    private float mRoll = 0.0f;
    private float mAzimut = 0.0f;

    private float[] rotation = new float[9];
    private float[] resultData = new float[3];

    public SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values.clone();
            switch(event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    instance.mGravity = values;
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    instance.mGeomagnetic = values;
                    break;
            }
            if (mGravity != null && mGeomagnetic != null) {
                // 가속 데이터와 자기장 데이터로 회전 매트릭스를 얻는다.
                SensorManager.getRotationMatrix(rotation, null, mGravity, mGeomagnetic);
                // 회전 매트릭스를 이용하여 방향 데이터를 얻는다.
                SensorManager.getOrientation(rotation, resultData);

                mAzimut = ((float)Math.toDegrees(resultData[0])+360) % 360;
                mPitch = resultData[1];
                mRoll = resultData[2];
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    public float getDirectionBetween(Location src, Location dest) {
        float bearing, relative = 0.0f;
        try {
            bearing = src.bearingTo(dest);
            relative = (bearing - mAzimut) % 360;

        } catch (Exception ex) {}
        return relative;
    }
    public float getDistanceBetween(Location src, Location dest) {
        return src.distanceTo(dest);
    }
}
