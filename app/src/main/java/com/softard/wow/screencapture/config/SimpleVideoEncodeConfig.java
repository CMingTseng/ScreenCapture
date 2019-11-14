package com.softard.wow.screencapture.config;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;

public class SimpleVideoEncodeConfig extends EncodeConfig implements ConfigInterface {
    private final static String TAG = "SimpleVideoEncodeConfig";
    public int width;
    public int height;
    protected int bitrate;
    protected int framerate;
    protected int iframeInterval;
    private static final int FPS = 30;

    public SimpleVideoEncodeConfig(String codecName, String codeMIMEType, int width, int height, int bitrate) {
        super(codecName, codeMIMEType);
        this.width = width;
        this.height = height;
        this.bitrate = bitrate;
        this.framerate = FPS;
        this.iframeInterval = 1;
    }

    @Override
    public MediaFormat toMediaFormat() {
        final MediaFormat mediaformat = MediaFormat.createVideoFormat(mMIMEType, width, height);
        switch (mMIMEType) {
            case MediaFormat.MIMETYPE_VIDEO_AVC:
                mediaformat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
                break;
            case MediaFormat.MIMETYPE_VIDEO_VP8:
                mediaformat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
                break;
        }
        mediaformat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        mediaformat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
        mediaformat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iframeInterval);
        mediaformat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 0);
        // maybe useful
        // format.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 10_000_000);
        return mediaformat;
    }

    @Override
    public String toString() {
        return "SimpleVideoEncodeConfig{" +
                "width=" + width +
                ", height=" + height +
                ", bitrate=" + bitrate +
                ", framerate=" + framerate +
                ", iframeInterval=" + iframeInterval +
                ", codecName='" + mCodecName + '\'' +
                ", mimeType='" + mMIMEType +
                '}';
    }
}
