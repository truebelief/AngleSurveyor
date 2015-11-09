package com.example.truebelief.anglesurveyor;

//import android.app.FragmentManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.os.Handler;
//import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
//import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Scroller;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.truebelief.anglesurveyor.Sensing.SensorDataStructure;
//import com.example.truebelief.anglesurveyor.Sensing.SensorInquiry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends FragmentActivity implements CreateJobFragment.onCreateJobFragmentListener,
        SlideMenuFragment.FragmentSwitchListener,ExistingJobsFragment.onExistingJobFragmentListener,
        SamplingFragment.onSamplingFragmentListener{

    public void onNewJobAdded(String newJob){
        if (currentFragment==existingJobsFragment) {
            if (!CheckJobName(newJob)){
                SwitchJobEdit(true);
                return;
            }
            ExistingJob newExistingJob = new ExistingJob(newJob);

            if (!ExistingJobs(existingJobsFragment.existing_job_list, newExistingJob)){
                existingJobsFragment.existing_job_list.add(0,newExistingJob);
                existingJobsFragment.existing_job_adapter.notifyDataSetChanged();
                SensorDataStructure newRecord=new SensorDataStructure();
                ArrayList<SensorDataStructure> newList=new ArrayList<SensorDataStructure>();
                newList.add(newRecord);
                sensorDatabase.add(0,newList);
                currentJobIndicator=0;
                currentJobName=newJob;
                String content="date,azimuth,pitch,roll,azimuth_std,pitch_std,roll_std,light\r\n";
//                FileOutputStream outputStream;
                try{
                    String sdcard_path = Environment.getExternalStorageDirectory().getAbsolutePath();
                    File fileDir = new File(sdcard_path+"/AngleSurveyor/");
                    if (!fileDir.exists()){
                        fileDir.mkdir();
                        Log.v("Start..","make dir");
                    };
                    File file = new File(fileDir, newJob+".csv");
                    FileOutputStream stream = new FileOutputStream(file);
                    try {
                        stream.write(content.getBytes());
                    } finally {
                        stream.close();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    public void onSlide(){
        this.mainPanel.showSlideMenuView();
//        Toast.makeText(this, "Slide Successfully!", Toast.LENGTH_SHORT).show();
    }
    EditText _ed;ImageButton _b1;ImageButton _b2;

//    public void onEnableJobEdit(EditText ed,ImageButton b1,ImageButton b2){
//        _ed=ed;_b1=b1;_b2=b2;
//    }
    private void SwitchJobEdit(boolean flag){
        if (flag){
            _b1.getDrawable().clearColorFilter();
            _b2.getDrawable().clearColorFilter();
        }else{
            _b1.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.CLEAR);
            _b2.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.CLEAR);
        }
        _ed.setEnabled(flag);
        _b1.setEnabled(flag);
        _b2.setEnabled(flag);
    }


    public void FragmentSwitch(String fragmentName) {
        switch (fragmentName)
        {
            case "job":
                if (currentFragment!=existingJobsFragment)
                {
                    ft = fm.beginTransaction();
                    ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                    ft.replace(R.id.center_frame, existingJobsFragment, "ExistingJobsFragment");
                    ft.commit();
                    currentFragment=existingJobsFragment;
//                    File f=getFilesDir();
//                    File[] fs=f.listFiles();
//                    for (int i=0;i<fs.length;i++){
//                        if (fs[i].getName().endsWith("csv")) {
//
//
//                        }
//                    }
//
//                    for
//                    ArrayList<SensorDataStructure> sds=new ArrayList<SensorDataStructure>();
//                    sensorDatabase.add

                    SwitchJobEdit(true);
                }
                break;
            case "angle":
                if (currentFragment != angleSensorFragment) {
                    ft = fm.beginTransaction();
                    ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                    ft.replace(R.id.center_frame, angleSensorFragment,"AngleSensorFragment");
                    ft.commit();
                    currentFragment=angleSensorFragment;
                    SwitchJobEdit(false);
                }
                break;
        }

//        if (newFragment != currentFragment){
//            ft.replace(currentFragment.getId(), newFragment);
//            currentFragment = newFragment;
//            ft.commit();
//        }
    }
    public  void onCallSampling(int database_ind){
        currentJobIndicator=database_ind;
        currentJobName=existingJobsFragment.existing_job_list.get(currentJobIndicator).getTask();
        EditText et=(EditText)findViewById(R.id.myEditText);
        et.setText(currentJobName);

        samplingFragment.ImportDatabase(sensorDatabase.get(currentJobIndicator),currentJobIndicator);

        ft = fm.beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.replace(R.id.center_frame, samplingFragment, "SamplingFragment");
        ft.commit();
//        mainPanel.setTextView();

        currentFragment=samplingFragment;
//        String check_result=sensorInquiry.CheckSensor();
//        if  (check_result!="done")
//        {
//            Toast.makeText(this,check_result, Toast.LENGTH_SHORT).show();
//        }
        SwitchJobEdit(false);

//        txv=(TextView) findViewById(R.id.samplingHead);

//        handler = new Handler();
//        SamplingItem headItem=samplingFragment.sampling_list.get(0);

//        samplingFragment.sampling_item_adapter.notifyDataSetChanged();


    }

    public void onLoadingDatabase(ArrayList<ArrayList<SensorDataStructure>>sdb){
        sensorDatabase=sdb;
    }

    public void onDestroySamplingView(){
        Log.v("s",getFilesDir().toString());
        SaveDatabase(currentJobName);

    }


    public void onDeleteSampling(int sampling_position,int position){
        sensorDatabase.get(sampling_position).remove(position);
    }

    public void onRecordingSampling(){
//        if (timing-System.currentTimeMillis()>30000000){
//            SaveDatabase(currentJobName);
//        }
        samplingFragment.ModifyDatabase(sensorDatabase.get(currentJobIndicator));
        tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400);
//        timing=System.currentTimeMillis();

    }
//    public void onAngleCalculated(SensorDataStructure sds){
//        if (samplingFragment.sampling_list.size()>0){
//            Log.v("sdf","dsfc2!");
//            samplingFragment.sampling_item_adapter.getItem(0).Modify(sds.toString(),sds.ToDate());
////                sampling_item_adapter.notifyDataSetChanged();
//        }
//    }

//    public

    private boolean CheckJobName(String jn){
        File file=new File(getFilesDir(),jn+".csv");
        if (file.exists()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle("File exist!")
                    .setMessage("Please rename your job!")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("OK", null)
                    .show();
            return false;
        }
        return true;
    }


    public void DeleteAllJobs(){
        File f=getFilesDir();
        File[] fs=f.listFiles();
//        f.delete();
        for (int i=fs.length-1;i>=0;i--){
            if (fs[i].getName().endsWith("csv")) {
                fs[i].delete();
            }
        }
    }
    private boolean ExistingJobs(ArrayList<ExistingJob> job_list, ExistingJob job){
        if (job == null) {
            return false;
        } else {
            for (int i=0;i<job_list.size();i++){
                if (job.task.equals(job_list.get(i).task))
                    return true;
            }
        }
        return false;
    }


    private FragmentManager fm;
    private SlideMenuFragment slideMenuFragment;
    private ExistingJobsFragment existingJobsFragment;
    public SamplingFragment samplingFragment;
    private Fragment currentFragment;

    private SensorManager sensorManager;
    private Timer saveTimer;

    private MainPanel mainPanel;

    private FragmentTransaction ft;
    private AngleSensorGLFragment angleSensorFragment;
    private ArrayList<ArrayList<SensorDataStructure>> sensorDatabase;
    private int currentJobIndicator;
    private String currentJobName;
    private final static int SAVE_RECORD_INTERVAL=10;
    private ToneGenerator tg;
//    Vibrator vbr;
//    SensorDataStructure sds;
//    private TextView txv;
//    public Handler handler;
    private DecimalFormat d = new DecimalFormat("#.##");
//    private SensorInquiry sensorInquiry;

//    public SensorManager sensorManager;



//    private AlertDialog.Builder dlgAlert;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        _ed = (EditText) findViewById(R.id.myEditText);
        _b1 = (ImageButton) findViewById(R.id.myJobEdit);
        _b2 = (ImageButton) findViewById(R.id.myJobAdd);

//        txv=(TextView) findViewById(R.id.samplingHead);
//        if (txv!=null){
//            Log.v("sd","dsaf");
//        }

//        handler = new Handler();
//        vbr=(Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
        tg=new ToneGenerator(AudioManager.STREAM_ALARM,100);
        sensorDatabase=new ArrayList<ArrayList<SensorDataStructure>>();
//        SensorDataStructure newRecord=new SensorDataStructure(0.0f,0.0f,0.0f,0.0f,new Date());
//        ArrayList<SensorDataStructure> newList=new ArrayList<SensorDataStructure>();
//        newList.add(newRecord);
//        sensorDatabase.add(newList);
//        Log.v("t", "cnm!");
        mainPanel=new MainPanel(this);
        mainPanel = (MainPanel) findViewById(R.id.MainPanel);
        mainPanel.setSlideMenuView(getLayoutInflater().inflate(R.layout.left_frame, null));
//        mainPanel.setRecordingMenuView(getLayoutInflater().inflate(R.layout.bottom_frame,null));
//        mainPanel.setHeadView(getLayoutInflater().inflate(R.layout.head_frame, null));
        mainPanel.setCenterView(getLayoutInflater().inflate(R.layout.center_frame, null));

//        mainPanel.setCenterView(getLayoutInflater().inflate(R.layout.center_frame, null));
//        mainPanel.addViews(getLayoutInflater().inflate(R.layout.slide_menu_fragment, null),getLayoutInflater().inflate(R.layout.existing_job_fragment,null));


        fm=this.getSupportFragmentManager();
        ft = fm.beginTransaction();

        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        slideMenuFragment = new SlideMenuFragment();
        ft.replace(R.id.left_frame, slideMenuFragment, "SlideMenuFragment");
//        ft.addToBackStack(null);
//        ft.commitAllowingStateLoss();
//        ft.add(slideMenuFragment, "SlideMenuFragment");

//        Log.v("t", "cnm1!");
        existingJobsFragment=new ExistingJobsFragment();
//        ft.add(existingJobsFragment, "ExistingJobsFragment");
//        ft.replace(R.id.center_frame, existingJobsFragment, "ExistingJobsFragment");
//        currentFragment=existingJobsFragment;
//        SwitchJobEdit(true);
//        Log.v("t", "cnm2!");
        angleSensorFragment=new AngleSensorGLFragment();
        ft.replace(R.id.center_frame, angleSensorFragment, "AngleSensorFragment");
        currentFragment=angleSensorFragment;
        SwitchJobEdit(false);
        ft.commit();

        samplingFragment=new SamplingFragment();
//        txv=(TextView) findViewById(R.id.samplingHead);

//        sensorInquiry=new SensorInquiry(this);
//        dlgAlert  = new AlertDialog.Builder(this);
//        builder.setTitle("App Title");
//        builder.setMessage("This is an alert with no consequence");
        saveTimer=new Timer();
        saveTimer.scheduleAtFixedRate(new SaveTimerTask(), 500,30000);


        DeleteAllJobs();

    }


    @Override
    public void onBackPressed(){
        ExitActivity();
        super.onBackPressed();

    }
    private void ExitActivity(){
//        Log.v("p","Pressed!");

//        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
//        samplingFragment.SaveDatabase(currentJobName);

        if (currentFragment==samplingFragment){
            ft = fm.beginTransaction();
            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            ft.replace(R.id.center_frame, existingJobsFragment, "ExistingJobsFragment");
            ft.commit();
            currentFragment=existingJobsFragment;
            SwitchJobEdit(true);
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                .setTitle("Exit")
                .setMessage("Are you sure?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Yes button clicked, do something
                        finish();
                    }
                })
                .setNegativeButton("No", null)						//Do nothing on no
                .show();

        }
    }

    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                //do something
