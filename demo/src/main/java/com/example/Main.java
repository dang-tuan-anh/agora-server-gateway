package com.example;

import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.VideoDimensions;
import io.agora.rtc.AgoraLocalVideoTrack;
import io.agora.rtc.AgoraVideoFrameSender;
import io.agora.rtc.VideoEncoderConfig;

public class Main {
    static {
        System.load("/usr/local/lib/libagora_rtc_sdk.so"); // Ensure the native library is loaded
    }

    private static final String APP_ID = "YOUR_APP_ID";
    private static final String APP_CERTIFICATE = "YOUR_APP_CERTIFICATE";
    private static final String CHANNEL_NAME = "YOUR_CHANNEL_NAME";
    private static final String UID = "12345"; // Set your UID or use 0 for automatic assignment

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
            long cptr = 0;
            // Create and configure local video track
            AgoraVideoFrameSender videoFrameSender = new AgoraVideoFrameSender(cptr);
            AgoraLocalVideoTrack localVideoTrack = agoraService.createCustomVideoTrackFrame(videoFrameSender);

            // Set video encoder configuration
            int codecType = 2;
            VideoDimensions dimensions = new VideoDimensions(640, 360);
            int frameRate = 24;
            int bitrate = 0;
            int minBitrate = 0;
            int orientationMode = 0;
            int degradationPreference = 0;
            int mirrorMode = 0;
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
}
