package com.example;

public class DataInfo {
    public DataInfo(int index, byte[] data, boolean isKeyFrame ) {
        this.index = index;
        this.data = data;
        this.isKeyFrame = isKeyFrame;
    }
    public int index = 0;
    public byte[] data = null;
    public boolean isKeyFrame;
}