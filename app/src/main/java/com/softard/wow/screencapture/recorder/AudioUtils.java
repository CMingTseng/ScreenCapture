package com.softard.wow.screencapture.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import com.softard.wow.screencapture.BuildConfig;
import com.softard.wow.screencapture.config.AudioEncodeConfig;

import java.util.Locale;

import static android.os.Build.VERSION_CODES.N;

public class AudioUtils {
    private static final String TAG = "AudioUtils";

    public static AudioRecord createAudioRecord(AudioEncodeConfig config) {
        return createAudioRecord(config.sampleRate, config.channelCount, AudioFormat.ENCODING_PCM_16BIT);
    }

    public static AudioRecord createAudioRecord(int sampleRateInHz, int channelConfig, int audioFormat) {
        int minBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        if (minBytes <= 0) {
            return null;
        }
        return createAudioRecord(MediaRecorder.AudioSource.MIC, sampleRateInHz, channelConfig, audioFormat, minBytes);
    }

    public static AudioRecord createAudioRecord(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes) {
        int minBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        if (minBytes <= 0) {
            Log.e(TAG, String.format(Locale.US, "Bad arguments: getMinBufferSize(%d, %d, %d)", sampleRateInHz, channelConfig, audioFormat));
            return null;
        }
        AudioRecord record = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, minBytes * 2);
        if (record.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e(TAG, String.format(Locale.US, "Bad arguments to new AudioRecord %d, %d, %d", sampleRateInHz, channelConfig, audioFormat));
            return null;
        }
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "created AudioRecord " + record + ", MinBufferSize= " + minBytes);
            if (Build.VERSION.SDK_INT >= N) {
                Log.d(TAG, " size in frame " + record.getBufferSizeInFrames());
            }
        }
        return record;
    }
}
