package com.example;

import java.io.FileInputStream;
import java.nio.ByteBuffer;

import io.agora.rtc.*;

public class Main {
    static {
        SDK.load();
    }

    private static final String APP_ID = "0b775ae52b9341baa9b46812b3b5cbae";
    private static final String TOKEN = "007eJxTYDiSemkN+8x3lgo71jJ4rZ0UmnDsV93SS+uDgkzfh82+sNtNgcEgydzcNDHV1CjJ0tjEMCkx0TLJxMzC0CjJOMk0OSkxVfpddlpDICOD1Kw4FkYGCATxeRhSUnPz45MzEvPyUnMYGADa1CPx";
    private static final String CHANNEL_NAME = "demo_channel";
    private static final String UID = "12345"; // Set your UID or use 0 for automatic assignment
    private static final String VIDEO_FILE_PATH = "send_video.h264";

    public static void main(String[] args) {
        System.out.println("App started");
        try {
            // Initialize AgoraService
            AgoraServiceConfig serviceConfig = new AgoraServiceConfig();
            serviceConfig.setAppId(APP_ID);
            serviceConfig.setAreaCode(0);
            AgoraService agoraService = new AgoraService();
            agoraService.initialize(serviceConfig);

            // Create RTC connection
            RtcConnConfig rtcConnConfig = new RtcConnConfig();
            AgoraRtcConn rtcConn = agoraService.agoraRtcConnCreate(rtcConnConfig);

            // Join the channel
            rtcConn.connect(TOKEN, CHANNEL_NAME, UID);
            
            // Create and configure local video track
            AgoraMediaNodeFactory factory = agoraService.createMediaNodeFactory();
            AgoraVideoFrameSender videoFrameSender = factory.createVideoFrameSender();
            AgoraLocalVideoTrack localVideoTrack = agoraService.createCustomVideoTrackFrame(videoFrameSender);

            // Set video encoder configuration
            VideoDimensions dimensions = new VideoDimensions(640, 360);
            int codecType = Constants.VIDEO_CODEC_H264;
            int frameRate = 24;
            int bitrate = 0;
            int minBitrate = 0;
            int orientationMode = Constants.VIDEO_ORIENTATION_0;
            int degradationPreference = 0;
            int mirrorMode = Constants.VIDEO_MIRROR_MODE_AUTO;
            VideoEncoderConfig config = new VideoEncoderConfig(
                codecType,
                dimensions,
                frameRate,
                bitrate,
                minBitrate,
                orientationMode,
                degradationPreference,
                mirrorMode
            );
            localVideoTrack.setVideoEncoderConfig(config);

            // Publish the local video track
            rtcConn.getLocalUser().publishVideo(localVideoTrack);

            // Read and send video frames from the H.264 file
            // publishVideoFromH264(videoFrameSender, VIDEO_FILE_PATH);

            // Leave the channel after use
            rtcConn.disconnect();
            agoraService.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void publishVideoFromH264(AgoraVideoFrameSender videoFrameSender, String videoFilePath) {
        try (FileInputStream fis = new FileInputStream(videoFilePath)) {
            byte[] frameData = new byte[1024 * 1024]; // Adjust frame size if necessary
            int bytesRead;

            while ((bytesRead = fis.read(frameData)) != -1) {
                ByteBuffer buffer = ByteBuffer.allocateDirect(bytesRead);
                buffer.put(frameData, 0, bytesRead);
                buffer.flip();

                // Create ExternalVideoFrame
                ExternalVideoFrame externalVideoFrame = new ExternalVideoFrame();
                externalVideoFrame.setFormat(Constants.EXTERNAL_VIDEO_FRAME_PIXEL_FORMAT_RGBA);
                externalVideoFrame.setStride(640); // Width of the video frame
                externalVideoFrame.setHeight(360); // Height of the video frame
                externalVideoFrame.setTimestamp(System.currentTimeMillis()); // Timestamp of the frame
                externalVideoFrame.setBuffer(buffer); // ByteBuffer containing video frame data
                externalVideoFrame.setRotation(0); // Rotation of the video frame

                // Send the frame to Agora
                int result = videoFrameSender.send(externalVideoFrame);
                if (result != 0) {
                    System.err.println("Failed to send video frame: " + result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
