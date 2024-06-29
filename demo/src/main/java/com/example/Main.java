package com.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import io.agora.rtc.*;

public class Main {
    static {
        SDK.load();
    }

    private static final String APP_ID = System.getenv("APP_ID");
    private static final String TOKEN = System.getenv("TOKEN");
    private static final String CHANNEL_NAME = "demo_channel";
    private static final String UID = ""; // blank is ok
    private static final String VIDEO_FILE_PATH = "jane_no_speak.h264";

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
            ExecutorService executorService = Executors.newFixedThreadPool(2);
            rtcConn.registerObserver(new DefaultRtcConnObserver() {
                @Override
                public void onConnected(AgoraRtcConn agora_rtc_conn, RtcConnInfo conn_info, int reason) {
                    System.out.println("Connected");
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            streamVideo(videoFrameSender);
                        }
                    });
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            streamAudio(audioFrameSender);
                        }
                    });
                }
            });

            // Join the channel
            result = rtcConn.connect(TOKEN, CHANNEL_NAME, UID);
            System.out.println("Connected result: " + result);
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

    interface IAudioStreamer {
        int sendAudioPcmData(byte[] frameBuf, int captureTimestamp, int samplesPer10ms, int bytesPerSample, int numOfChannels, int sampleRate);
    }
    public static void streamAudio(AgoraAudioPcmDataSender sender) {
        String inputFilePath = "send_audio_16k_1ch.pcm";
        AudioReader audioReader = new AudioReader();
        AudioOptions options = new AudioOptions();
        options.audioFile = inputFilePath;
        options.interval = 100;
        options.numOfChannels = 1;
        options.sampleRate = 16000;

        IAudioStreamer audioStreamer = new IAudioStreamer() {
            @Override
            public int sendAudioPcmData(byte[] frameBuf, int captureTimestamp, int samplesPer10ms, int bytesPerSample, int numOfChannels, int sampleRate) {
                int result = sender.send(frameBuf, captureTimestamp, samplesPer10ms, bytesPerSample, numOfChannels, sampleRate);
                // StringBuilder hexString = new StringBuilder();
                // for (byte sample : frameBuf) {
                //    hexString.append(String.format("%02X ", sample));
                // }
                // System.out.println(hexString.toString());
                return result;
            }
        };

        audioReader.sampleSendAudioTask(options, audioStreamer, new AtomicBoolean());
        // audioReader.sendUsingByteBuffer(options, audioStreamer, new AtomicBoolean());
    }

    public static void streamVideo(AgoraVideoEncodedImageSender sender) {
        String inputFilePath = VIDEO_FILE_PATH;
        H264Reader.readH264(inputFilePath, true, (dataInfo) -> {
            EncodedVideoFrameInfo info = new EncodedVideoFrameInfo();
            info.setCodecType(Constants.VIDEO_CODEC_H264);
            // info.setRotation(Constants.VIDEO_ORIENTATION_0);
            info.setFramesPerSecond(30);
            info.setFrameType(dataInfo.isKeyFrame ? Constants.VIDEO_FRAME_TYPE_KEY_FRAME : Constants.VIDEO_FRAME_TYPE_DELTA_FRAME);
            int result = sender.send(dataInfo.data, dataInfo.data.length, info);
            

            // StringBuilder hexString = new StringBuilder();
            // for (byte b : dataInfo.data) {
            //     hexString.append(String.format("%02X ", b));
            // }            // System.out.println(hexString.toString());
            
            try {
                Thread.sleep(1000/30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
    }
}
