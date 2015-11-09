package com.example.truebelief.anglesurveyor;

import android.util.Log;
import android.widget.ImageButton;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by truebelief on 2015/6/24.
 */
public class ExistingJob {
    String task;
    Date created;
    public String getTask(){
        return task;
    }
    public String getFormattedTask(){
        return task;
    }

    public Date getCreated(){
        return created;
    }
    public  ExistingJob(String _task){
        this(_task, new Date(java.lang.System.currentTimeMillis()));
    }
    public ExistingJob(String _task, Date _created){
        task=_task;
        created=_created;
    }
    @Override
    public String toString(){
        SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss dd/MM/yy");
        String dateString=sdf.format(created);
        return "("+dateString+")"+task;
    }


}
