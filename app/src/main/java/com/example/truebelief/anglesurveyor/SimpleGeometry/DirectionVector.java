package com.example.truebelief.anglesurveyor.SimpleGeometry;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by truebelief on 2015/6/27.
 */
public class DirectionVector {
    byte[] PrincipleVector;

    public DirectionVector(){
    }

    private int _program;
    static final int COORDS_PER_VERTEX = 3;
    static private float _vectorCoords[] = {
            0f ,0f, 0f,
            0f ,1.0f, 0f,
            0f,0f,1.0f,
    };
    static  short[] _drawOrder = new short[] {
            0,1,
            0,2,
    };

    public final int _vertexStride = COORDS_PER_VERTEX * 4;
    float _color[] = { 0.709803922f,0.2f,0.898039216f, 1.0f };

    private int _positionHandle;
    private int _colorHandle;

    private FloatBuffer _vertexBuffer;
    private ShortBuffer _drawListBuffer;

    public void init(int program){

        this._program=program;
        ByteBuffer bb = ByteBuffer.allocateDirect(
                this._vectorCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        this._vertexBuffer = bb.asFloatBuffer();
        this._vertexBuffer.put(this._vectorCoords);
        this._vertexBuffer.position(0);
        ByteBuffer dlb = ByteBuffer.allocateDirect(_drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());

        _drawListBuffer = dlb.asShortBuffer();
        _drawListBuffer.put(_drawOrder);
        _drawListBuffer.position(0);

    }

    public void draw(){
        this._positionHandle = GLES20.glGetAttribLocation(this._program, "vPosition");

        GLES20.glEnableVertexAttribArray(this._positionHandle);
        GLES20.glVertexAttribPointer(
                this._positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                this._vertexStride, this._vertexBuffer);

        this._colorHandle = GLES20.glGetUniformLocation(this._program, "vColor");

        GLES20.glLineWidth(8.0f);
        GLES20.glUniform4fv(this._colorHandle, 1, this._color, 0);
        // Draw the square
        GLES20.glDrawElements(
                GLES20.GL_LINES, _drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, _drawListBuffer);

        GLES20.glDrawElements(
                GLES20.GL_LINES, _drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, _drawListBuffer);
        GLES20.glDrawElements(
                GLES20.GL_LINES, _drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, _drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(_positionHandle);

    }



}