//                if (currentFragment==samplingFragment){
//                    if (samplingFragment.textView.getText().toString()!="Head"){
//                        samplingFragment.sampling_list.add(0, new SamplingItem(convertTextViewToString(samplingFragment.textView.getText().toString()), new Date()));
////                    Log.v("SDFf",samplingFragment.sampling_list.get(0).toString());
//                        samplingFragment.sampling_item_adapter.notifyDataSetChanged();
//                        samplingFragment.ModifyDatabase(sensorDatabase.get(currentJobIndicator));
//                        tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,400);
////                        vbr=(Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
////                        vbr.vibrate(300);
//                    }
//
//                }
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                //do something
                if (currentFragment==samplingFragment){
                    SaveDatabase(currentJobName);
                }
                break;
            case KeyEvent.KEYCODE_BACK:
                //do something
                ExitActivity();
            case KeyEvent.KEYCODE_MENU:
                //do something
            case KeyEvent.KEYCODE_HOME:
                //invalid...

        }
        return true;
                //super.onKeyDown (keyCode, event);

    }

    class SaveTimerTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (currentFragment==samplingFragment){
                        SaveDatabase(currentJobName);
                    }
                }
            });
        }
    }
    public boolean SaveDatabase(String fileName){

        String content="date,azimuth,pitch,roll,light\r\n";
        for (int i=0;i<samplingFragment.sampling_list.size();i++){
            String st=samplingFragment.sampling_list.get(i).toString();
            st=st.replace(")","),");
            content+=st;
            content+="\r\n";
        }
        try{
            String sdcard_path = Environment.getExternalStorageDirectory().getAbsolutePath();
            File fileDir = new File(sdcard_path+"/AngleSurveyor/");
            if (!fileDir.exists()){
                fileDir.mkdir();
                Log.v("Start..","make dir");
            };
            File file = new File(fileDir, fileName+".csv");
//            Log.v("ssa",fileName+".csv");
            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(content.getBytes());
            } finally {
                stream.close();
            }
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Save failed!", Toast.LENGTH_SHORT).show();
            return false;
        }
        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
        tg.startTone(ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE, 400);
        return true;

    }
//    public String convertTextViewToString(String tvs){
////        Log.v("daf",tvs);
//        String[] str=tvs.split("\r\n");
//        return str[0].substring(("azimuth=").length())+","+str[1].substring(("pitch=").length())+","+str[2].substring(("roll=").length())+","
//                +str[3].substring(("azimuth_std=").length())+","+str[4].substring(("pitch_std=").length())+","+str[5].substring(("roll_std=").length())+","
//                +str[6].substring(("light=").length());
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//        Log.d("fd","ere");
        //noinspection SimplifiableIfStatement
//        if (id==R.id.home)
//            ExitActivity();

//        if (id == R.id.action_settings) {
//            return true;
//        }


        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onPause() {
        super.onPause();
        if (angleSensorFragment._GLView!=null){
            angleSensorFragment._GLView.onPause();
        }
        if (currentFragment==samplingFragment)
            samplingFragment.StopListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (angleSensorFragment._GLView!=null){
            angleSensorFragment._GLView.onResume();
        }
        if (currentFragment==samplingFragment)
            samplingFragment.InitListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister sensor listeners to prevent the activity from draining the device's battery.
        if (currentFragment==samplingFragment)
            samplingFragment.StopListener();
    }



}
