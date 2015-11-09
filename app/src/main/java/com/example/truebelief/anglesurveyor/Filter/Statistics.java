package com.example.truebelief.anglesurveyor.Filter;

import java.util.ArrayList;

/**
 * Created by truebelief on 2015/7/21.
 */
public class Statistics {

    public static double std(double[] list){
        double sum=0;
        double sum_sq=0;
        for(int i=0;i<list.length;i++){
            sum+=list[i];
            sum_sq+=list[i]*list[i];
        }
        return  Math.sqrt((sum_sq-sum*sum/list.length)/(list.length-1));
    }
    public static float std(float[] list){
        double sum=0;
        double sum_sq=0;
        for(int i=0;i<list.length;i++){
            sum+=list[i];
            sum_sq+=list[i]*list[i];
        }
        return  (float )Math.sqrt((sum_sq-sum*sum/list.length)/(list.length-1));
    }

    public static float std(ArrayList<Float> list){
        double sum=0;
        double sum_sq=0;
        for(int i=0;i<list.size();i++){
            sum+=list.get(i);
            sum_sq+=list.get(i)*list.get(i);
        }
        return  (float )Math.sqrt((sum_sq-sum*sum/list.size())/(list.size()-1));
    }

    public static double stdsq(double[] list){
        double sum=0;
        double sum_sq=0;
        for(int i=0;i<list.length;i++){
            sum+=list[i];
            sum_sq+=list[i]*list[i];
        }
        return  (sum_sq-sum*sum/list.length)/(list.length-1);
    }

    public static float stdsq(float[] list){
        double sum=0;
        double sum_sq=0;
        for(int i=0;i<list.length;i++){
            sum+=list[i];
            sum_sq+=list[i]*list[i];
        }
        return  (float) (sum_sq-sum*sum/list.length)/(list.length-1);
    }
}
