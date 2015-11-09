package com.example.truebelief.anglesurveyor.SimpleGeometry;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by truebelief on 2015/6/27.
 */
public class TestSquare {
    static final int COORDS_PER_VERTEX = 3;
    static float _squareCoords[] = {
            -0.5f,  0.5f, 0.0f,   // top left
            -0.5f, -0.5f, 0.0f,   // bottom left
            0.5f, -0.5f, 0.0f,   // bottom right
            0.5f,  0.5f, 0.0f }; // top right
    private final short _drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

//    public final int _vertexCount = squareCoords.length / COORDS_PER_VERTEX;
    public final int _vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private int _program;
//    float _color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 0.0f };
    float _color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

    private int _positionHandle;
    private int _colorHandle;
    //    private int _MVPMatrixHandle;
    private FloatBuffer _vertexBuffer;
    private ShortBuffer _drawListBuffer;


    public TestSquare(){

    }
    public void init(int program){
        this._program=program;
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                this._squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        this._vertexBuffer = bb.asFloatBuffer();
        this._vertexBuffer.put(this._squareCoords);
        this._vertexBuffer.position(0);
        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                _drawOrder.length * 2);
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


        GLES20.glUniform4fv(this._colorHandle, 1, this._color, 0);
        // Draw the square
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, _drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, _drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(_positionHandle);

    }
}
