package com.example.truebelief.anglesurveyor.Sensing;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.truebelief.anglesurveyor.Filter.LowPassSmoothingFilter;
import com.example.truebelief.anglesurveyor.Filter.Statistics;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by truebelief on 2015/7/4.
 */
public class SensorInquiry implements SensorEventListener {
    public float[] accelerometerValues = new float[3];
    public float[] accelerometerLinearValues = new float[3];
    public float[] magneticFieldValues = new float[3];
    public float[] accMagOrientation = new float[3];


    public float[] gyroValues = new float[3];
    public float[] gyroMatrix = new float[9];
    public float[] gyroOrientation = new float[3];

    public float[] rotationMatrix = new float[9];
    public float[] fusedRaw=new float[3];
    public float[] fusedOrientation = new float[3];

    public float[] gravityValues=new float[3];
    public float[] last_gravityValues=new float[3];
    public boolean initGyroState = true;
    public boolean initLinearAccState=true;

    public float temperature;
    public float pressure;
    public float lightValues;

    public static final int TIME_CONSTANT = 30;
    public static final float EPSILON = 0.000000001f;
    public static final float NS2S = 1.0f / 1000000000.0f;
    public static final float FILTER_COEFFICIENT = 0.98f;
    public static final float DEGREE2RAD=3.1415926f/180f;
    public static final float RAD2DEGREE=180f/3.1415926f;


    public long count=0;
    public float timeStamp;
    public float timestamp;
    public double tspan;
    public float timeConstant=0.2f;

    public SensorManager sensorManager;
    public LocationManager locationManager;
    public SensorDataStructure sds;
    public Activity _activity;

    private float[] position=new float[3];
    private float[] velocity=new float[3];
    private float[] last_velocity=new float[3];
    private float[] last_acceleration=new float[3];


    private float[] fusedOrientation_std=new float[3];
    private ArrayList azimuth_buffer=new ArrayList(20);
    private ArrayList pitch_buffer=new ArrayList(20);
    private ArrayList roll_buffer=new ArrayList(20);
    private int buffer_count=0;
//    private float current_t;
    private float last_t;
    private double dt;

//    public PositionInquiry positionInquiry;
    private Location loc;
    private Location loc0;
    private boolean isInitGPS=false;
    private double lat0,lon0,ele0;
    private double lat,lon,ele;

    private  double loc_accuracy;

    public String log_content;

    public boolean isInit=false;
    public boolean isLogged=false;
//    public float[] rotAngles;

    LowPassSmoothingFilter lpsAcceleration;

//    private TextView txv;

    public SensorInquiry (Activity _act){
        _activity=_act;
        if (!isInit)
            init();
    }
//    public SensorInquiry (Activity _act, TextView tx){
//        _activity=_act;
//        txv=tx;
//        init();
//    }

    private void init(){
        String sensor_service_name = Context.SENSOR_SERVICE;
        String location_service_name=Context.LOCATION_SERVICE;
        sensorManager = (SensorManager)_activity.getSystemService(sensor_service_name);
//        positionInquiry=new PositionInquiry(_activity);


//        locationManager = (LocationManager) _activity.getSystemService(location_service_name);

//        int minTime = 3000;
//        int minDistance = 0;




        gyroOrientation[0] = 0.0f;
        gyroOrientation[1] = 0.0f;
        gyroOrientation[2] = 0.0f;

        // initialise gyroMatrix with identity matrix
        gyroMatrix[0] = 1.0f; gyroMatrix[1] = 0.0f; gyroMatrix[2] = 0.0f;
        gyroMatrix[3] = 0.0f; gyroMatrix[4] = 1.0f; gyroMatrix[5] = 0.0f;
        gyroMatrix[6] = 0.0f; gyroMatrix[7] = 0.0f; gyroMatrix[8] = 1.0f;

        last_velocity[0]=0f;
        last_velocity[1]=0f;
        last_velocity[2]=0f;
        velocity[0]=0f;
        velocity[1]=0f;
        velocity[2]=0f;

        last_t=0f;
//        position[0]=0f;
//        position[1]=0f;
//        position[2]=0f;

        gravityValues[0]=0f;
        gravityValues[1]=0f;
        gravityValues[2]=0f;

//        lpsAcceleration=new LowPassSmoothingFilter();
        isInit=true;


    }

