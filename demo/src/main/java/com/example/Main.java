package com.example;

import io.agora.rtc.*;

public class Main {
    static {
        SDK.load();
    }

    private static final String APP_ID = System.getenv("APP_ID");
    private static final String TOKEN = System.getenv("TOKEN");
    private static final String CHANNEL_NAME = "demo_channel";
    private static final String UID = ""; // blank is ok
    private static final String VIDEO_FILE_PATH = "output.h264";

    public static void main(String[] args) {
        System.out.println("App started");
        try {
            // Initialize AgoraService
            AgoraServiceConfig serviceConfig = new AgoraServiceConfig();
            serviceConfig.setAppId(APP_ID);
            serviceConfig.setEnableAudioDevice(0);
            serviceConfig.setEnableAudioProcessor(1);
            serviceConfig.setEnableVideo(1);
            AgoraService agoraService = new AgoraService();
            agoraService.setLogFilter(Constants.LOG_FILTER_INFO);
            agoraService.initialize(serviceConfig);

            // Create RTC connection
            RtcConnConfig rtcConnConfig = new RtcConnConfig();
            rtcConnConfig.setAutoSubscribeAudio(0);
            rtcConnConfig.setAutoSubscribeVideo(0);
            rtcConnConfig.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
            AgoraRtcConn rtcConn = agoraService.agoraRtcConnCreate(rtcConnConfig);
            AgoraMediaNodeFactory factory = agoraService.createMediaNodeFactory();
            
            // Create and configure local audio track
            AgoraAudioPcmDataSender audioFrameSender = factory.createAudioPcmDataSender();
            AgoraLocalAudioTrack localAudioTrack = agoraService.createCustomAudioTrackPcm(audioFrameSender);
            localAudioTrack.setEnabled(1);
            // // Set audio encoder configuration
            // int sampleRate = 48000;
            // int channelCount = 2;
            // int bitRate = 64000;
            
            
            // Create and configure local video track
            AgoraVideoEncodedImageSender videoFrameSender = factory.createVideoEncodedImageSender();
            AgoraLocalVideoTrack localVideoTrack = agoraService.createCustomVideoTrackEncoded(videoFrameSender, new SenderOptions());
            int codecType = Constants.VIDEO_CODEC_H264;
            int frameRate = 30;
            int orientationMode = Constants.VIDEO_ORIENTATION_0;
            VideoEncoderConfig config = new VideoEncoderConfig();
            config.setCodecType(codecType);
            // config.setDimensions(dimensions);
            config.setFrameRate(frameRate);
            config.setOrientationMode(orientationMode);
            localVideoTrack.setVideoEncoderConfig(config);

            int result = 0;
            result = rtcConn.getLocalUser().publishAudio(localAudioTrack);
            System.out.println("Publish audio result: " + result);
            result = rtcConn.getLocalUser().publishVideo(localVideoTrack);
            System.out.println("Publish result: " + result);
           
            rtcConn.registerObserver(new DefaultRtcConnObserver() {
                @Override
                public void onConnected(AgoraRtcConn agora_rtc_conn, RtcConnInfo conn_info, int reason) {
                    System.out.println("Connected");
                    stream(videoFrameSender);
                }
            });

            // Join the channel
            result = rtcConn.connect(TOKEN, CHANNEL_NAME, UID);
            System.out.println("Conneted result: " + result);
            Thread.sleep(1000 * 60 * 5); // wait for 5 minutes
            // Leave the channel after use
            // rtcConn.disconnect();
            // agoraService.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("App ended");

        }
    }

    public static void stream(AgoraVideoEncodedImageSender sender) {
        String inputFilePath = VIDEO_FILE_PATH;
        H264Reader.readH264(inputFilePath, true, (dataInfo) -> {
            EncodedVideoFrameInfo info = new EncodedVideoFrameInfo();
            info.setCodecType(Constants.VIDEO_CODEC_H264);
            // info.setRotation(Constants.VIDEO_ORIENTATION_0);
            info.setFramesPerSecond(30);
            info.setFrameType(dataInfo.isKeyFrame ? Constants.VIDEO_FRAME_TYPE_KEY_FRAME : Constants.VIDEO_FRAME_TYPE_DELTA_FRAME);
            int result = sender.send(dataInfo.data, dataInfo.data.length, info);
            System.out.println(dataInfo.index + " send result: " + result + " size: " + dataInfo.data.length + " key: " + dataInfo.isKeyFrame);

            // StringBuilder hexString = new StringBuilder();
            // for (byte b : dataInfo.data) {
            //     hexString.append(String.format("%02X ", b));
            // }            // System.out.println(hexString.toString());
            
            try {
                Thread.sleep(1000/30);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        });
    }
}
