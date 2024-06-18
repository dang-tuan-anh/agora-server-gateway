package com.example;

import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class FFMpegParser1 {

    public static void parse(String filePath,  Consumer<DataInfo> action) throws InterruptedException {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(filePath)) {
            grabber.start();

            AVPacket packet;
            int index = 0;
            while ((packet = grabber.grabPacket()) != null) {
                int dataSize = packet.size();
                ByteBuffer dataBuffer = packet.data().capacity(dataSize).asByteBuffer();
                byte dataBytes[] = new byte[dataSize];
                dataBuffer.get(dataBytes);
                boolean isKeyFrame = (packet.flags() & avcodec.AV_PKT_FLAG_KEY & avcodec.AV_PKT_FLAG_KEY) != 0;
                DataInfo dataInfo = new DataInfo(index, dataBytes, isKeyFrame);
                action.accept(dataInfo);
                index++;
                Thread.sleep(1000/30);
            }

            grabber.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] getFrameBytes(Frame frame) {
        int width = frame.imageWidth;
        int height = frame.imageHeight;
        int depth = frame.imageDepth;
        int channels = frame.imageChannels;
    
        // Calculate buffer size
        int bufferSize = width * height * channels * (depth / 8);
    
        // Get ByteBuffer from frame
        ByteBuffer buffer = (ByteBuffer) frame.image[0];
    
        // Ensure buffer has enough remaining bytes
        if (buffer.remaining() < bufferSize) {
            throw new RuntimeException("Buffer does not contain enough data for the frame");
        }
    
        // Create byte array to hold image data
        byte[] imageBytes = new byte[bufferSize];
    
        // Save current buffer position
        int currentPosition = buffer.position();
    
        try {
            // Read data from ByteBuffer into byte array
            buffer.get(imageBytes);
        } finally {
            // Restore buffer position to original
            buffer.position(currentPosition);
        }
    
        return imageBytes;
    }
}