    public void InitListener(Timer fuseTimer){
        if (!isInit)init();

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                sensorManager.SENSOR_DELAY_FASTEST);
//        sensorManager.registerListener(this,
//                sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
//                sensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
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

        isInit=true;

    }
    public void StopListener(Timer fuseTimer){
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, accelerometerValues, 0, 3);
                calculateAccMagOrientation();
//                final float[] gravity= lpsAcceleration.ApplyFilter(accelerometerValues);
//                accelerometerLinearValues[0]=accelerometerValues[0]-gravity[0];
//                accelerometerLinearValues[1]=accelerometerValues[1]-gravity[1];
//                accelerometerLinearValues[2]=accelerometerValues[2]-gravity[2];
//                System.arraycopy(accelerometerLinearValues,0,position,0,3);
//                accelerometerLinearValues=

//                CalcPosition(event.timestamp);
//                LogAccValues();


//                break;
//            case Sensor.TYPE_LINEAR_ACCELERATION:
                // copy new accelerometer data into accel array and calculate orientation
//                accelerometerLinearValues[0]=(accelerometerLinearValues[0]+event.values[0])*0.5f;
//                accelerometerLinearValues[1]=(accelerometerLinearValues[1]+event.values[1])*0.5f;
//                accelerometerLinearValues[2]=(accelerometerLinearValues[2]+event.values[2])*0.5f;
//                accelerometerLinearValues[0]=1;
//                accelerometerLinearValues[1]=0;
//                accelerometerLinearValues[2]=0;


//                System.arraycopy(event.values, 0, accelerometerLinearValues, 0, 3);
//                linearAccFunction(event);
//                dt=(event.timestamp - timeStamp) * NS2S;

