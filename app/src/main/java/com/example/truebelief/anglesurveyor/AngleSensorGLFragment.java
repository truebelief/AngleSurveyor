package com.example.truebelief.anglesurveyor;


import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.truebelief.anglesurveyor.Sensing.SensorDataStructure;
import com.example.truebelief.anglesurveyor.Sensing.SensorInquiry;
import com.example.truebelief.anglesurveyor.SimpleGeometry.Axis;
import com.example.truebelief.anglesurveyor.SimpleGeometry.DirectionVector;
import com.example.truebelief.anglesurveyor.SimpleGeometry.TestSquare;
import com.example.truebelief.anglesurveyor.SimpleGeometry.TestTriangle;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by truebelief on 2015/6/27.
 */

public class AngleSensorGLFragment extends Fragment {

    public GLSurfaceView _GLView;
    public AngleSensorGLRenderer _GLRenderer;
    private Timer fuseTimer = new Timer();
    public SensorInquiry sensorInquiry;
    private TextView txv;
    private Timer timer;
//    private float[] rotAngles;
    private float[] _rotateAngles=new float[]{0.0f,0.0f,0.0f};

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        _GLRenderer =new AngleSensorGLRenderer();
        timer=new Timer();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);

        View view=inflater.inflate(R.layout.angle_fragment,null);
//        _GLView=(GLSurfaceView)view.findViewById(R.id.angleGL);

        FrameLayout l=(FrameLayout)view.findViewById(R.id.angleGL);

        _GLView=new GLSurfaceView(getActivity());
        _GLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        _GLView.setEGLContextClientVersion(2);

