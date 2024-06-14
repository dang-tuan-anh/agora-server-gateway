package com.example;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;

import io.agora.rtc.*;

public class Main {
    static {
        // System.loadLibrary("agora_rtc_sdk"); // Ensure the native library is loaded
        SDK.load();

    }

    private static final String APP_ID = "YOUR_APP_ID";
    private static final String APP_CERTIFICATE = "YOUR_APP_CERTIFICATE";
    private static final String CHANNEL_NAME = "YOUR_CHANNEL_NAME";
    private static final String UID = "12345"; // Set your UID or use 0 for automatic assignment
    private static final String VIDEO_FILE_PATH = "jane.mp4";

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

            // Generate token if necessary
            String token = generateToken(APP_ID, APP_CERTIFICATE, CHANNEL_NAME, UID);

            // Join the channel
            rtcConn.connect(token, CHANNEL_NAME, UID);
            // Create and configure local video track
            AgoraMediaNodeFactory factory = agoraService.createMediaNodeFactory();
            AgoraVideoFrameSender videoFrameSender = factory.createVideoFrameSender();
            // factory.createMediaPlayerSource(0)
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

            publishVideoFromMp4(videoFrameSender);
            // Leave the channel after use
            rtcConn.disconnect();
            agoraService.destroy();
            // agoraService.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String generateToken(String appId, String appCertificate, String channelName, String uid) {
        // Token generation logic here, similar to the previous example
        // This method should return a valid token
        return "YOUR_TOKEN";
    }

    private static void publishVideoFromMp4(AgoraVideoFrameSender videoFrameSender) {
        try {
            // Open the MP4 file
            File file = new File(VIDEO_FILE_PATH);
            FileInputStream fis = new FileInputStream(file);

            // Read and send each frame
            byte[] frameData = new byte[1024 * 1024]; // Adjust frame size if necessary
            int bytesRead;
            while ((bytesRead = fis.read(frameData)) != -1) {
                ByteBuffer buffer = ByteBuffer.wrap(frameData, 0, bytesRead);

                // Create ExternalVideoFrame
                ExternalVideoFrame externalVideoFrame = new ExternalVideoFrame();
                externalVideoFrame.setFormat(Constants.EXTERNAL_VIDEO_FRAME_PIXEL_FORMAT_RGBA);
                externalVideoFrame.setStride(640); // Width of the video frame
                externalVideoFrame.setHeight(360); // Height of the video frame
                externalVideoFrame.setTimestamp(System.currentTimeMillis()); // Timestamp of the frame
                externalVideoFrame.setBuffer(buffer); // Byte array containing video frame data
                externalVideoFrame.setRotation(0); // Rotation of the video frame

                // Send the frame to Agora
                videoFrameSender.send(externalVideoFrame);
            }

            // Close streams
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}