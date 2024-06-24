package com.example;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import io.agora.rtc.AgoraAudioPcmDataSender;

public class AudioStreamer {

    static FileInputStream file = null;

    public void sendOnePcmFrame(SampleOptions options, AgoraAudioPcmDataSender sender) {
        String fileName = options.audioFile;

        // Calculate byte size for 10ms audio samples
        int sampleSize = 2 * options.audio.numOfChannels; // sizeof(int16_t) in bytes is 2
        int samplesPer10ms = options.audio.sampleRate / 100;
        int sendBytes = sampleSize * samplesPer10ms;

        try {
            if (file == null) {
                file = new FileInputStream(fileName);
                System.out.println("Open audio file " + fileName + " successfully");
            }

            byte[] frameBuf = new byte[sendBytes];

            if (file.read(frameBuf) != sendBytes) {
                if (file.available() == 0) {
                    file.close();
                    file = null;
                    System.out.println("End of audio file");
                } else {
                    System.err.println("Error reading audio data");
                }
                return;
            }

            // Assuming you have a method sendPcmFrame in your audioFrameSender to send frameBuf
            sender.sendPcmFrame(frameBuf);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Example interface for audio data sender (adapt based on your actual interface)
    interface IAudioPcmDataSender {
        void sendPcmFrame(byte[] pcmFrame);
    }

    // Example class to hold sample options
    static class SampleOptions {
        AudioOptions audio;
        String audioFile;

        SampleOptions(AudioOptions audio, String audioFile) {
            this.audio = audio;
            this.audioFile = audioFile;
        }
    }

    // Example class to hold audio options
    static class AudioOptions {
        int numOfChannels;
        int sampleRate;

        AudioOptions(int numOfChannels, int sampleRate) {
            this.numOfChannels = numOfChannels;
            this.sampleRate = sampleRate;
        }
    }
}