//        _GLView.setRenderer(_GLRenderer);
        _GLView.setRenderer(_GLRenderer);

        l.addView(_GLView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

//        container.addView(this._GLView);
//        _GLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
//        this.getActivity().setContentView(view);

        txv=(TextView)view.findViewById(R.id.angleText);


        sensorInquiry=new SensorInquiry(getActivity());
//        sensorInquiry=new SensorInquiry(getActivity(),txv,_rotateAngles);


        setTimer();


        return  view;


    }

    public void setTimer(){
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Your logic here...

                // When you need to modify a UI element, do so on the UI thread.
                // 'getActivity()' is required as this is being ran from a Fragment.
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // This code will always run on the UI thread, therefore is safe to modify UI elements.
                        if (sensorInquiry.sds!=null){
//                            Log.v("Sensor","Working!");
                            txv.setText(sensorInquiry.sds.toFormattedString());
                        }
                    }
                });
            }
        }, 0, 200); // End of your timer code.
    }


    @Override
    public void onActivityCreated(Bundle mState)
    {
        super.onActivityCreated(mState);

        //setting your renderer here causes onSurfaceCreated to be called
        //if you set your renderer here then you have a context to load resources

    }

    @Override
    public void onPause() {

//        sensorInquiry.StopListener(fuseTimer);
        super.onPause();
        timer.cancel();
//        sensorInquiry.positionInquiry.Cancel();
    }

    @Override
    public void onResume() {
//        sensorInquiry=new SensorInquiry(getActivity(),txv);
        sensorInquiry.InitListener(fuseTimer);

        setTimer();
        super.onResume();
    }

    @Override
    public void onStop() {

//        sensorInquiry.StopListener(fuseTimer);
        super.onStop();
        timer.cancel();
//        sensorInquiry.positionInquiry.Cancel();
    }


    public class AngleSensorGLRenderer  implements GLSurfaceView.Renderer{

        private static final String TAG = "AngleSensorGLRenderer";


        // number of coordinates per vertex in this array
        final int COORDS_PER_VERTEX = 3;
        final float[] zeroRotate=new float[]{0,0,0};

        private final float[] _MVPMatrix = new float[16];
//        public final float[] _rotateAngles=new float[3];
        private final float[] _projectionMatrix = new float[16];
        private final float[] _viewMatrix = new float[16];
        private final float[] _rotationMatrix = new float[16];

        private int _positionHandle;
        private int _colorHandle;
        private int _MVPMatrixHandle;

        TestTriangle _triangle;
        TestSquare _square;
        Axis _axis;
        DirectionVector _dir;

        private final String _vertexShaderCode =
                // This matrix member variable provides a hook to manipulate
                // the coordinates of the objects that use this vertex shader
                "uniform mat4 uMVPMatrix;" +
                        "attribute vec4 vPosition;" +
                        "void main() {" +
                        // The matrix must be included as a modifier of gl_Position.
                        // Note that the uMVPMatrix factor *must be first* in order
                        // for the matrix multiplication product to be correct.
                        "  gl_Position = uMVPMatrix * vPosition;" +
                        "}";

        private final String _fragmentShaderCode =
                "uniform vec4 vColor;" +
                        "void main() {" +
                        "  gl_FragColor = vColor;" +
                        "}";

        private  int _program;



        public AngleSensorGLRenderer(){

        }
        @Override
        public void onSurfaceCreated(GL10 unused,EGLConfig config){
//            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            int vertexShader=loadShader(GLES20.GL_VERTEX_SHADER, this._vertexShaderCode);
            int fragmentShader=loadShader(GLES20.GL_FRAGMENT_SHADER, this._fragmentShaderCode);

            this._program= GLES20.glCreateProgram();
            GLES20.glAttachShader(this._program, vertexShader);   // add the vertex shader to program
            GLES20.glAttachShader(this._program, fragmentShader); // add the fragment shader to program
            GLES20.glLinkProgram(this._program);                  // create OpenGL program executables
//        GLES20.glDisable(GL10.GL_DITHER);
//        GLES20.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
//            GLES20.glShadeModel(GL10.GL_SMOOTH);
//        GLES20.glEnable(GL10.GL_DEPTH_TEST);
//        GLES20.glDepthFunc(GL10.GL_LEQUAL);


//        this._triangle=new TestTriangle();
//        this._triangle.init(this._program);
//        this._square=new TestSquare();
//        this._square.init(this._program);
            this._axis=new Axis();
            this._axis.init(this._program);
            this._dir=new DirectionVector();
            this._dir.init(this._program);

        }
        @Override
        public void onDrawFrame(GL10 gl){

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            GLES20.glUseProgram(this._program);

            Matrix.setLookAtM(this._viewMatrix, 0, 2.0f, 2.0f, 2.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
            Matrix.multiplyMM(this._MVPMatrix, 0, _projectionMatrix, 0, this._viewMatrix, 0);

            this._MVPMatrixHandle = GLES20.glGetUniformLocation(this._program, "uMVPMatrix");
//        checkGlError("glGetUniformLocation");
            GLES20.glUniformMatrix4fv(this._MVPMatrixHandle, 1, false, this._MVPMatrix, 0);
//        checkGlError("glUniformMatrix4fv");

//        this._triangle.draw();
//        this._square.draw();
            this._dir.draw();
//            Log.v("sf0", "fk repeat!!");
//
//            if (sensorInquiry.sds!=null){
////                Log.v("sf1","fk here!!");
//            }
//            sensorInquiry.calculateFusedOrientation();
            sensorInquiry.calculateSensingData();
            //az (z),pitch (x),roll (y)
            if (sensorInquiry.sds!=null){
                _rotateAngles[0]=sensorInquiry.sds.angle_x;
                _rotateAngles[1]=sensorInquiry.sds.angle_y;
                _rotateAngles[2]=sensorInquiry.sds.angle_z;
            }

            if (_rotateAngles!=zeroRotate) {
//                Matrix.rotateM(this._MVPMatrix, 0, (float)(-Math.asin(Math.cos(_rotateAngles[1] * 3.1415926 / 180) / Math.sin(_rotateAngles[2] * 3.1415926 / 180))*180/3.1415926), 0.0f, 0.0f, 1.0f); //azimuth
//                Matrix.rotateM(this._MVPMatrix, 0, (float)(-Math.asin(Math.cos(_rotateAngles[1]*3.1415926/180)/Math.sin(_rotateAngles[2]*3.1415926/180))*180/3.1415926), 0.0f, 0.0f, 1.0f); //azimuth
//                Log.v("rt",String.valueOf((float)(-Math.asin(Math.cos(_rotateAngles[1]*3.1415926/180)/Math.sin(_rotateAngles[2] * 3.1415926 / 180)))*));

                Matrix.rotateM(this._MVPMatrix, 0, -(_rotateAngles[0]), 0.0f, 0.0f, 1.0f); //azimuth
                Matrix.rotateM(this._MVPMatrix, 0, -(_rotateAngles[1]), 1.0f, 0.0f, 0.0f); //pitch
                Matrix.rotateM(this._MVPMatrix, 0, -_rotateAngles[2], 0.0f, 1.0f, 0.0f); //roll
            }
//        Matrix.multiplyMM(this._MVPMatrix, 0, _projectionMatrix, 0, this._viewMatrix, 0);

            this._MVPMatrixHandle = GLES20.glGetUniformLocation(this._program, "uMVPMatrix");
//        checkGlError("glGetUniformLocation");
            GLES20.glUniformMatrix4fv(this._MVPMatrixHandle, 1, false, this._MVPMatrix, 0);
            this._axis.draw();

        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height){

            GLES20.glViewport(0, 0, width, height);
            float ratio = (float) width / height;
            Matrix.frustumM(_projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        }

        public void checkGlError(String glOperation) {
            int error;
            while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
                Log.e(TAG, glOperation + ": glError " + error);
                throw new RuntimeException(glOperation + ": glError " + error);
            }
        }


        public int loadShader(int type, String shaderCode){
            int shader = GLES20.glCreateShader(type);
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);

            return shader;
        }


    }



}
