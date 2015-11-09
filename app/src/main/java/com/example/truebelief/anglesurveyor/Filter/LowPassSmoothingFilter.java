package com.example.truebelief.anglesurveyor.Filter;

/**
 * Created by truebelief on 2015/7/13.
 */
public class LowPassSmoothingFilter {
    private float timeConstant = 0.18f;
    private float alpha = 0.9f;
    private float dt = 0;
    private static final float NS2S = 1.0f / 1000000000.0f;


    private float timestamp = System.nanoTime();
    private float startTime = 0;

    private int count = 0;

    private float[] output = new float[]
            { 0, 0, 0 };

    public float[] ApplyFilter(float[] input)
    {
        if (startTime == 0)
        {
            startTime = System.nanoTime();
        }

        timestamp = System.nanoTime();

        dt = 1 / (count++ / ((timestamp - startTime) *NS2S));

        alpha = timeConstant / (timeConstant + dt);

        if (count > 5)
        {
            output[0] = alpha * output[0] + (1 - alpha) * input[0];
            output[1] = alpha * output[1] + (1 - alpha) * input[1];
            output[2] = alpha * output[2] + (1 - alpha) * input[2];
        }

        return output;
    }

    public void setTimeConstant(float timeConstant)
    {
        this.timeConstant = timeConstant;
    }

    public void reset()
    {
        startTime = 0;
        timestamp = 0;
        count = 0;
        dt = 0;
        alpha = 0;
    }


}