//                calculateAccMagOrientation();
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
                lightValues=event.values[0];
                break;

            case Sensor.TYPE_GRAVITY:
                System.arraycopy(event.values,0,gravityValues,0,3);
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
    public void getRotationVectorFromGyro(float[] gyroValues,
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

//    public void LogAccValues(){
//        timestamp=System.nanoTime();
//        if (last_t==0f){
//            count=1;
//            last_t=timestamp;
//            log_content ="time,dt,theta_x,theta_y,theta_z," +
//                    "accMag_x,accMag_y,accMag_z," +
//                    "gyro_x,gyro_y,gyro_z\r\n";
//            return;
//        }
//        double t=((timestamp - last_t) * NS2S);
//        if (t==tspan){
//            return;
//        }
//        tspan=t;
//        dt = tspan/count;
//        count++;
//        if (tspan<2){
//            return;
//        }
//        if (tspan<=40) {
//            log_content += tspan + "," + dt + "," + fusedOrientation[0]* RAD2DEGREE + "," + fusedOrientation[1]* RAD2DEGREE + "," + fusedOrientation[2]* RAD2DEGREE
//                    + "," + accMagOrientation[0]* RAD2DEGREE + "," + accMagOrientation[1]* RAD2DEGREE + "," + accMagOrientation[2]* RAD2DEGREE
//                    + "," + gyroOrientation[0]* RAD2DEGREE + "," + gyroOrientation[0]* RAD2DEGREE + "," + gyroOrientation[0]* RAD2DEGREE
//                    +"\r\n";
//        }else{
//            if (!isLogged){
//                Toast.makeText(_activity, "Start Logging!", Toast.LENGTH_LONG).show();
//                try{
//                    String sdcard_path = Environment.getExternalStorageDirectory().getAbsolutePath();
//                    File fileDir = new File(sdcard_path+"/AngleSurveyor/");
//                    if (!fileDir.exists()){
//                        fileDir.mkdir();
//                    };
//                    File file = new File(fileDir, "Log_angle.csv");
//                    FileOutputStream stream = new FileOutputStream(file);
//                    try {
//                        stream.write(log_content.getBytes());
//                    } finally {
//                        Toast.makeText(_activity, "Log saved!", Toast.LENGTH_SHORT).show();
//                        stream.close();
//                    }
//                }catch(Exception e){
//                    e.printStackTrace();
//                    Toast.makeText(_activity, "Log not saved!", Toast.LENGTH_SHORT).show();
//                }
//            }
//            isLogged=true;
//        }
//
//
//
//    }

    public void LogValues(){
        timestamp=System.nanoTime();
        if (last_t==0f){
            count=1;
            last_t=timestamp;
            log_content ="time,dt,theta_x,theta_y,theta_z," +
                    "acc_x,acc_y,acc_z,grav_x,grav_y,grav_z," +
                    "vel_x,vel_y,vel_z,pos_x,pos_y,pos_z,pos_accuracy\r\n";
            return;
        }


        double t=((timestamp - last_t) * NS2S);
        if (t==tspan){
            return;
        }
        tspan=t;
        dt = tspan/count;
        count++;
        if (tspan<3){
            return;
        }

        if (!isInitGPS)
        {
//            loc0=positionInquiry.getLocation();
            if (loc0!=null){
                ele0=loc0.getAltitude();
                Toast.makeText(_activity, "GPS initialed!", Toast.LENGTH_LONG).show();
                isInitGPS=true;
            }

        }
//        loc=positionInquiry.getLocation();
        if (loc!=null){
            ele = loc.getAltitude();
            double dist=loc.distanceTo(loc0);
            double bearing=loc.getBearing()*DEGREE2RAD;
            double vel=loc.getSpeed();
            velocity[0]=(float)(vel*Math.cos(bearing));
            velocity[1]=(float)(vel*Math.sin(bearing));
            velocity[2]=0f;

            position[0]=(float)(dist*Math.cos(bearing));
            position[1]=(float)(dist*Math.sin(bearing));
            position[2]=(float)(ele-ele0);

            loc_accuracy=loc.getAccuracy();
        }



        if (tspan<=60) {
            log_content += tspan + "," + dt + "," + fusedOrientation[0]* RAD2DEGREE + "," + fusedOrientation[1]* RAD2DEGREE + "," + fusedOrientation[2]* RAD2DEGREE
                    + "," + accelerometerValues[0] + "," + accelerometerValues[1] + "," + accelerometerValues[2]
                    + "," + gravityValues[0] + "," + gravityValues[1] + "," + gravityValues[2]
                    + "," + velocity[0] + "," + velocity[1] + "," + velocity[2]
                    + "," + position[0] + "," + position[1] + "," + position[2] + "," + loc_accuracy+"\r\n";
        }else{
            if (!isLogged){
                Toast.makeText(_activity, "Start Logging!", Toast.LENGTH_LONG).show();
                try{
                    String sdcard_path = Environment.getExternalStorageDirectory().getAbsolutePath();
                    File fileDir = new File(sdcard_path+"/AngleSurveyor/");
                    if (!fileDir.exists()){
                        fileDir.mkdir();
                    };
                    File file = new File(fileDir, "Log.csv");
                    FileOutputStream stream = new FileOutputStream(file);
                    try {
                        stream.write(log_content.getBytes());
                    } finally {
                        Toast.makeText(_activity, "Log saved!", Toast.LENGTH_SHORT).show();
                        stream.close();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    Toast.makeText(_activity, "Log not saved!", Toast.LENGTH_SHORT).show();
                }
            }
            isLogged=true;
        }
    }

    public void LogValues2(){
        timestamp=System.nanoTime();

        if (last_t==0f){
            count=1;
            last_t=timestamp;
            log_content ="time,acc_x,acc_y,acc_z,v_x,v_y,v_z,dt,pos_x,pos_y,pos_z\r\n";
            return;
        }
        double t=((timestamp - last_t) * NS2S);
        if (t==tspan){
            return;
        }
        tspan=t;
        dt = tspan/count;
        count++;
//        Log.v("tm",String.valueOf(tspan));
        if (tspan<3){
            return;
        }


        if (tspan<=30) {
            velocity[0] += accelerometerLinearValues[0] * dt;
            velocity[1] += accelerometerLinearValues[1] * dt;
            velocity[2] += accelerometerLinearValues[2] * dt;
            position[0] += (velocity[0]) * dt;
            position[1] += (velocity[1]) * dt;
            position[2] += (velocity[2]) * dt;

            log_content += tspan + "," + accelerometerLinearValues[0] + "," + accelerometerLinearValues[1] + "," + accelerometerLinearValues[2]
                    + "," + velocity[0] + "," + velocity[1] + "," + velocity[2] + "," + dt
                    + "," + position[0] + "," + position[1] + "," + position[2] + "\r\n";
        }else{
            if (!isLogged){
                try{
                    String sdcard_path = Environment.getExternalStorageDirectory().getAbsolutePath();
                    File fileDir = new File(sdcard_path+"/AngleSurveyor/");
                    if (!fileDir.exists()){
                        fileDir.mkdir();
                    };
                    File file = new File(fileDir, "Log.csv");
                    FileOutputStream stream = new FileOutputStream(file);
                    try {
                        stream.write(log_content.getBytes());
                    } finally {
                        Toast.makeText(_activity, "Log saved!", Toast.LENGTH_SHORT).show();
                        stream.close();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    Toast.makeText(_activity, "Log not saved!", Toast.LENGTH_SHORT).show();
                }
            }
            isLogged=true;
        }




    }
    public void CalcPosition(long ts){
        timestamp=ts;

        if (last_t==0f){
            count=1;
            last_t=timestamp;
            return;
        }
//        dt=(timestamp-last_t) * NS2S;
        count++;
        if (count>100){
            dt = ((timestamp - last_t) *NS2S) / (count);
//            Log.v("dfa1",String.valueOf(velocity[2]));
            Log.v("dfaACC",String.valueOf(accelerometerLinearValues[2]));
            double accelerometerLinearValuesABS=Math.sqrt(accelerometerLinearValues[0]*accelerometerLinearValues[0]
            +accelerometerLinearValues[1]*accelerometerLinearValues[1]+accelerometerLinearValues[2]*accelerometerLinearValues[2]);
            if (accelerometerLinearValuesABS>0.01) {
                velocity[0] += accelerometerLinearValues[0] * dt;
                velocity[1] += accelerometerLinearValues[1] * dt;
                velocity[2] += accelerometerLinearValues[2] * dt;
            }
//            Log.v("dfa2",String.valueOf(velocity[2]));
//                position[0] += (velocity[0] + last_velocity[0]) * dt * 0.5;
//                position[1] += (velocity[1] + last_velocity[1]) * dt * 0.5;
//                position[2] += (velocity[2] + last_velocity[2]) * dt * 0.5;
            Log.v("dfaV1",String.valueOf(velocity[2]));
            double velocityABS=Math.sqrt(velocity[0]*velocity[0]+velocity[1]*velocity[1]+velocity[2]*velocity[2]);
            if (velocityABS>0.02){
                position[0] += (velocity[0]) * dt;
                position[1] += (velocity[1]) * dt;
                position[2] += (velocity[2]) * dt;
            }



//                System.arraycopy(velocity, 0, last_velocity, 0, 3);
//            }

//            System.arraycopy(velocity, 0, position, 0, 3);
        }




//        last_t=timestamp;
//


    }
//    public void linearAccFunction(SensorEvent event) {
////        if (accMagOrientation == null)
////            return;
////        if (last_t==0){
////            last_t=event.timestamp;
//////            System.arraycopy(event.values,0,gravityValues,0,3);
////            return;
////        }
//        accelerometerLinearValues[0]=event.values[0]-gravityValues[0];
//        accelerometerLinearValues[1]=event.values[1]-gravityValues[1];
//        accelerometerLinearValues[2]=event.values[2]-gravityValues[2];
//
////        float dt = 1 / (count++ / ((event.timestamp - last_t) / NS2S));
////
////        float alpha = timeConstant / (timeConstant + dt);
////        if(count> 5) {
////            Log.v("dfsa", "df");
////            final float dt=(event.timestamp-last_t) * NS2S;
//
////            gravityValues[0]=alpha*last_gravityValues[0]+(1-alpha)*gravityValues[0];
////            gravityValues[1]=alpha*last_gravityValues[1]+(1-alpha)*gravityValues[1];
////            gravityValues[2]=alpha*last_gravityValues[2]+(1-alpha)*gravityValues[2];
////            System.arraycopy(gravityValues, 0, last_gravityValues, 0, 3);
//
////            accelerometerLinearValues[0]=alpha*last_acceleration[0]+(1-alpha)*accelerometerLinearValues[0];
////            accelerometerLinearValues[1]=alpha*last_acceleration[1]+(1-alpha)*accelerometerLinearValues[1];
////            accelerometerLinearValues[2]=alpha*last_acceleration[2]+(1-alpha)*accelerometerLinearValues[2];
////            System.arraycopy(accelerometerLinearValues, 0, last_acceleration, 0, 3);
//
////            if (accelerometerLinearValues[0]<0.1 && accelerometerLinearValues[0]>-0.1){
////                velocity[0]=0;
////            }else {
////                velocity[0]=last_velocity[0]+accelerometerLinearValues[0]*dt;
////            }
////
////            if (accelerometerLinearValues[1]<0.1 &&  accelerometerLinearValues[1]>-0.1){
////                velocity[1]=0;
////            }else {
////                velocity[1]=last_velocity[1]+accelerometerLinearValues[1]*dt;
////            }
////            if (accelerometerLinearValues[2]<0.1 &&  accelerometerLinearValues[2]>-0.1){
////                velocity[2]=0;
////            }else {
////                velocity[2]=last_velocity[2]+accelerometerLinearValues[2]*dt;
////            }
//
////            position[0]+=(velocity[0]+last_velocity[0])*dt*0.5;
////            position[1]+=(velocity[1]+last_velocity[1])*dt*0.5;
////            position[2]+=(velocity[2]+last_velocity[2])*dt*0.5;
//
////            position[0]=accelerometerLinearValues[0]-gravityValues[0];
////            position[1]=accelerometerLinearValues[1]-gravityValues[1];
////            position[2]=accelerometerLinearValues[2]-gravityValues[2];
//
//            System.arraycopy(accelerometerLinearValues, 0, position, 0, 3);
//
////            position[0]=accelerometerLinearValues[0];
////            position[1]=accelerometerLinearValues[1];
////            position[2]=accelerometerLinearValues[2];
////        }
//
////        Log.v("ewr", "aads");
//
////        accelerometerLinearValues[0]=0f;
////        accelerometerLinearValues[1]=0f;
////        accelerometerLinearValues[2]=0f;
//
//
//    }

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

    public float[] getRotationMatrixFromOrientation(float[] o) {
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

    public float[] matrixMultiplication(float[] A, float[] B) {
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

    public void calculateFusedOrientation() {
        float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;

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

        // update sensor output in GUI
//        sds=new SensorDataStructure((float)((fusedOrientation[0] + new Random().nextFloat())* 180/3.1415926),(float)(fusedOrientation[1] * 180/3.1415926),(float)(fusedOrientation[2] * 180/3.1415926),lightValues,new Date());
//        if (txv!=null)
//        {
//            txv.setText(sds.toFormattedString());
//        }

    }


    public void calculateSensingData(){
        calculateFusedOrientation();
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
            fusedOrientation_std[0]=Statistics.std(azimuth_buffer);
            fusedOrientation_std[1]=Statistics.std(pitch_buffer);
            fusedOrientation_std[2]=Statistics.std(roll_buffer);
        }
        sds=new SensorDataStructure((float)(fusedOrientation[0]* RAD2DEGREE),(float)(fusedOrientation[1] * RAD2DEGREE),(float)(fusedOrientation[2] * RAD2DEGREE),
//                position[0],position[1],position[2],
                fusedOrientation_std[0]*RAD2DEGREE,fusedOrientation_std[1]*RAD2DEGREE,fusedOrientation_std[2]*RAD2DEGREE,
                lightValues,
                new Date());

    }

}
