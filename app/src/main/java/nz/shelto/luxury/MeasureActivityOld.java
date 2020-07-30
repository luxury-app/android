/*
package nz.shelto.luxury;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.SessionConfiguration;
import android.icu.util.Measure;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.UseCase;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import nz.shelto.luxury.fragments.HomeFragment;

public class MeasureActivityOld extends AppCompatActivity implements SensorEventListener {


    private SensorManager sensorManager;
    private Sensor lightSensor;

    private CameraDevice frontFacingCamera;
    private Handler cameraHandler;

    private SurfaceView surfaceView;

    private final List<Surface> cameraOutputs = new ArrayList<>();

    private CameraCaptureSession cameraCaptureSession;

    private Thread cameraThread = null;

    private final CameraCaptureSession.StateCallback cameraSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            setTextView("Camera session config!");
            cameraCaptureSession = session;
            cameraThread = new Thread(cameraCaptureRunnable);
            cameraThread.start();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            setTextView("Camera session failed config!");
        }
    };

    private List<Float> lightData = new ArrayList<>();
    private List<Double> cameraLightData = new ArrayList<>();
    private long startTime = -1, measureStartTime = -1;
    private boolean sendMeasureMessage = true;

    private final CameraDevice.StateCallback cameraCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            frontFacingCamera = camera;
            try {
                frontFacingCamera.createCaptureSession(MeasureActivityOld.this.cameraOutputs, MeasureActivityOld.this.cameraSessionCallback, MeasureActivityOld.this.cameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            frontFacingCamera.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            frontFacingCamera.close();
            frontFacingCamera = null;
            finish();
        }
    };

    private final Runnable cameraCaptureRunnable = new Runnable() {

        private boolean shouldRun = true;

        private int totalCaptures = 0;

        private final CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);

                final float fStop = result.get(CaptureResult.LENS_APERTURE);
                final long exposureTime = result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
                final double exposureTimeSec = exposureTime * Math.pow(10, -9);
                final double ev = Math.log(Math.pow(fStop, 2)/exposureTimeSec)/Math.log(2);
                final double cameraLux = 2.5 * Math.pow(2, ev);
                cameraLightData.add(cameraLux);
                setTextView(String.format("Capture %d, light: %.4f", totalCaptures, cameraLux));
                Log.d("Luxury/Capture", String.format("[%d] %.3f, %d, %.4f, %.3f --> %.3f", totalCaptures, fStop, exposureTime, exposureTimeSec, ev, cameraLux));
                totalCaptures++;
            }
        };

        @Override
        public void run() {
            while(shouldRun) {
                CaptureRequest captureRequest = null;
                try {
                    final CaptureRequest.Builder captureBuilder =  MeasureActivityOld.this.frontFacingCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                    captureBuilder.addTarget(surfaceView.getHolder().getSurface());
                    captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                    captureBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_HDR);
                    captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                    captureBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                            CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
                    captureBuilder.set(CaptureRequest.CONTROL_AE_LOCK, false);
                    captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                    captureBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
                    captureRequest = captureBuilder.build();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                if(sendMeasureMessage || captureRequest == null) {
                    continue;
                }
                if(isTimeUp()) {
                    shouldRun = false;
                    break;
                }
                final CaptureRequest finalCaptureRequest = captureRequest;
                MeasureActivityOld.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("Luxury/Camera", "Started capture");
                            MeasureActivityOld.this.cameraCaptureSession.capture(finalCaptureRequest, captureCallback, null);
                        } catch (CameraAccessException e) {
                            setTextView("Failed to capture");
                        }
                    }
                });
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);
        lightData.clear();
        startTime = System.currentTimeMillis();
        setTextView("Please set your phone in the sun!");
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    private boolean shouldRecordData(final float light) {
        return light > 50 && System.currentTimeMillis() > startTime + 5000
                && !isTimeUp();
    }

    private boolean isTimeUp() {
        return System.currentTimeMillis() > measureStartTime + 20000 && measureStartTime > 0;
    }

    private void cleanup(final boolean clearList) {
        sensorManager.unregisterListener(this);
        startTime = -1;
        measureStartTime = -1;
        if(cameraThread != null) {
            try {
                cameraThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (clearList) {
            lightData.clear();
        }
    }

    private void setTextView(final String text) {
        final TextView textView = this.findViewById(R.id.progressTextView);
        Log.d("Luxury", "setTextView, textView: " + text);
        textView.setText(text);
        textView.setVisibility(View.GONE);
        textView.setVisibility(View.VISIBLE);
        textView.postInvalidate();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final float light = event.values[0];
        Log.d("Luxury", "onSensorChanged: " + light);
        if (shouldRecordData(light)) {
            if (sendMeasureMessage) {
                Log.d("Luxury", "Measuring light...");
                setTextView("Measuring light...");
                measureStartTime = System.currentTimeMillis();
                sendMeasureMessage = false;
            }
            lightData.add(light);
            return;
        }
        if (isTimeUp()) {
            Log.d("Luxury", "onSensorChanged, times up!");
            float runningTotal = 0f;
            for (float f : lightData) {
                runningTotal += f;
            }
            cleanup(false);
            final float averageLight = runningTotal / lightData.size();
            setTextView("Average Light: " + averageLight);
            Log.d("Luxury", "onSensorChanged, averageLight: " + averageLight);
            final float uvModifier = 300;
            final float uvLight = (averageLight * uvModifier) / 1000f;
            Log.d("Luxury", "onSensorChanged, uvLight: " + uvLight);
            final int uvIndex = Math.round(uvLight / 25);
            Log.d("Luxury", "onSensorChanged, uvIndex: " + uvIndex);
            lightData.clear();
            sendMeasureMessage = true;
            final Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra("uv_index", uvIndex);
            startActivity(intent);
        }
    }

    private void createCamera() throws CameraAccessException {

        final CameraManager cameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            return;
        }
//        for (final String s : cameraManager.getCameraIdList()) {
//            final CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(s);
//            Log.d("Luxury/Camera-Stats", String.format("<=============================%s=============================", s));
//            for(final CameraCharacteristics.Key k: characteristics.getKeys()) {
//                Log.d("Luxury/Camera-Stats", String.format("%s=%s", k.getName(), characteristics.get(k).toString()));
//            }
//            Log.d("Luxury/Camera-Stats", String.format("=============================%s=============================>", s));
//        }
        cameraManager.openCamera("1", this.cameraCallback, this.cameraHandler);
        surfaceView = this.findViewById(R.id.surfaceView);
        cameraOutputs.add(surfaceView.getHolder().getSurface());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanup(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        cleanup(true);
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        try {
            createCamera();
        } catch (CameraAccessException e) {
            setTextView("Camera creation exception!");
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        cleanup(true);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
*/
