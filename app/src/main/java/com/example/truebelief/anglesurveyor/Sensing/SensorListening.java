package com.example.truebelief.anglesurveyor.Sensing;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import com.example.truebelief.anglesurveyor.Filter.Statistics;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by truebelief on 2015/7/3.
 */
public class SensorListening implements SensorEventListener {

    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];
    private float[] accMagOrientation = new float[3];


    private float[] gyroValues = new float[3];
    private float[] gyroMatrix = new float[9];
    private float[] gyroOrientation = new float[3];

    private float[] rotationMatrix = new float[9];
    private float[] fusedOrientation = new float[3];

    private boolean initGyroState = true;
    private float timeStamp;
//    public Handler handler;

    private float temperature;
    private float pressure;
    private float lightValues;

    public static final int TIME_CONSTANT = 30;
    public static final float EPSILON = 0.000000001f;
    private static final float NS2S = 1.0f / 1000000000.0f;
    public static final float FILTER_COEFFICIENT = 0.98f;

    private float[] fusedOrientation_std=new float[3];
    private ArrayList azimuth_buffer=new ArrayList(20);
    private ArrayList pitch_buffer=new ArrayList(20);
    private ArrayList roll_buffer=new ArrayList(20);
    private int buffer_count=0;

    private SensorManager sensorManager;
    public SensorDataStructure sds;
    private Activity _activity;
    private TextView txv;
//    private float[] rotAngles;
    public static final float DEGREE2RAD=3.1415926f/180f;
    public static final float RAD2DEGREE=180f/3.1415926f;
    public SensorListening (Activity _act){
        _activity=_act;
        init();
    }
    public SensorListening (Activity _act, TextView tx){
        _activity=_act;
        txv=tx;
        init();
    }
