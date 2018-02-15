package com.example.roxi.sensorprojekt2;
        import android.app.Dialog;
        import android.app.Service;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.graphics.Camera;
        import android.hardware.Sensor;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
        import android.hardware.SensorManager;
        import android.hardware.camera2.CameraAccessException;
        import android.hardware.camera2.CameraManager;
        import android.media.MediaPlayer;
        import android.os.Build;
        import android.support.v4.app.ActivityCompat;
        import android.support.v7.app.AlertDialog;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.google.android.gms.common.ConnectionResult;
        import com.google.android.gms.common.GoogleApiAvailability;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    TextView tv, tvGyroscope, tvdegrees, tvPressure;
    SensorManager sensorManager;
    Sensor sensor, gyroscopeSensor, accelerometerSensor, magnetometerSensor, pressureSensor;

    private CameraManager mCameraManager;
    private String mCameraId;
    boolean isOn;
    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private boolean lastAccelerometerSet = false;
    private boolean lastMagnetometerSet = false;
    private float[] r = new float[9];
    private float[] orientation = new float[3];
    private static final String TAG = "MainActivity";



    private static final int ERROR_DIALOG_REQUEST = 9001;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.lightTextView);
        tvGyroscope = (TextView) findViewById(R.id.sensor1TextView);
        tvdegrees = (TextView) findViewById(R.id.sensor2TextView);
        tvPressure= (TextView) findViewById(R.id.sensor3TextView);
        sensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if(pressureSensor==null)
            Toast.makeText(this.getApplicationContext(),
                    "Sorry - your device doesn't have an ambient temperature sensor!",
                    Toast.LENGTH_SHORT).show();

        Log.d("mainActivity", "onCreate()");

        isOn = false;
        Boolean isFlashAvailable = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }


    public boolean isServicesOK(){

        Log.d(TAG, "isServicesOK: checking google services version");



        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);



        if(available == ConnectionResult.SUCCESS){

            //everything is fine and the user can make map requests

            Log.d(TAG, "isServicesOK: Google Play Services is working");
            Toast.makeText(this, "isServicesOK: Google Play Services is working", Toast.LENGTH_SHORT).show();

            return true;

        }

        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){

            //an error occured but we can resolve it

            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Toast.makeText(this, "isServicesOK: an error occured but we can fix it", Toast.LENGTH_SHORT).show();
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);

            dialog.show();

        }else{

            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();

        }

        return false;

    }

    public void turnOnFlashLight() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager.setTorchMode(mCameraId, true);

                isOn = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void turnOffFlashLight() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager.setTorchMode(mCameraId, false);
                isOn = false;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);


    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT) {
            tv.setText("" + sensorEvent.values[0]);
            //tvGyroscope.setText("" + sensorEvent.values[0]);
            if (isOn == false && sensorEvent.values[0] < 10) {
                turnOnFlashLight();

            }
            if (isOn == true && sensorEvent.values[0] > 10) {
                turnOffFlashLight();

            }}
        if (sensorEvent.sensor == accelerometerSensor) {
            System.arraycopy(sensorEvent.values, 0, lastAccelerometer, 0, sensorEvent.values.length);
            lastAccelerometerSet = true;
        } else if (sensorEvent.sensor == magnetometerSensor) {
            System.arraycopy(sensorEvent.values, 0, lastMagnetometer, 0, sensorEvent.values.length);
            lastMagnetometerSet = true;
        }
        if (lastAccelerometerSet && lastMagnetometerSet) {
            SensorManager.getRotationMatrix(r, null, lastAccelerometer, lastMagnetometer);
            SensorManager.getOrientation(r, orientation);
            float azimuthInDegrees = (int) (Math.toDegrees(orientation[0] + 360) % 360);
            tvdegrees.setText(String.valueOf(azimuthInDegrees));

        }

        if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            tvGyroscope.setText("x: " + String.valueOf(sensorEvent.values[0]) + " y: " + String.valueOf(sensorEvent.values[1]) + " x: " + String.valueOf(sensorEvent.values[2]));
        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PRESSURE) {
            tvPressure.setText("Pressure: " + sensorEvent.values[0]);
        }

    }

    @Override
    public void onAccuracyChanged (Sensor sensor,int i){

    }

    protected void onStop() {
        turnOffFlashLight();
        super.onStop();
    }

    public void klik(View view) {


        if(isServicesOK()){

            Intent intent = new Intent(MainActivity.this, MapsActivity.class);

            startActivity(intent);

        }}


}