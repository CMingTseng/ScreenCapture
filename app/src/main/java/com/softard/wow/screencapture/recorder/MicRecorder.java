package com.softard.wow.screencapture.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseLongArray;

import com.softard.wow.screencapture.BuildConfig;
import com.softard.wow.screencapture.config.AudioEncodeConfig;
import com.softard.wow.screencapture.encoder.BaseEncoderAction;
import com.softard.wow.screencapture.encoder.MediaCallback;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

/**
 * Created by wOw on 2019-08-19. Email: wossoneri@163.com Copyright (c) 2019 Softard. All rights
 * reserved.
 */
public class MicRecorder implements BaseEncoderAction {
    private static final String TAG = "MicRecorder";
//    private final AudioEncoder mEncoder;

    private RecordHandler mRecordHandler;
    private AudioRecord mMic; // access in mRecordThread only!
    private int mSampleRate;
    private int mChannelConfig;
    private int mFormat = AudioFormat.ENCODING_PCM_16BIT;

    private AtomicBoolean mForceStop = new AtomicBoolean(false);
    private MediaCallback mCallback;

    private AudioRecorderThread mRecorderThread;
    private int mChannelsSampleRate;
    private ActionState mState;
    public final MutableLiveData<ActionState> mActionStateObserver;
    public final Observer<ActionState> mInfosObserver;

    public MicRecorder(AudioEncodeConfig config) {
//        mEncoder = new AudioEncoder(config);
        mSampleRate = config.sampleRate;
        mChannelsSampleRate = mSampleRate * config.channelCount;
        if (BuildConfig.DEBUG) Log.i(TAG, "in bitrate " + mChannelsSampleRate * 16 /* PCM_16BIT*/);
        mChannelConfig = config.channelCount;
        mState = new ActionState();
        mActionStateObserver = new MutableLiveData<>();
        mRecorderThread = new AudioRecorderThread(config);
        mInfosObserver = new Observer<ActionState>() {
            @Override
            public void onChanged(@Nullable ActionState notifyinfos) {
                Log.e(TAG, "Action message :  " + notifyinfos.mState);
                switch (notifyinfos.mState) {
                    case RecordAction.MSG_ERROR:
//                        Log.e(TAG, "get error  ");
                        break;
                }
            }
        };
        mActionStateObserver.observeForever(mRecorderThread.mThreadInfosObserver);
        mRecorderThread.mThreadActionStateObserver.observeForever(mInfosObserver);

    }

    public void setCallback(MediaCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void prepare() throws IOException {

        mRecorderThread.run();
//        mState.mState = RecordAction.MSG_PREPARE;
//        mState.mResult = ActionState.ACTION_START;
//        mActionStateObserver.postValue(mState);
    }

    @Override
    public void stop() {
        mState.mState = RecordAction.MSG_STOP;
        mState.mResult = ActionState.ACTION_FINISH;
        mActionStateObserver.postValue(mState);
    }

    @Override
    public void release() {
        if (mRecordHandler != null) mRecordHandler.sendEmptyMessage(RecordAction.MSG_RELEASE);
        mState.mState = RecordAction.MSG_RELEASE;
        mState.mResult = ActionState.ACTION_FINISH;
        mActionStateObserver.postValue(mState);
    }

    @Override
    public void onActionError(BaseEncoderAction basktask, MediaCodec codec, Exception e) {
        mState.mState = RecordAction.MSG_ERROR;
        mState.mResult = ActionState.ACTION_FINISH;
        mActionStateObserver.postValue(mState);
    }

    void releaseOutputBuffer(int index) {
        if (BuildConfig.DEBUG) Log.d(TAG, "audio encoder released output buffer index=" + index);
        Message.obtain(mRecordHandler, RecordAction.MSG_RELEASE_OUTPUT, index, 0).sendToTarget();
        mState.mState = RecordAction.MSG_RELEASE_OUTPUT;
        mState.mResult = ActionState.ACTION_FINISH;
        mState.mOutputBufferIndex = index;
        mActionStateObserver.postValue(mState);
    }


    ByteBuffer getOutputBuffer(int index) {
//        return mEncoder.getOutputBuffer(index);
        return null;
    }

    private class RecordHandler extends Handler {

        private LinkedList<MediaCodec.BufferInfo> mCachedInfos = new LinkedList<>();
        private LinkedList<Integer> mMuxingOutputBufferIndices = new LinkedList<>();
        private int mPollRate = 2048_000 / mSampleRate; // poll per 2048 samples

        RecordHandler(Looper l) {
            super(l);
        }

        @Override
        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case RecordAction.MSG_PREPARE:
//                    AudioRecord r = AudioUtils.createAudioRecord(mSampleRate, mChannelConfig, mFormat);
//                    if (r == null) {
//                        Log.e(TAG, "create audio record failure");
//                        mCallbackDelegate.onError(MicRecorder.this, null, new IllegalArgumentException());
//                        break;
//                    } else {
//                        r.startRecording();
//                        mMic = r;
//                    }
//                    try {
//                        mEncoder.prepare();
//                    } catch (Exception e) {
//                        mCallbackDelegate.onError(MicRecorder.this, null, e);
//                        break;
//                    }
//                case RecordAction.MSG_FEED_INPUT:
//                    if (!mForceStop.get()) {
//                        int index = pollInput();
//                        if (BuildConfig.DEBUG)
//                            Log.d(TAG, "audio encoder returned input buffer index=" + index);
//                        if (index >= 0) {
//                            feedAudioEncoder(index);
//                            // tell encoder to eat the fresh meat!
//                            if (!mForceStop.get()) sendEmptyMessage(RecordAction.MSG_DRAIN_OUTPUT);
//                        } else {
//                            // try later...
//                            if (BuildConfig.DEBUG) Log.i(TAG, "try later to poll input buffer");
//                            sendEmptyMessageDelayed(RecordAction.MSG_FEED_INPUT, mPollRate);
//                        }
//                    }
//                    break;
//                case RecordAction.MSG_DRAIN_OUTPUT:
//                    offerOutput();
//                    pollInputIfNeed();
//                    break;
//                case RecordAction.MSG_RELEASE_OUTPUT:
//                    mEncoder.releaseOutputBuffer(msg.arg1);
//                    mMuxingOutputBufferIndices.poll(); // Nobody care what it exactly is.
//                    if (BuildConfig.DEBUG) Log.d(TAG, "audio encoder released output buffer index="
//                            + msg.arg1 + ", remaining=" + mMuxingOutputBufferIndices.size());
//                    pollInputIfNeed();
//                    break;
//                case RecordAction.MSG_STOP:
//                    if (mMic != null) {
//                        mMic.stop();
//                    }
//                    mEncoder.stop();
//                    break;
//                case RecordAction.MSG_RELEASE:
//                    if (mMic != null) {
//                        mMic.release();
//                        mMic = null;
//                    }
//                    mEncoder.release();
//                    break;
//            }
        }

        private void offerOutput() {
//            while (!mForceStop.get()) {
//                MediaCodec.BufferInfo info = mCachedInfos.poll();
//                if (info == null) {
//                    info = new MediaCodec.BufferInfo();
//                }
//                int index = mEncoder.getMediaCodecEncoder().dequeueOutputBuffer(info, 1);
//                if (BuildConfig.DEBUG)
//                    Log.d(TAG, "audio encoder returned output buffer index=" + index);
//                if (index == INFO_OUTPUT_FORMAT_CHANGED) {
//                    mCallbackDelegate.onOutputFormatChanged(mEncoder, mEncoder.getMediaCodecEncoder().getOutputFormat());
//                }
//                if (index < 0) {
//                    info.set(0, 0, 0, 0);
//                    mCachedInfos.offer(info);
//                    break;
//                }
//                mMuxingOutputBufferIndices.offer(index);
//                mCallbackDelegate.onOutputBufferAvailable(mEncoder, index, info);
//
//            }
        }

        private int pollInput() {
//            return mEncoder.getMediaCodecEncoder().dequeueInputBuffer(0);
            return -1;
        }

        private void pollInputIfNeed() {
            if (mMuxingOutputBufferIndices.size() <= 1 && !mForceStop.get()) {
                // need fresh data, right now!
                removeMessages(RecordAction.MSG_FEED_INPUT);
                sendEmptyMessageDelayed(RecordAction.MSG_FEED_INPUT, 0);
            }
        }
    }

