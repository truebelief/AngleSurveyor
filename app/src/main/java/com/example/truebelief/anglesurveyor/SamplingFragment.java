package com.example.truebelief.anglesurveyor;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.truebelief.anglesurveyor.Sensing.SensorDataStructure;
import com.example.truebelief.anglesurveyor.Sensing.SensorInquiry;
import com.example.truebelief.anglesurveyor.Sensing.SensorListening;
//import com.example.truebelief.anglesurveyor.Sensing.SensorInquiry;
//import com.example.truebelief.anglesurveyor.Sensing.SensorInquiry;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by truebelief on 2015/6/28.
 */
public class SamplingFragment extends ListFragment {

    private SensorListening sensorInquiry;
    public ArrayList<SamplingItem> sampling_list;
    public int sampling_list_position;
    public SamplingItemAdapter sampling_item_adapter;
//    public SamplingItem sampling0;
    public Activity _activity;

    //    DecimalFormat d = new DecimalFormat("#.##");
    private Timer fuseTimer;
    private onSamplingFragmentListener onSamplingFragmentListenerInstance;

    public TextView textView;
    public ImageButton recordingButton;
    public RelativeLayout headerView;
//    public View footView;
    //    public boolean isVisible;
    public SamplingFragment(){

//        sampling0=new SamplingItem("123,123,123,123",new Date());
        sampling_list=new ArrayList<SamplingItem>();

//        sampling_list.add(sampling0);
    }

    public boolean ImportDatabase(ArrayList<SensorDataStructure> sampling_database,int sampling_database_position){
        sampling_list_position=sampling_database_position;

//        sampling_list=new ArrayList<SamplingItem>();
//        sampling_list.add(0, sampling0);
        sampling_list.clear();

        if(sampling_database.size()<1){
            return false;
        }
        if(sampling_database.get(0).isEmpty){
            return false;
        }
        for (int i=0;i<sampling_database.size();i++){
            SamplingItem spi=new SamplingItem(sampling_database.get(i).toString(),sampling_database.get(i).ToDate());
            sampling_list.add(spi);
        }

        return true;
    }

    public boolean ModifyDatabase(ArrayList<SensorDataStructure> sampling_database){
//            Log.v("d2", String.valueOf(sampling_database.get(0).toString()));
        sampling_database.clear();
            for (int i = 0; i < sampling_list.size(); i++) {
                sampling_database.add(sampling_list.get(i).ToStructure());
            }
        return true;
    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sampling_fragment, null);
        headerView=(RelativeLayout)inflater.inflate(R.layout.sampling_header, null);

//        footView=inflater.inflate(R.layout.sampling_footer, null);
        textView=(TextView)headerView.findViewById(R.id.samplingHead);
        recordingButton=(ImageButton)headerView.findViewById(R.id.samplingRecording);
        recordingButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        ImageButton vb = (ImageButton) v;
                        vb.setColorFilter(v.getResources().getColor(R.color.job_hover_color));
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                        // Your action here on button click
                        sampling_list.add(0, new SamplingItem(convertTextViewToString(textView.getText().toString()), new Date()));
                        sampling_item_adapter.notifyDataSetChanged();
                        onSamplingFragmentListenerInstance.onRecordingSampling();
//                        vbr=(Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
//                        vbr.vibrate(300);
                    case MotionEvent.ACTION_CANCEL: {
                        ImageButton vb = (ImageButton) v;
                        vb.clearColorFilter();
                        v.invalidate();
                        break;
                    }
                }
                return false;
            }
        });

        return view;
    }
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
//        setListAdapter(null);
        if (headerView!=null){
            getListView().addHeaderView(headerView);

        }
