package com.softard.wow.screencapture.recorder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.softard.wow.screencapture.encoder.BaseEncoder;
import com.softard.wow.screencapture.encoder.BaseEncoderTask;
import com.softard.wow.screencapture.encoder.MediaCallback;

public class CallbackDelegate extends Handler {
    private static final String TAG = "CallbackDelegate";
    private MediaCallback mCallback;

    public CallbackDelegate(Looper l, MediaCallback callback) {
        super(l);
        this.mCallback = callback;
    }

    void onError(BaseEncoderTask encoder, MediaCodec codec, Exception exception) {
        Message.obtain(this, () -> {
            if (mCallback != null) {
                mCallback.onError(encoder, codec, exception);
            }
        }).sendToTarget();
    }

    void onOutputFormatChanged(BaseEncoder encoder, MediaFormat format) {
        Message.obtain(this, () -> {
            if (mCallback != null) {
                mCallback.onOutputFormatChanged(encoder, format);
            }
        }).sendToTarget();
    }

    void onOutputBufferAvailable(BaseEncoder encoder, int index, MediaCodec.BufferInfo info) {
        Message.obtain(this, () -> {
            if (mCallback != null) {
                mCallback.onOutputBufferAvailable(encoder, index, info);
            }
        }).sendToTarget();
    }
}
