package com.example;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import io.agora.rtc.*;

public class Main {
    static {
        SDK.load();
    }

    private static final String APP_ID = "0b775ae52b9341baa9b46812b3b5cbae";
    private static final String TOKEN = "007eJxTYEio2p63LDFyru+mCS/dJgS2GSkZF/yTXd50rubznl93ZucqMBgkmZubJqaaGiVZGpsYJiUmWiaZmFkYGiUZJ5kmJyWm6ukUpjUEMjJ8qdvIxMgAgSA+D0NKam5+fHJGYl5eag4DAwDZ4yRr";
    private static final String CHANNEL_NAME = "demo_channel";
    private static final String UID = "12345"; // Set your UID or use 0 for automatic assignment
    private static final String VIDEO_FILE_PATH = "send_video.h264";

    public static void main(String[] args) {
        System.out.println("App started");
        try {
            // Initialize AgoraService
            AgoraServiceConfig serviceConfig = new AgoraServiceConfig();
            serviceConfig.setAppId(APP_ID);
            serviceConfig.setEnableVideo(1);
            // serviceConfig.setAreaCode(0);
            AgoraService agoraService = new AgoraService();
            agoraService.setLogFilter(Constants.LOG_FILTER_INFO);
            agoraService.initialize(serviceConfig);

            // Create RTC connection
            RtcConnConfig rtcConnConfig = new RtcConnConfig();
            rtcConnConfig.setAutoSubscribeAudio(0);
            rtcConnConfig.setAutoSubscribeVideo(0);
            rtcConnConfig.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);

            AgoraRtcConn rtcConn = agoraService.agoraRtcConnCreate(rtcConnConfig);
            
                    // Create and configure local video track
            AgoraMediaNodeFactory factory = agoraService.createMediaNodeFactory();
            AgoraVideoEncodedImageSender videoFrameSender = factory.createVideoEncodedImageSender();
            AgoraLocalVideoTrack localVideoTrack = agoraService.createCustomVideoTrackEncoded(videoFrameSender, new SenderOptions());

            // Set video encoder configuration
            // VideoDimensions dimensions = new VideoDimensions(640, 360);
            int codecType = Constants.VIDEO_CODEC_H264;
            int frameRate = 30;
            int orientationMode = Constants.VIDEO_ORIENTATION_0;
            VideoEncoderConfig config = new VideoEncoderConfig();
            config.setCodecType(codecType);
            // config.setDimensions(dimensions);
            config.setFrameRate(frameRate);
            config.setOrientationMode(orientationMode);
            
            localVideoTrack.setVideoEncoderConfig(config);

            // Publish the local video track
            int result = rtcConn.getLocalUser().publishVideo(localVideoTrack);
            System.out.println("Publish result: " + result);
            rtcConn.registerObserver(new DefaultRtcConnObserver() {
                @Override
                public void onConnected(AgoraRtcConn agora_rtc_conn, RtcConnInfo conn_info, int reason) {
                    System.out.println("Connected");
                    try {
                        Thread.sleep(3000);
                        stream(videoFrameSender);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });

            // Join the channel
            result = rtcConn.connect(TOKEN, CHANNEL_NAME, UID);
            System.out.println("Conneted result: " + result);
            Thread.sleep(1000 * 60 * 5);
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
        FFMpegParser1.parse(inputFilePath, (dataInfo) -> {
            EncodedVideoFrameInfo info = new EncodedVideoFrameInfo();
            info.setCodecType(Constants.VIDEO_CODEC_H264);
            info.setRotation(Constants.VIDEO_ORIENTATION_0);
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
        // List<byte[]> frameDataList = decodeH264ToByteArrays(inputFilePath, sender);

        // // Print information about the frames
        // System.out.println("Number of frames decoded: " + frameDataList.size());
        // for (int i = 0; i < frameDataList.size(); i++) {
        //     System.out.println("Frame " + (i + 1) + " size: " + frameDataList.get(i).length + " bytes");
        // }
    }
    /**
     * InnerMain
     */

    private static List<byte[]> decodeH264ToByteArrays(String inputFilePath, AgoraVideoEncodedImageSender sender) throws InterruptedException {
        List<byte[]> frameDataList = new ArrayList<>();
        long waitInterval = 1000 / 30;
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFilePath)) {
            grabber.start();

            try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
                Frame frame;
                while ((frame = grabber.grabFrame()) != null) {
                    if (frame.image != null) {
                        byte[] imageData = frameToByteArray(frame);
                        frameDataList.add(imageData);
                    
                    
                        EncodedVideoFrameInfo info = new EncodedVideoFrameInfo();
                        info.setCodecType(Constants.VIDEO_CODEC_H264);
                        info.setRotation(Constants.VIDEO_ORIENTATION_0);
                        info.setFramesPerSecond(30);
                        info.setFrameType(frame.keyFrame ? Constants.VIDEO_FRAME_TYPE_KEY_FRAME : Constants.VIDEO_FRAME_TYPE_DELTA_FRAME);
                        int result = sender.send(imageData, imageData.length, info);
                        System.err.println("send video frame result: " + result);
                        frameDataList.add(imageData);
                        Thread.sleep(waitInterval);
                    }
                }
            }
            grabber.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return frameDataList;
    }

    private static byte[] frameToByteArray(Frame frame) {
        if (frame.image != null && frame.image[0] instanceof ByteBuffer) {
            ByteBuffer buffer = (ByteBuffer) frame.image[0];
            byte[] imageData = new byte[buffer.remaining()];
            buffer.get(imageData);
            return imageData;
        } else {
            System.err.println("Frame does not contain image data in ByteBuffer format");
            return null;
        }
    }
}