//        if (footView!=null){
//            getListView().addHeaderView(footView);
//        }
        sensorInquiry=new SensorListening(_activity,textView);
        fuseTimer = new Timer();
        InitListener();
        sampling_item_adapter=new SamplingItemAdapter(getActivity(),R.layout.sampling_fragment, sampling_list);
        setListAdapter(sampling_item_adapter);
    }
    public void onDestroyView(){
        super.onDestroyView();
        onSamplingFragmentListenerInstance.onDestroySamplingView();
        setListAdapter(null);
        StopListener();

    }



    public void InitListener(){
        sensorInquiry.InitListener(fuseTimer);
    }
    public void StopListener(){
        sensorInquiry.StopListener(fuseTimer);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
                onSamplingFragmentListenerInstance = (onSamplingFragmentListener) activity;
            _activity=activity;
//                sensorInquiry=new SensorInquiry(_activity);
//                InitListener();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " attach error");
        }
    }
        @Override
        public void onDetach() {
            super.onDetach();
            try {
//                StopListener();
            } catch (ClassCastException e) {
                throw new ClassCastException("detach error");
            }
        }


        public class SamplingItemAdapter extends ArrayAdapter<SamplingItem> {
        int resource;
        public SamplingItemAdapter(Context context,int resource,List<SamplingItem> items){
            super(context,resource,items);
            this.resource=resource;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            RelativeLayout samplingitemView;


            SamplingItem item=getItem(position);
            String taskString=item.getFormattedRecord();
            Date createDate=item.getCreated();
//                Log.v("d",taskString);
//                Log.v("d2",item.toString());
            SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss dd/MM/yy");
            String dateString=sdf.format(createDate);

            if (convertView == null){
                samplingitemView=new RelativeLayout(getContext());
                String inflater=Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater li;
                li=(LayoutInflater) getContext().getSystemService(inflater);
                li.inflate(resource,samplingitemView,true);

            }else{
                samplingitemView=(RelativeLayout)convertView;
            }

//            if (position==0){
//
//                txv=(TextView)samplingitemView.findViewById(R.id.samplingHead);
//
//                sensorInquiry=new SensorListening(_activity,txv);
////                    txv.setText("Head");
//                txv.setVisibility(View.VISIBLE);
//                TextView dateView=(TextView)samplingitemView.findViewById(R.id.samplingDate);
//                dateView.setVisibility(View.INVISIBLE);
//                TextView recordView=(TextView)samplingitemView.findViewById(R.id.samplingRecord);
//                recordView.setVisibility(View.INVISIBLE);
//                Button delete_button=(Button) samplingitemView.findViewById(R.id.samplingDelete);
//                delete_button.setVisibility(View.INVISIBLE);
//                InitListener();
//
//                return samplingitemView;
//            }
            TextView dateView=(TextView)samplingitemView.findViewById(R.id.samplingDate);
            TextView recordView=(TextView)samplingitemView.findViewById(R.id.samplingRecord);
            dateView.setText(dateString);
//                dateView.setTextColor(samplingitemView.getResources().getColor(R.color.job_default_color));//"#BB777777"
//                dateView.setBackgroundColor(samplingitemView.getResources().getColor(R.color.job_background_color));//"#BBEEEEEE"
            recordView.setText(taskString);

            recordView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    return false;
                }
            });
            recordView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                        onExistingJobFragment.onCallSampling(position);
                }
            });



            final Button delete_button=(Button) samplingitemView.findViewById(R.id.samplingDelete);
            delete_button.setText("X");
            delete_button.setVisibility(View.VISIBLE);
            delete_button.setTextColor(samplingitemView.getResources().getColor(R.color.job_default_color));//"#BB777777"
            delete_button.setBackgroundColor(samplingitemView.getResources().getColor(R.color.job_background_color));//"#BBEEEEEE"
            delete_button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            Button vb = (Button) v;
                            vb.setTextColor(v.getResources().getColor(R.color.job_hover_color));
                            v.invalidate();
                            break;
                        }
                        case MotionEvent.ACTION_UP:
                            // Your action here on button click
                            sampling_list.remove(position);
                            sampling_item_adapter.notifyDataSetChanged();
                            onSamplingFragmentListenerInstance.onDeleteSampling(sampling_list_position,position);
                        case MotionEvent.ACTION_CANCEL: {
                            Button vb = (Button) v;
                            vb.setTextColor(v.getResources().getColor(R.color.job_default_color));
                            v.invalidate();
                            break;
                        }
                    }
                    return false;
                }
            });

            return samplingitemView;
        }
}

public interface onSamplingFragmentListener{
    public void onDestroySamplingView();
    public void onDeleteSampling(int sampling_list_position,int position);
    public void onRecordingSampling();
}

public String convertTextViewToString(String tvs){
//        Log.v("daf",tvs);
        String[] str=tvs.split("\r\n");
        return str[0].substring(("azimuth=").length())+","+str[1].substring(("pitch=").length())+","+str[2].substring(("roll=").length())+","
                +str[3].substring(("azimuth_std=").length())+","+str[4].substring(("pitch_std=").length())+","+str[5].substring(("roll_std=").length())+","
                +str[6].substring(("light=").length());
    }



}


