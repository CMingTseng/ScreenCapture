package com.softard.wow.screencapture.CodecTools;

import android.media.MediaFormat;

import com.softard.wow.screencapture.config.AudioEncodeConfig;

/**
 * Created by wOw on 2019-08-19.
 * Email: wossoneri@163.com
 * Copyright (c) 2019 Softard. All rights reserved.
 */
public class AudioEncoder extends BaseEncoder {
    private static final String TAG = "AudioEncoder";
    private final AudioEncodeConfig mConfig;

    AudioEncoder(AudioEncodeConfig config) {
        super(config.mCodecName);
        this.mConfig = config;
    }

    @Override
    protected MediaFormat createMediaFormat() {
        return mConfig.toMediaFormat();
    }
}

