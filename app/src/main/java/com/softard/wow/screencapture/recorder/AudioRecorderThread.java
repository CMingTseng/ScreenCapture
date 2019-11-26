package com.softard.wow.screencapture.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseLongArray;

import com.softard.wow.screencapture.BuildConfig;
import com.softard.wow.screencapture.config.AudioEncodeConfig;
import com.softard.wow.screencapture.encoder.AudioEncoder;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

public class AudioRecorderThread extends HandlerThread {//FIXME  AudioEncoder (maybe MediaCodec  should run in a HandlerThread)
    private static final String TAG = "AudioRecorderThread";
    private int mSampleRate;
    private int mChannelCount;
    private int mFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int mChannelsSampleRate;
    private LinkedList<MediaCodec.BufferInfo> mCachedInfos = new LinkedList<>();
    private LinkedList<Integer> mMuxingOutputBufferIndices = new LinkedList<>();
    private static final int LAST_FRAME_ID = -1;
    private SparseLongArray mFramesUsCache = new SparseLongArray(2);
    private int mPollRate = 2048_000;
    private final ActionState mAction;

    public final MutableLiveData<ActionState> mThreadActionStateObserver;
    public final Observer<ActionState> mThreadInfosObserver;
    private AudioRecord mMic; // access in mRecordThread only!
    private final AudioEncoder mEncoder;
    private AtomicBoolean mForceStop;
    public final MutableLiveData<RecorderInfo> mRecorderObserver;

    public AudioRecorderThread(AudioEncodeConfig config) {
        super(TAG);
        mSampleRate = config.sampleRate;
        mChannelCount = config.channelCount;
        mEncoder = new AudioEncoder(config);
        mSampleRate = config.sampleRate;
        mChannelsSampleRate = mSampleRate * config.channelCount;
        mPollRate = 2048_000 / mSampleRate; // poll per 2048 samples
        mForceStop = new AtomicBoolean(false);

        mAction = new ActionState();
        mThreadActionStateObserver = new MutableLiveData<>();
        mRecorderObserver = new MutableLiveData<>();
        mThreadInfosObserver = new Observer<ActionState>() {
            @Override
            public void onChanged(@Nullable ActionState notifyinfos) {
                Log.e(TAG, "Show  get  ActionState info change " + notifyinfos.mState);
                final int state = notifyinfos.mState;
                final int result = notifyinfos.mResult;
                switch (state) {
                    case RecordAction.MSG_PREPARE:
                        if (result == ActionState.ACTION_START) {
                            Log.e(TAG, "create audio record  ");
                            AudioRecord r = AudioUtils.createAudioRecord(mSampleRate, mChannelCount, mFormat);
                            if (r == null) {
                                Log.e(TAG, "create audio record failure");
//                            onActionError(this, null, new Exception("create audio record failure"));
                                break;
                            } else {
                                mMic = r;
                                Log.e(TAG, "create audio record ok ");
                                try {
                                    Log.e(TAG, "Show  AudioEncoder : " + mEncoder);
                                    mEncoder.prepare();//FIXME  AudioEncoder (maybe MediaCodec  should run in a HandlerThread)
                                    //FIXME to notify start
                                    mAction.mState = RecordAction.MSG_START;
                                    mAction.mResult = ActionState.ACTION_START;
                                    mThreadActionStateObserver.postValue(mAction);
                                } catch (Exception e) {
                                    Log.e(TAG, "prepare failure : " + e);
//                                onActionError(this, null, e);
                                    mAction.mState = RecordAction.MSG_ERROR;
                                    mAction.mResult = ActionState.ACTION_FINISH;
                                    mThreadActionStateObserver.postValue(mAction);
                                    break;
                                }
                            }
                        }

                        break;
                    case RecordAction.MSG_START:
                        if (result == ActionState.ACTION_START) {
                            mMic.startRecording();
                            mAction.mState = RecordAction.MSG_FEED_INPUT;
                            mAction.mResult = ActionState.ACTION_START;
                            mThreadActionStateObserver.postValue(mAction);
                        }
                        break;
                    case RecordAction.MSG_FEED_INPUT:
                        if (!mForceStop.get()) {
                            int index = pollInput();
                            Log.d(TAG, "Show audio encoder returned input buffer index = " + index);
                            if (result == ActionState.ACTION_START) {
                                if (index >= 0) {
                                    feedAudioEncoder(index);
                                    if (!mForceStop.get()) {
                                        mAction.mState = RecordAction.MSG_DRAIN_OUTPUT;
                                        mAction.mResult = ActionState.ACTION_START;
                                        mThreadActionStateObserver.postValue(mAction);
                                    }
                                } else {
                                    mAction.mState = RecordAction.MSG_DRAIN_OUTPUT;
                                    mAction.mResult = ActionState.ACTION_RETRY;
                                    mThreadActionStateObserver.postValue(mAction);
                                }
                            } else if (result == ActionState.ACTION_RETRY) {
                                mAction.mState = RecordAction.MSG_FEED_INPUT;
                                mAction.mResult = ActionState.ACTION_RETRY;
                                mThreadActionStateObserver.postValue(mAction);
                            }
                        }

                        break;
                    case RecordAction.MSG_DRAIN_OUTPUT:
                        if (result == ActionState.ACTION_START) {
                            offerOutput();
                            Log.e(TAG, "@ MSG_DRAIN_OUTPUT get break");
                            pollInputIfNeed();
                        }

                        break;
                    case RecordAction.MSG_STOP:
                        if (result == ActionState.ACTION_START) {
                            if (mMic != null) {
                                mMic.stop();
                            }
                            mEncoder.stop();
                        }

                        break;
                    case RecordAction.MSG_RELEASE:
                        if (result == ActionState.ACTION_START) {
                            if (mMic != null) {
                                mMic.release();
                                mMic = null;
                            }
                            mEncoder.release();
                        }

                        break;


                    case RecordAction.MSG_RELEASE_OUTPUT:
                        if (result == ActionState.ACTION_START) {
                            //                        mEncoder.releaseOutputBuffer(msg.arg1);
//                        mMuxingOutputBufferIndices.poll(); // Nobody care what it exactly is.
//                        if (BuildConfig.DEBUG)
//                            Log.d(TAG, "audio encoder released output buffer index="
//                                    + msg.arg1 + ", remaining=" + mMuxingOutputBufferIndices.size());
//                        pollInputIfNeed();
                        }

                        break;

                    case RecordAction.MSG_ERROR:
                        Log.d(TAG, "Thread get Error ");

                        break;
                }
            }
        };
        mThreadActionStateObserver.observeForever(mThreadInfosObserver);
    }

