package com.example.truebelief.anglesurveyor;

import android.util.Log;

import com.example.truebelief.anglesurveyor.Sensing.SensorDataStructure;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by truebelief on 2015/6/29.
 */
public class SamplingItem{

    String record;
    Date created;
    public String getRecord(){
        return record;
    }
    public String getFormattedRecord(){
        String[] st=record.split(",");
//        Log.v("d", task);
        if (st.length<=1) return record;
        return "azimuth="+st[0]+"\r\n"+"pitch="+st[1]+"\r\n"+"roll="+st[2]+"\r\n"+
//                "x="+st[3]+"\r\n"+"y="+st[4]+"\r\n"+"z="+st[5]+"\r\n"+"light="+st[6];
                "azimuth_std="+st[3]+"\r\n"+"pitch_std="+st[4]+"\r\n"+"roll_std="+st[5]+"\r\n"+"light="+st[6];
//                "light="+st[3];
    }
    public Date getCreated(){
        return created;
    }
    public SamplingItem(String _record){
        this(_record, new Date(java.lang.System.currentTimeMillis()));
    }
    public SamplingItem(String _record, Date _created){
        record=_record;
        created=_created;
    }
    public SamplingItem copy(){
        return new SamplingItem(record,created);
    }
    public void Modify(String _record, Date _created){
        record=_record;
        created=_created;
    }

    @Override
    public String toString(){
//        SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yy");
        SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss dd/MM/yy",Locale.CANADA);
        String dateString=sdf.format(created);
        return "("+dateString+")"+record;
    }
    public SensorDataStructure ToStructure(){
        String[] rds=record.split(",");
        return new SensorDataStructure(Float.parseFloat(rds[0]),Float.parseFloat(rds[1]),Float.parseFloat(rds[2]),
                Float.parseFloat(rds[3]),Float.parseFloat(rds[4]),Float.parseFloat(rds[5]),
                Float.parseFloat(rds[6]),created);
//                Float.parseFloat(rds[3]),created);

    }



}
