package com.example.truebelief.anglesurveyor.SimpleGeometry;

import java.nio.ByteBuffer;

/**
 * Created by truebelief on 2015/6/27.
 */
public class WiredCube {
    public WiredCube(){

    }
    public void init(){
        float x=0.05f,y=0.025f,z=0.01f;
        float[] lineVertices = new float[] {
                -x, -y, -z,//0
                x, -y,-z,//1
                x,y,-z,//2
                -x,y,-z,//3
                -x,y,z,//4
                -x,-y,z,//5
                x,-y,z,//6
                x,y,z,//7
        };
        byte[] lineFacets = new byte[]{
                0,1,
                0,3,
                0,4,
                1,2,
                1,5,
                2,3,
                2,6,
                3,7,
                4,5,
                4,7,
                5,6,
                6,7
        };

//        lineVerticesBuffer = floatBufferUtil(lineVertices);
//        xyzVerticesBuffer = floatBufferUtil(xyzVertices);
//
//        lineFacetsBuffer = ByteBuffer.wrap(lineFacets);
//        PrincipleVectorBuffer = ByteBuffer.wrap(PrincipleVector);
//        XFacetsBuffer = ByteBuffer.wrap(XFacets);
//        YFacetsBuffer = ByteBuffer.wrap(YFacets);
//        ZFacetsBuffer = ByteBuffer.wrap(ZFacets);
    }

}
