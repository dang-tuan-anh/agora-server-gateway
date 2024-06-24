package com.example;

public class SampleOptions {
    String audioFile;
    Audio audio;
    int interval;

    static class Audio {
        int numOfChannels;
        int sampleRate;
    }
}