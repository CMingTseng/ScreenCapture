package com.softard.wow.screencapture.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Created by wOw on 2019-08-19.
 * Email: wossoneri@163.com
 * Copyright (c) 2019 Softard. All rights reserved.
 */
public abstract class BaseEncoder implements BaseEncoderAction {
    private final static String TAG = "BaseEncoder";
    private String mCodecName;
    private MediaCodec mEncoder;
    private MediaCallback mCallback;
    /**
     * let media codec run async mode if mCallback != null
     */
    private MediaCodec.Callback mCodecCallback;

    public BaseEncoder(String codecName) {
        this.mCodecName = codecName;
        mCodecCallback = new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(MediaCodec codec, int index) {
                mCallback.onInputBufferAvailable(BaseEncoder.this, index);
            }

            @Override
            public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
                mCallback.onOutputBufferAvailable(BaseEncoder.this, index, info);
            }

            @Override
            public void onError(MediaCodec codec, MediaCodec.CodecException e) {
                BaseEncoder.this.onActionError(BaseEncoder.this, codec, e);
            }

            @Override
            public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
                mCallback.onOutputFormatChanged(BaseEncoder.this, format);
            }
        };
    }

    public void setCallback(MediaCallback callback) {
        if (this.mEncoder != null)
            throw new IllegalStateException("Encoder(MediaCodec) is not null");
        this.mCallback = callback;
    }

    /**
     * Must call in a worker handler thread!
     */
    @Override
    public void prepare() throws IOException {
        if (Looper.myLooper() == null
                || Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("should run in a HandlerThread");
        }
        if (mEncoder != null) {
            throw new IllegalStateException("prepared!");
        }
        MediaFormat format = generateMediaFormat();
        Log.d(TAG, "Create media format: " + format);

        String mimeType = format.getString(MediaFormat.KEY_MIME);
        final MediaCodec encoder = createEncoder(mimeType);
        try {
            if (this.mCallback != null) {
                // NOTE: MediaCodec maybe crash on some devices due to null callback
                encoder.setCallback(mCodecCallback);
            }
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            onEncoderConfigured(encoder);
            encoder.start();
        } catch (MediaCodec.CodecException e) {
            Log.e(TAG, "Configure codec failure!\n  with format" + format, e);
            throw e;
        }
        mEncoder = encoder;
    }

    /**
     * call immediately after {@link #getMediaCodecEncoder() MediaCodec}
     * configure with {@link #generateMediaFormat() MediaFormat} success
     *
     * @param encoder
     */
    protected void onEncoderConfigured(MediaCodec encoder) {
    }

    /**
     * create a new instance of MediaCodec
     */
    private MediaCodec createEncoder(String type) throws IOException {
        try {
            // use codec name first
            if (this.mCodecName != null) {
                return MediaCodec.createByCodecName(mCodecName);
            }
        } catch (IOException e) {
            Log.w(TAG, "Create MediaCodec by name '" + mCodecName + "' failure!", e);
        }
        return MediaCodec.createEncoderByType(type);
    }

    /**
     * create {@link MediaFormat} for {@link MediaCodec}
     */
    protected abstract MediaFormat generateMediaFormat();

    public final MediaCodec getMediaCodecEncoder() {
        return Objects.requireNonNull(mEncoder, "doesn't prepare()");
    }

    /**
     * @throws NullPointerException if prepare() not call
     * @see MediaCodec#getOutputBuffer(int)
     */
    public final ByteBuffer getOutputBuffer(int index) {
        return getMediaCodecEncoder().getOutputBuffer(index);
    }

    /**
     * @throws NullPointerException if prepare() not call
     * @see MediaCodec#getInputBuffer(int)
     */
    public final ByteBuffer getInputBuffer(int index) {
        return getMediaCodecEncoder().getInputBuffer(index);
    }

    /**
     * @throws NullPointerException if prepare() not call
     * @see MediaCodec#queueInputBuffer(int, int, int, long, int)
     * @see MediaCodec#getInputBuffer(int)
     */
    public final void queueInputBuffer(int index, int offset, int size, long pstTs, int flags) {
        getMediaCodecEncoder().queueInputBuffer(index, offset, size, pstTs, flags);
    }

    /**
     * @throws NullPointerException if prepare() not call
     * @see MediaCodec#releaseOutputBuffer(int, boolean)
     */
    public final void releaseOutputBuffer(int index) {
        getMediaCodecEncoder().releaseOutputBuffer(index, false);
    }

    /**
     * @see MediaCodec#stop()
     */
    @Override
    public void stop() {
        if (mEncoder != null) {
            mEncoder.stop();
        }
    }

    /**
     * @see MediaCodec#release()
     */
    @Override
    public void release() {
        if (mEncoder != null) {
            mEncoder.release();
            mEncoder = null;
        }
    }
}