    /**
     * NOTE: Should waiting all output buffer disappear queue input buffer
     */
    private void feedAudioEncoder(int index) {
//        if (index < 0 || mForceStop.get()) return;
//        final AudioRecord r = Objects.requireNonNull(mMic, "maybe release");
//        final boolean eos = r.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED;
//        final ByteBuffer frame = mEncoder.getInputBuffer(index);
//        int offset = frame.position();
//        int limit = frame.limit();
//        int read = 0;
//        if (!eos) {
//            read = r.read(frame, limit);
//            if (BuildConfig.DEBUG) Log.d(TAG, "Read frame data size " + read + " for index "
//                    + index + " buffer : " + offset + ", " + limit);
//            if (read < 0) {
//                read = 0;
//            }
//        }
//
//        long pstTs = calculateFrameTimestamp(read << 3);
//        int flags = BUFFER_FLAG_KEY_FRAME;
//
//        if (eos) {
//            flags = BUFFER_FLAG_END_OF_STREAM;
//        }
//        // feed frame to encoder
//        if (BuildConfig.DEBUG) Log.d(TAG, "Feed codec index=" + index + ", presentationTimeUs="
//                + pstTs + ", flags=" + flags);
//        mEncoder.queueInputBuffer(index, offset, read, pstTs, flags);
    }


    private static final int LAST_FRAME_ID = -1;
    private SparseLongArray mFramesUsCache = new SparseLongArray(2);

    /**
     * Gets presentation time (us) of polled frame. 1 sample = 16 bit
     */
    private long calculateFrameTimestamp(int totalBits) {
        int samples = totalBits >> 4;
        long frameUs = mFramesUsCache.get(samples, -1);
        if (frameUs == -1) {
            frameUs = samples * 1000_000 / mChannelsSampleRate;
            mFramesUsCache.put(samples, frameUs);
        }
        long timeUs = SystemClock.elapsedRealtimeNanos() / 1000;
        // accounts the delay of polling the audio sample data
        timeUs -= frameUs;
        long currentUs;
        long lastFrameUs = mFramesUsCache.get(LAST_FRAME_ID, -1);
        if (lastFrameUs == -1) { // it's the first frame
            currentUs = timeUs;
        } else {
            currentUs = lastFrameUs;
        }
        if (BuildConfig.DEBUG)
            Log.i(TAG, "count samples pts: " + currentUs + ", time pts: " + timeUs + ", samples: " + samples);
        // maybe too late to acquire sample data
        if (timeUs - currentUs >= (frameUs << 1)) {
            // reset
            currentUs = timeUs;
        }
        mFramesUsCache.put(LAST_FRAME_ID, currentUs + frameUs);
        return currentUs;
    }
}
