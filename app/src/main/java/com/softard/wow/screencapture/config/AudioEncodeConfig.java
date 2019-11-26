package com.softard.wow.screencapture.config;

/*
 * Copyright (c) 2017 Yrom Wang <http://www.yrom.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.media.AudioFormat;
import android.media.MediaFormat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * @author yrom
 * @version 2017/12/3
 */

/**
 * Created by wOw on 2019-08-19. Email: wossoneri@163.com Copyright (c) 2019 Softard. All rights
 * reserved.
 */

public class AudioEncodeConfig extends EncodeConfig implements ConfigInterface {
    private final static String TAG = "AudioEncodeConfig";
    final int bitRate;
    public final int sampleRate;
    private int mChannelsSampleRate;
    public final int channelCount;
    final int profile;

    @IntDef(value = {
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.CHANNEL_IN_LEFT, AudioFormat.CHANNEL_IN_RIGHT, AudioFormat.CHANNEL_IN_FRONT,
            AudioFormat.CHANNEL_IN_BACK, AudioFormat.CHANNEL_IN_LEFT_PROCESSED, AudioFormat.CHANNEL_IN_RIGHT_PROCESSED,
            AudioFormat.CHANNEL_IN_FRONT_PROCESSED, AudioFormat.CHANNEL_IN_BACK_PROCESSED, AudioFormat.CHANNEL_IN_PRESSURE,
            AudioFormat.CHANNEL_IN_X_AXIS, AudioFormat.CHANNEL_IN_Y_AXIS, AudioFormat.CHANNEL_IN_Z_AXIS,
            AudioFormat.CHANNEL_IN_VOICE_UPLINK, AudioFormat.CHANNEL_IN_VOICE_DNLINK, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.CHANNEL_IN_STEREO
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface AudioChannelCountType {
    }


    public AudioEncodeConfig(String codecName, String codeMIMEType, int bitRate, int sampleRate, int channelCount, int profile) {
        super(codecName, codeMIMEType);
        this.bitRate = bitRate;
        this.sampleRate = sampleRate;
        this.channelCount = channelCount;
        this.profile = profile;
        mChannelsSampleRate = sampleRate * channelCount;
    }

    @Override
    public MediaFormat toMediaFormat() {
        MediaFormat format = MediaFormat.createAudioFormat(mMIMEType, sampleRate, channelCount);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, profile);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        //format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 4096 * 4);
        return format;
    }

    @Override
    public String toString() {
        return "AudioEncodeConfig{" +
                "codecName='" + mCodecName + '\'' +
                ", mimeType='" + mMIMEType + '\'' +
                ", bitRate=" + bitRate +
                ", sampleRate=" + sampleRate +
                ", channelCount=" + channelCount +
                ", profile=" + profile +
                '}';
    }
}
