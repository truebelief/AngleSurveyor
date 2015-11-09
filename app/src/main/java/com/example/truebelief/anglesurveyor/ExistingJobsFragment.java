package com.example.truebelief.anglesurveyor;

/**
 * Created by truebelief on 2015/6/23.
 */

//import android.app.ListFragment;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.truebelief.anglesurveyor.Sensing.SensorDataStructure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExistingJobsFragment extends ListFragment {
    public ArrayList<ExistingJob> existing_job_list;
    public ExistingJobAdapter existing_job_adapter;
    private onExistingJobFragmentListener onExistingJobFragment;
    private ArrayList<ArrayList<SensorDataStructure>> sdb;

//    public boolean isVisible;
    public ExistingJobsFragment(){
        existing_job_list=new ArrayList<ExistingJob>();
        sdb=new ArrayList<ArrayList<SensorDataStructure>>();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.existing_job_fragment, null);
        return view;
    }
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
//        File f=new File("/sdcard");
//        existing_job_list.clear();
//        existing_job_list=new;
//        File f=getActivity().getFilesDir();
//        File[] fs=f.listFiles(fl);
//        File[] fs=f.listFiles();

        String sdcard_path = Environment.getExternalStorageDirectory().getAbsolutePath();
        File fileDir = new File(sdcard_path+"/AngleSurveyor/");
        if (!fileDir.mkdir()){
            Log.v("Failed!","make dir");
        };
        File[] fs=fileDir.listFiles();

        sdb.clear();
        existing_job_list.clear();
        for (int i=0;i<fs.length;i++){
            if (fs[i].getName().endsWith("csv")) {
                existing_job_list.add(new ExistingJob(fs[i].getName().replace(".csv", ""), new Date(fs[i].lastModified())));
                StringBuilder stb=new StringBuilder();
                try {
                    BufferedReader br=new BufferedReader(new FileReader(fs[i]));
                    String line;
                    ArrayList<SensorDataStructure> sds=new ArrayList<SensorDataStructure>();
                    br.readLine();
                    while((line=br.readLine())!=null){
                        line=line.replace("),",")");
                        sds.add(new SensorDataStructure(line));
                    }
                    sdb.add(sds);
                    br.close();
                }catch (Exception e){
                    e.printStackTrace();
                }


            }
        }
        onExistingJobFragment.onLoadingDatabase(sdb);
        existing_job_adapter=new ExistingJobAdapter(getActivity(),R.layout.existing_job_fragment, existing_job_list);
        setListAdapter(existing_job_adapter);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try {
//            onNewJobAddedListenerInstance = (onNewJobAddedListener)activity;
//            onSlideListenerInstance=(onSlideListener)activity;
            onExistingJobFragment=(onExistingJobFragmentListener) activity;

        }catch (ClassCastException e){
            throw new ClassCastException(activity.toString()+"must implement onCreateJobFragmentListener");
        }
    }


    public void onDestroyView(){
        super.onDestroyView();
        setListAdapter(null);
    }


    public class ExistingJobAdapter extends ArrayAdapter<ExistingJob> {
        int resource;
        public ExistingJobAdapter(Context context,int resource,List<ExistingJob> jobs){
            super(context,resource,jobs);
            this.resource=resource;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent){

            RelativeLayout existingjobView;

            ExistingJob job=getItem(position);
            String taskString=job.getFormattedTask();
            Date createDate=job.getCreated();
            SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss dd/MM/yy");
            String dateString=sdf.format(createDate);

            if (convertView == null){
                existingjobView=new RelativeLayout(getContext());
                String inflater=Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater li;
                li=(LayoutInflater) getContext().getSystemService(inflater);
                li.inflate(resource,existingjobView,true);

            }else{
                existingjobView=(RelativeLayout)convertView;
            }

            TextView dateView=(TextView)existingjobView.findViewById(R.id.rowDate);
            TextView taskView=(TextView)existingjobView.findViewById(R.id.row);
            dateView.setText(dateString);
            taskView.setText(taskString);

            taskView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    return false;
                }
            });
            taskView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onExistingJobFragment.onCallSampling(position);
                }
            });


            final Button delete_button=(Button) existingjobView.findViewById(R.id.rowExpand);
            delete_button.setText("X");
            delete_button.setVisibility(View.VISIBLE);
            delete_button.setTextColor(existingjobView.getResources().getColor(R.color.job_default_color));//"#BB777777"
            delete_button.setBackgroundColor(existingjobView.getResources().getColor(R.color.job_background_color));//"#BBEEEEEE"
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder
                                    .setTitle("Caution!")
                                    .setMessage("File delete?")
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
//                                            Log.v("df", existing_job_list.get(position).getTask());
                                            try {
                                                String sdcard_path = Environment.getExternalStorageDirectory().getAbsolutePath();
                                                File fileDir = new File(sdcard_path + "/AngleSurveyor/");
                                                if (!fileDir.mkdir()) {
                                                    Log.v("Failed!", "make dir");
                                                }
                                                ;
                                                File file = new File(fileDir, existing_job_list.get(position).getTask() + ".csv");
                                                Log.v("df",existing_job_list.get(position).getTask() + ".csv");
                                                if (!file.delete()) {
                                                    Log.v("d","failed delete");
                                                } ;
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            existing_job_list.remove(position);
                                            existing_job_adapter.notifyDataSetChanged();
                                            Log.v("d", String.valueOf(sdb.size()));
                                            Log.v("d", String.valueOf(position));
                                            sdb.remove(position);
                                            Log.v("d", String.valueOf(sdb.size()));
                                            onExistingJobFragment.onLoadingDatabase(sdb);
                                        }
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .show();

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

            return existingjobView;
        }
    }

    public interface onExistingJobFragmentListener{
        public void onCallSampling(int database_ind);
        public void onLoadingDatabase(ArrayList<ArrayList<SensorDataStructure>>sdb);
    }

}