    private void offerOutput() {
        Log.d(TAG, "offerOutput");
        while (!mForceStop.get()) {
            MediaCodec.BufferInfo info = mCachedInfos.poll();
            if (info == null) {
                info = new MediaCodec.BufferInfo();
            }
            int index = mEncoder.getMediaCodecEncoder().dequeueOutputBuffer(info, 1);
            Log.d(TAG, "audio encoder returned output buffer index=" + index);
            if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                //FIXME send to write file
//                mCallbackDelegate.onOutputFormatChanged(mEncoder, mEncoder.getEncoder().getOutputFormat());
            }
            if (index < 0) {
                info.set(0, 0, 0, 0);
                mCachedInfos.offer(info);
                break;
            }
            mMuxingOutputBufferIndices.offer(index);
            //FIXME send to ScreenRecorder !!
//            mCallbackDelegate.onOutputBufferAvailable(mEncoder, index, info);

        }
    }

    private int pollInput() {
        return mEncoder.getMediaCodecEncoder().dequeueInputBuffer(0);
    }

    private void pollInputIfNeed() {
        Log.e(TAG, "pollInputIfNeed mMuxingOutputBufferIndices.size() " + mMuxingOutputBufferIndices.size());
        Log.e(TAG, "pollInputIfNeed mForceStop.get() " + mForceStop.get());
        if (mMuxingOutputBufferIndices.size() <= 1 && !mForceStop.get()) {
            // need fresh data, right now!

//            removeMessages(RecordAction.MSG_FEED_INPUT);
//            sendEmptyMessageDelayed(RecordAction.MSG_FEED_INPUT, 0);
            mAction.mState = RecordAction.MSG_FEED_INPUT;
            mAction.mResult = ActionState.ACTION_START;//FIXME
            mThreadActionStateObserver.postValue(mAction);
        }
    }

    /**
     * NOTE: Should waiting all output buffer disappear queue input buffer
     */
    private void feedAudioEncoder(int index) {
        if (index < 0 || mForceStop.get()) return;
        final AudioRecord r = Objects.requireNonNull(mMic, "maybe release");
        final boolean eos = r.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED;
        final ByteBuffer frame = mEncoder.getInputBuffer(index);
        int offset = frame.position();
        int limit = frame.limit();
        int read = 0;
        if (!eos) {
            read = r.read(frame, limit);
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Read frame data size " + read + " for index " + index + " buffer : " + offset + ", " + limit);
            if (read < 0) {
                read = 0;
            }
        }

        long pstTs = calculateFrameTimestamp(read << 3);
        int flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;

        if (eos) {
            flags = MediaCodec.BUFFER_FLAG_END_OF_STREAM;
        }
        // feed frame to encoder
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Feed codec index=" + index + ", presentationTimeUs = " + pstTs + ", flags=" + flags);
        mEncoder.queueInputBuffer(index, offset, read, pstTs, flags);
    }

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

    @Override
    public void run() {
//        mEncoder.start();
        mAction.mState = RecordAction.MSG_PREPARE;
        mAction.mResult = ActionState.ACTION_START;
        mThreadActionStateObserver.postValue(mAction);
    }
}
