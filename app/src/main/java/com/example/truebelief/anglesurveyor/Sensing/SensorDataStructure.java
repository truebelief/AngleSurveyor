package com.example.truebelief.anglesurveyor.Sensing;

import android.util.Log;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by truebelief on 2015/6/29.
 */
public class SensorDataStructure {
    public float light;
    public float angle_x;
    public float angle_y;
    public float angle_z;
    public float angle_x_std;
    public float angle_y_std;
    public float angle_z_std;
//    public float position_x;
//    public float position_y;
//    public float position_z;


    public Date date;
    public boolean isEmpty;
    private DecimalFormat d=new DecimalFormat("###.#####");;
    public SensorDataStructure(){
        isEmpty=true;
    }
//    public SensorDataStructure(float ang_x,float ang_y,float ang_z,float lt,float x,float y,float z,Date dt){
    public SensorDataStructure(float ang_x,float ang_y,float ang_z,float ang_x_std,float ang_y_std,float ang_z_std,float lt,Date dt){
        angle_x=ang_x;
        angle_y=ang_y;
        angle_z=ang_z;
        angle_x_std=ang_x_std;
        angle_y_std=ang_y_std;
        angle_z_std=ang_z_std;
//        position_x=x;
//        position_y=y;
//        position_z=z;
        light=lt;
        date=dt;
        isEmpty=false;
    }

    public SensorDataStructure(String str){
        DateFormat df=new SimpleDateFormat("HH:mm:ss dd/MM/yy", Locale.CANADA);
        String dates=str.substring(1, 17);
        try {
            date=df.parse(dates);
        }catch(Exception e){
            e.printStackTrace();
        }
        String[] rec=str.substring(19).split(",");
//        Log.v("dasf",str);
        angle_x=Float.parseFloat(rec[0]);
        angle_y=Float.parseFloat(rec[1]);
        angle_z=Float.parseFloat(rec[2]);

//        position_x=Float.parseFloat(rec[3]);
//        position_y=Float.parseFloat(rec[4]);
//        position_z=Float.parseFloat(rec[5]);
        angle_x_std=Float.parseFloat(rec[3]);
        angle_y_std=Float.parseFloat(rec[4]);
        angle_z_std=Float.parseFloat(rec[5]);

//        light=Float.parseFloat(rec[3]);
        light=Float.parseFloat(rec[6]);
    }
//    @Override
//    public String toString(){
//        return String.valueOf(angle_x)+","+
//        String.valueOf(angle_y)+","+
//        String.valueOf(angle_z)+","+
//        String.valueOf(light);
//    }
//    @Override
    public String toString(){
        return d.format(angle_x)+","+
                d.format(angle_y)+","+
                d.format(angle_z)+","+
//                d.format(position_x)+","+
//                d.format(position_y)+","+
//                d.format(position_z)+","+
                d.format(angle_x_std)+","+
                d.format(angle_y_std)+","+
                d.format(angle_z_std)+","+
                d.format(light);
    }
    public String toFormattedString(){
        return "azimuth="+d.format(angle_x)+"\r\n"+"pitch="+d.format(angle_y)+"\r\n"+"roll="+d.format(angle_z)+"\r\n"+
//                "x="+d.format(position_x)+"\r\n"+"y="+d.format(position_y)+"\r\n"+"z="+d.format(position_z)+"\r\n"+
                "azimuth_std="+d.format(angle_x_std)+"\r\n"+"pitch_std="+d.format(angle_y_std)+"\r\n"+"roll_std="+d.format(angle_z_std)+"\r\n"+
                "light="+d.format(light);
    }

    public Date ToDate(){
        return date;
    }
    public void notifyEmpty(){
       isEmpty=true;
    }

}
