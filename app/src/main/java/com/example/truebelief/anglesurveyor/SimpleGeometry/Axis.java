package com.example.truebelief.anglesurveyor.SimpleGeometry;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by truebelief on 2015/6/27.
 */
public class Axis {
    private int _program;
    static final int COORDS_PER_VERTEX = 3;
    static float _axisCoords[] = {
            -0.1f ,0f, 0f,//0 X start
            0.5f ,0f, 0f,//1 X end
            0.4f,0.05f,0f,//2 X arrow1
            0.4f,-0.05f,0f,//3 X arrow2

            0f ,-0.1f , 0f,//4 Y start
            0f ,0.5f , 0f,//5 Y end
            0.05f ,0.4f ,0f,//6 Y arrow1
            -0.05f ,0.4f ,0f,//7 Y arrow2

            0f ,0f ,-0.1f,//8 Z start
            0f ,0f ,0.5f,//9 Z end
            0f ,0.05f ,0.4f,//10 Z arrow1
            0f ,-0.05f ,0.4f,//11 Z arrow2

            0.6f,0f,0.05f,//12 Label X
            0.6f,0.05f,0.0f,//13
            0.6f,-0.05f,0.1f,//14
            0.6f,0.05f,0.1f,//15
            0.6f,-0.05f,0.0f,//16

            0.1f,0.6f,0f,//17 Label Y
            0.1f,0.5f,0f,//18
            0.0f,0.7f,0f,//19
            0.2f,0.7f,0f,//20

            0.05f ,0.05f ,0.6f,//21 Label Z
            0.15f,0.05f,0.6f,//22
            0.05f,-0.05f,0.6f,//23
            0.15f,-0.05f,0.6f,//24


            0.05f,0f,0f,//25
            0.05f,0.05f,0f,//26
            0.05f,0f,0f,//27
            0.05f,0f,0.05f,//28

            0f,0.05f,0f,//29
            0.05f,0.05f,0f,//30
            0f,0.05f,0f,//31
            0f,0.05f,0.05f,//32

            0f,0f,0.05f,//33
            0.05f,0f,0.05f,//34
            0f,0f,0.05f,//35
            0f,0.05f,0.05f//36
    };
    static  short[] _drawOrder_XFacets = new short[] {
            0,1,

            1,2,
            1,3,

            12,13,
            12,14,
            12,15,
            12,16,

            25,26,
            27,28

    };
    static  short[] _drawOrder_YFacets = new short[] {

            4,5,

            5,6,
            5,7,

            17,18,
            17,19,
            17,20,

            29,30,
            31,32

    };

    static  short[] _drawOrder_ZFacets = new short[] {

            8,9,

            9,10,
            9,11,

            21,22,
            22,23,
            23,24,

            33,34,
            35,36
    };

//    private final short _drawOrder[] = { 0, 1, 2, 0, 2, 3 };
    public final int _vertexStride = COORDS_PER_VERTEX * 4;
    float _color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

    private int _positionHandle;
    private int _colorHandle;
//    private int _MVPMatrixHandle;
    private FloatBuffer _vertexBuffer;
    private ShortBuffer _drawListBuffer_x;
    private ShortBuffer _drawListBuffer_y;
    private ShortBuffer _drawListBuffer_z;

    public Axis(){

    }

    public void init(int program){

        this._program=program;
        ByteBuffer bb = ByteBuffer.allocateDirect(
                this._axisCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        this._vertexBuffer = bb.asFloatBuffer();
        this._vertexBuffer.put(this._axisCoords);
        this._vertexBuffer.position(0);
        // initialize byte buffer for the draw list
        ByteBuffer dlb_x = ByteBuffer.allocateDirect(
                _drawOrder_XFacets.length * 2);
        dlb_x.order(ByteOrder.nativeOrder());
        ByteBuffer dlb_y = ByteBuffer.allocateDirect(
                _drawOrder_YFacets.length * 2);
        dlb_y.order(ByteOrder.nativeOrder());
        ByteBuffer dlb_z = ByteBuffer.allocateDirect(
                _drawOrder_ZFacets.length * 2);
        dlb_z.order(ByteOrder.nativeOrder());

        _drawListBuffer_x = dlb_x.asShortBuffer();
        _drawListBuffer_x.put(_drawOrder_XFacets);
        _drawListBuffer_x.position(0);
        _drawListBuffer_y = dlb_y.asShortBuffer();
        _drawListBuffer_y.put(_drawOrder_YFacets);
        _drawListBuffer_y.position(0);
        _drawListBuffer_z = dlb_z.asShortBuffer();
        _drawListBuffer_z.put(_drawOrder_ZFacets);
        _drawListBuffer_z.position(0);



    }

    public void draw(){
        this._positionHandle = GLES20.glGetAttribLocation(this._program, "vPosition");

        GLES20.glEnableVertexAttribArray(this._positionHandle);
        GLES20.glVertexAttribPointer(
                this._positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                this._vertexStride, this._vertexBuffer);

        this._colorHandle = GLES20.glGetUniformLocation(this._program, "vColor");

        GLES20.glLineWidth(4.0f);
        GLES20.glUniform4fv(this._colorHandle, 1, this._color, 0);
        // Draw the square
        GLES20.glDrawElements(
                GLES20.GL_LINES, _drawOrder_XFacets.length,
                GLES20.GL_UNSIGNED_SHORT, _drawListBuffer_x);

        GLES20.glDrawElements(
                GLES20.GL_LINES, _drawOrder_YFacets.length,
                GLES20.GL_UNSIGNED_SHORT, _drawListBuffer_y);
        GLES20.glDrawElements(
                GLES20.GL_LINES, _drawOrder_ZFacets.length,
                GLES20.GL_UNSIGNED_SHORT, _drawListBuffer_z);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(_positionHandle);

    }

}
