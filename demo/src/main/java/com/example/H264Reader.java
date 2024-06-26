package com.example;

import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class H264Reader {

    public static void readH264(String filePath, boolean loop, Consumer<DataInfo> action) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(filePath)) {
            grabber.start();
    
            AVPacket packet;
            int index = 0;
            while (true) {
                packet = grabber.grabPacket();
                if (packet == null) {
                    if (loop) {
                        grabber.restart();
                        index = 0;
                        continue;
                    } else {
                        break;
                    }
                }
    
                int dataSize = packet.size();
                ByteBuffer dataBuffer = packet.data().capacity(dataSize).asByteBuffer();
                byte[] dataBytes = new byte[dataSize];
                dataBuffer.get(dataBytes);
                boolean isKeyFrame = (packet.flags() & avcodec.AV_PKT_FLAG_KEY) != 0;
                DataInfo dataInfo = new DataInfo(index, dataBytes, isKeyFrame);
                action.accept(dataInfo);
                index++;
            }
    
            grabber.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}


