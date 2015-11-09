package com.example.truebelief.anglesurveyor.SimpleGeometry;

import android.opengl.GLES20;

//import com.example.truebelief.anglesurveyor.AngleSensorGLRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by truebelief on 2015/6/27.
 */
public class TestTriangle {
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float _triangleCoords[] = {
            // in counterclockwise order:
            0.0f,  0.622008459f, 0.0f,   // top
            -0.5f, -0.311004243f, 0.0f,   // bottom left
            0.5f, -0.311004243f, 0.0f    // bottom right
    };
    public final int _vertexCount = _triangleCoords.length / COORDS_PER_VERTEX;
    public final int _vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private int _program;
    float _color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 0.0f };

    private int _positionHandle;
    private int _colorHandle;
//    private int _MVPMatrixHandle;
    private FloatBuffer _vertexBuffer;
//    private final ShortBuffer _drawListBuffer;


    public TestTriangle(){

    }
    public void init(int program){
        this._program=program;
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                this._triangleCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        this._vertexBuffer = bb.asFloatBuffer();
        this._vertexBuffer.put(this._triangleCoords);
        this._vertexBuffer.position(0);

    }
    public void draw(){
        this._positionHandle = GLES20.glGetAttribLocation(this._program, "vPosition");

        GLES20.glEnableVertexAttribArray(this._positionHandle);
        GLES20.glVertexAttribPointer(
                this._positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                this._vertexStride, this._vertexBuffer);
        this._colorHandle = GLES20.glGetUniformLocation(this._program, "vColor");
        GLES20.glUniform4fv(this._colorHandle, 1, this._color, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, this._vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(_positionHandle);

    }
}
