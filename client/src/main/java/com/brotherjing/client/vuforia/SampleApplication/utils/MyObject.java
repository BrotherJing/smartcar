package com.brotherjing.client.vuforia.SampleApplication.utils;

import java.nio.Buffer;

/**
 * Created by Brotherjing on 2015/10/4.
 */
public class MyObject extends MeshObject{

    private Buffer mVertBuff;
    private Buffer mTexCoordBuff;
    private Buffer mNormBuff;
    private Buffer mIndBuff;

    private int indicesNumber = 0;
    private int verticesNumber = 0;


    public MyObject()
    {
        setVerts();
        setTexCoords();
        setNorms();
        setIndices();
    }

    private void setVerts(){
        float myVertices[] = new float[]{
                -0.5f,-0.5f,0.0f,
                0.5f,-0.5f,0.0f,
                0.5f,0.5f,0.0f,
                -0.5f,0.5f,0.0f
        };
        mVertBuff = fillBuffer(myVertices);
        verticesNumber = myVertices.length/3;
    }

    private void setTexCoords(){
        float[] myTexs = new float[]{
                0.0f,0.0f,
                1.0f,0.0f,
                1.0f,1.0f,
                0.0f,1.0f
        };
        mTexCoordBuff = fillBuffer(myTexs);
    }

    private void setNorms(){
        float myNorms[] = new float[]{
                0.0f,0.0f,1.0f,
                0.0f,0.0f,1.0f,
                0.0f,0.0f,1.0f,
                0.0f,0.0f,1.0f
        };
        mNormBuff = fillBuffer(myNorms);
    }

    private void setIndices(){
        short myIndices[] = new short[]{
                0,1,2,
                2,3,0
        };
        mIndBuff = fillBuffer(myIndices);
        indicesNumber = myIndices.length;
    }

    @Override
    public Buffer getBuffer(BUFFER_TYPE bufferType) {
        switch (bufferType){
            case BUFFER_TYPE_VERTEX:
                return mVertBuff;
            case BUFFER_TYPE_TEXTURE_COORD:
                return mTexCoordBuff;
            case BUFFER_TYPE_NORMALS:
                return mNormBuff;
            case BUFFER_TYPE_INDICES:
                return mIndBuff;
            default:return null;
        }
    }

    @Override
    public int getNumObjectVertex() {
        return verticesNumber;
    }

    @Override
    public int getNumObjectIndex() {
        return indicesNumber;
    }
}