//    public SensorListening (Activity _act, TextView tx,float[] ra){
//        _activity=_act;
//        txv=tx;
//        rotAngles=ra;
//        init();
//    }
    private void init(){
        String service_name = Context.SENSOR_SERVICE;
        sensorManager = (SensorManager)_activity.getSystemService(service_name);


        gyroOrientation[0] = 0.0f;
        gyroOrientation[1] = 0.0f;
        gyroOrientation[2] = 0.0f;

        // initialise gyroMatrix with identity matrix
        gyroMatrix[0] = 1.0f; gyroMatrix[1] = 0.0f; gyroMatrix[2] = 0.0f;
        gyroMatrix[3] = 0.0f; gyroMatrix[4] = 1.0f; gyroMatrix[5] = 0.0f;
        gyroMatrix[6] = 0.0f; gyroMatrix[7] = 0.0f; gyroMatrix[8] = 1.0f;
    }

    public void InitListener(Timer fuseTimer){
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                sensorManager.SENSOR_DELAY_FASTEST);

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                sensorManager.SENSOR_DELAY_FASTEST);

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                sensorManager.SENSOR_DELAY_FASTEST);

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                sensorManager.SENSOR_DELAY_NORMAL);

        fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(), 1000, TIME_CONSTANT);

    }
    public void StopListener(Timer fuseTimer){
        sensorManager.unregisterListener(this);
        fuseTimer.cancel();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                // copy new accelerometer data into accel array and calculate orientation
                System.arraycopy(event.values, 0, accelerometerValues, 0, 3);
                calculateAccMagOrientation();
                break;

            case Sensor.TYPE_GYROSCOPE:
                // process gyro data
                gyroFunction(event);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                // copy new magnetometer data into magnet array
                System.arraycopy(event.values, 0, magneticFieldValues, 0, 3);
                break;

            case Sensor.TYPE_LIGHT:
                lightValues=event.values[0]+ new Random().nextFloat()*20;
                break;
        }
    }


    // calculates orientation angles from accelerometer and magnetometer output
    public void calculateAccMagOrientation() {
        if(SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerValues, magneticFieldValues)) {
            SensorManager.getOrientation(rotationMatrix, accMagOrientation);
        }
    }

    // This function is borrowed from the Android reference
    // at http://developer.android.com/reference/android/hardware/SensorEvent.html#values
    // It calculates a rotation vector from the gyroscope angular speed values.
    private void getRotationVectorFromGyro(float[] gyroValues,
                                           float[] deltaRotationVector,
                                           float timeFactor)
    {
        float[] normValues = new float[3];

        // Calculate the angular speed of the sample
        float omegaMagnitude =
                (float)Math.sqrt(gyroValues[0] * gyroValues[0] +
                        gyroValues[1] * gyroValues[1] +
                        gyroValues[2] * gyroValues[2]);

        // Normalize the rotation vector if it's big enough to get the axis
        if(omegaMagnitude > EPSILON) {
            normValues[0] = gyroValues[0] / omegaMagnitude;
            normValues[1] = gyroValues[1] / omegaMagnitude;
            normValues[2] = gyroValues[2] / omegaMagnitude;
        }

        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        float thetaOverTwo = omegaMagnitude * timeFactor;
        float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
        deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
        deltaRotationVector[3] = cosThetaOverTwo;
    }

    // This function performs the integration of the gyroscope data.
    // It writes the gyroscope based orientation into gyroOrientation.
    public void gyroFunction(SensorEvent event) {
        // don't start until first accelerometer/magnetometer orientation has been acquired
        if (accMagOrientation == null)
            return;

        // initialisation of the gyroscope based rotation matrix
        if(initGyroState) {
            float[] initMatrix = new float[9];
            initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
            float[] test = new float[3];
            SensorManager.getOrientation(initMatrix, test);
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
            initGyroState = false;
        }

        // copy the new gyro values into the gyro array
        // convert the raw gyro data into a rotation vector
        float[] deltaVector = new float[4];
        if(timeStamp != 0) {
            final float dT = (event.timestamp - timeStamp) * NS2S;
            System.arraycopy(event.values, 0, gyroValues, 0, 3);
            getRotationVectorFromGyro(gyroValues, deltaVector, dT / 2.0f);
        }

        // measurement done, save current time for next interval
        timeStamp = event.timestamp;

        // convert rotation vector into rotation matrix
        float[] deltaMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

        // apply the new rotation interval on the gyroscope based rotation matrix
        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);

        // get the gyroscope based orientation from the rotation matrix
        SensorManager.getOrientation(gyroMatrix, gyroOrientation);
    }

    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        float sinX = (float)Math.sin(o[1]);
        float cosX = (float)Math.cos(o[1]);
        float sinY = (float)Math.sin(o[2]);
        float cosY = (float)Math.cos(o[2]);
        float sinZ = (float)Math.sin(o[0]);
        float cosZ = (float)Math.cos(o[0]);

        // rotation about x-axis (pitch)
        xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
        xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
        xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;

        // rotation about y-axis (roll)
        yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
        yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
        yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;

        // rotation about z-axis (azimuth)
        zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
        zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
        zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;

        // rotation order is y, x, z (roll, pitch, azimuth)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
    }

    private float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];

        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return result;
    }

    class calculateFusedOrientationTask extends TimerTask {
        @Override
        public void run() {
            _activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;

                    /*
                     * Fix for 179?<--> -179?transition problem:
                     * Check whether one of the two orientation angles (gyro or accMag) is negative while the other one is positive.
                     * If so, add 360?(2 * math.PI) to the negative value, perform the sensor fusion, and remove the 360?from the result
                     * if it is greater than 180? This stabilizes the output in positive-to-negative-transition cases.
                     */

                    // azimuth
                    if (gyroOrientation[0] < -0.5 * Math.PI && accMagOrientation[0] > 0.0) {
                        fusedOrientation[0] = (float) (FILTER_COEFFICIENT * (gyroOrientation[0] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[0]);
                        fusedOrientation[0] -= (fusedOrientation[0] > Math.PI) ? 2.0 * Math.PI : 0;
                    }
                    else if (accMagOrientation[0] < -0.5 * Math.PI && gyroOrientation[0] > 0.0) {
                        fusedOrientation[0] = (float) (FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * (accMagOrientation[0] + 2.0 * Math.PI));
                        fusedOrientation[0] -= (fusedOrientation[0] > Math.PI)? 2.0 * Math.PI : 0;
                    }
                    else {
                        fusedOrientation[0] = FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * accMagOrientation[0];
                    }

                    // pitch
                    if (gyroOrientation[1] < -0.5 * Math.PI && accMagOrientation[1] > 0.0) {
                        fusedOrientation[1] = (float) (FILTER_COEFFICIENT * (gyroOrientation[1] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[1]);
                        fusedOrientation[1] -= (fusedOrientation[1] > Math.PI) ? 2.0 * Math.PI : 0;
                    }
                    else if (accMagOrientation[1] < -0.5 * Math.PI && gyroOrientation[1] > 0.0) {
                        fusedOrientation[1] = (float) (FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * (accMagOrientation[1] + 2.0 * Math.PI));
                        fusedOrientation[1] -= (fusedOrientation[1] > Math.PI)? 2.0 * Math.PI : 0;
                    }
                    else {
                        fusedOrientation[1] = FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * accMagOrientation[1];
                    }

                    // roll
                    if (gyroOrientation[2] < -0.5 * Math.PI && accMagOrientation[2] > 0.0) {
                        fusedOrientation[2] = (float) (FILTER_COEFFICIENT * (gyroOrientation[2] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[2]);
                        fusedOrientation[2] -= (fusedOrientation[2] > Math.PI) ? 2.0 * Math.PI : 0;
                    }
                    else if (accMagOrientation[2] < -0.5 * Math.PI && gyroOrientation[2] > 0.0) {
                        fusedOrientation[2] = (float) (FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * (accMagOrientation[2] + 2.0 * Math.PI));
                        fusedOrientation[2] -= (fusedOrientation[2] > Math.PI)? 2.0 * Math.PI : 0;
                    }
                    else {
                        fusedOrientation[2] = FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * accMagOrientation[2];
                    }

                    // overwrite gyro matrix and orientation with fused orientation
                    // to comensate gyro drift
                    gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
                    System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);


                    if (buffer_count<=10)
                    {
                        azimuth_buffer.add(fusedOrientation[0]);
                        pitch_buffer.add(fusedOrientation[1]);
                        roll_buffer.add(fusedOrientation[2]);
                        buffer_count++;
                    }else {
                        azimuth_buffer.remove(0);
                        pitch_buffer.remove(0);
                        roll_buffer.remove(0);
                        azimuth_buffer.add(fusedOrientation[0]);
                        pitch_buffer.add(fusedOrientation[1]);
                        roll_buffer.add(fusedOrientation[2]);
                        fusedOrientation_std[0]= Statistics.std(azimuth_buffer);
                        fusedOrientation_std[1]=Statistics.std(pitch_buffer);
                        fusedOrientation_std[2]=Statistics.std(roll_buffer);
                    }

                    // update sensor output in GUI
                    //            mHandler.post(updateOreintationDisplayTask);
//                    sds=new SensorDataStructure((float)((fusedOrientation[0]+ new Random().nextFloat())*2 * 180/Math.PI),(float)(fusedOrientation[1] * 180/Math.PI),(float)(fusedOrientation[2] * 180/Math.PI),lightValues,new Date());
//                    sds=new SensorDataStructure((float)(fusedOrientation[0] * 180/Math.PI),(float)(fusedOrientation[1] * 180/Math.PI),(float)(fusedOrientation[2] * 180/Math.PI),lightValues,0,0,0,new Date());
//                    sds=new SensorDataStructure((float)(fusedOrientation[0] * 180/Math.PI),(float)(fusedOrientation[1] * 180/Math.PI),(float)(fusedOrientation[2] * 180/Math.PI),lightValues,new Date());
                    sds=new SensorDataStructure((float)(fusedOrientation[0]* RAD2DEGREE),(float)(fusedOrientation[1] * RAD2DEGREE),(float)(fusedOrientation[2] * RAD2DEGREE),
                            fusedOrientation_std[0]*RAD2DEGREE,fusedOrientation_std[1]*RAD2DEGREE,fusedOrientation_std[2]*RAD2DEGREE,
                            lightValues,
                            new Date());

                    if (txv!=null)
                    {
                        txv.setText(sds.toFormattedString());
                    }

//                    if (rotAngles!=null){
//                        rotAngles[0]=sds.angle_x;
//                        rotAngles[1]=sds.angle_y;
//                        rotAngles[2]=sds.angle_z;
//                    }

                }
            });
        }
    }
}
