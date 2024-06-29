package com.example;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import com.example.Main.IAudioStreamer;

public class AudioReader {

    static BufferedInputStream bufferInputStream = null;

    private void sendOnePcmFrame(AudioOptions options, IAudioStreamer sender) {
        String fileName = options.audioFile;

        // Calculate byte size for 10ms audio samples
        int sampleSize = Short.BYTES * options.numOfChannels; // sizeof(int16_t) in bytes is 2
        int samplesPer10ms = options.sampleRate / 1000 * options.interval;
        int sendBytes = sampleSize * samplesPer10ms;

        try {
            if (bufferInputStream == null) {
                bufferInputStream = new BufferedInputStream(new FileInputStream(fileName));
                System.out.println("Open audio file " + fileName + " successfully");
            }

            byte[] frameByteArray = new byte[sendBytes];

            if (bufferInputStream.read(frameByteArray) == -1) {
                if (bufferInputStream.available() == 0) {
                    bufferInputStream.close();
                    bufferInputStream = null;
                    System.out.println("End of audio file");
                } else {
                    System.err.println("Error reading audio data");
                }
                return;
            }

            // Assuming you have a method sendPcmFrame in your audioFrameSender to send
            // frameBuf
            sender.sendAudioPcmData(frameByteArray, 0, samplesPer10ms, sampleSize, options.numOfChannels,
                    options.sampleRate);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sampleSendAudioTask(AudioOptions options, IAudioStreamer audioFrameSender, AtomicBoolean exitFlag) {
        // Currently only 10 ms PCM frame is supported. So PCM frames are sent at 10 ms
        // interval
        long intervalMs = options.interval;
        // long nextSendTime = System.currentTimeMillis();

        while (!exitFlag.get()) {
            long startTime = System.currentTimeMillis();
            sendOnePcmFrame(options, audioFrameSender);
            long endTime = System.currentTimeMillis();
            long sleepTime = intervalMs - (endTime - startTime);
            if (sleepTime > 0) {
                try {
                    // System.out.println("Sleeping for " + sleepTime + " ms");
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            // nextSendTime += intervalMs;
        }
    }

    public void sendUsingByteBuffer(AudioOptions options, IAudioStreamer audioFrameSender, AtomicBoolean exitFlag) {
        String filePath = "send_audio_16k_1ch.pcm";
        long intervalMs = options.interval;
        int sampleSize = Short.BYTES * options.numOfChannels; // sizeof(int16_t) in bytes is 2
        int sampleCountPerInterval = options.sampleRate / 1000 * options.interval;
        int dataSize = sampleSize * sampleCountPerInterval;
        int sampleRate = options.sampleRate;


        try (FileInputStream fis = new FileInputStream(filePath);
             FileChannel fileChannel = fis.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate((int)fileChannel.size());
            fileChannel.read(buffer);
            buffer.flip();
            while (true) {
                long startTime = System.currentTimeMillis();
                if (buffer.remaining() < dataSize) {
                    buffer.rewind();
                }
                byte[] data = new byte[dataSize];
                buffer.get(data);
                audioFrameSender.sendAudioPcmData(data, 0, sampleCountPerInterval, sampleSize, options.numOfChannels, sampleRate);
                long endTime = System.currentTimeMillis();
                long sleepTime = intervalMs - (endTime - startTime);
                if (sleepTime > 0) {
                    try {
                        // System.out.println("Sleeping for " + sleepTime + " ms");
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void readAudioUsingJavaCV(AudioOptions options, IAudioStreamer audioFrameSender, AtomicBoolean exitFlag) {
        String audioFile = "send_audio_16k_1ch.pcm";

        // Create a grabber object
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(audioFile);
        grabber.setFormat("s16le");
        grabber.setAudioChannels(1);
        grabber.setSampleRate(16000);
        grabber.setAudioCodec(avcodec.AV_CODEC_ID_PCM_S16LE);

        int sampleSize = Short.BYTES  * options.numOfChannels; // sizeof(int16_t) in bytes is 2
        int sampleRate = options.sampleRate;

        try {
            // Start the grabber
            grabber.start();

            // Loop to read audio frames
            AVPacket packet;
            while (true) {
                packet = grabber.grabPacket();
                if (packet == null) {
                    grabber.restart();
                    continue;
                }
                int dataSize = packet.size();
                ByteBuffer dataBuffer = packet.data().capacity(dataSize).asByteBuffer();
                byte[] dataBytes = new byte[dataSize];
                dataBuffer.get(dataBytes);

                // Send audio frame
                audioFrameSender.sendAudioPcmData(dataBytes, 0, 640, sampleSize, options.numOfChannels, sampleRate);
                // System.out.println("A " + index +  " size: " + dataSize);
                Thread.sleep(40);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                grabber.stop();
                grabber.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
