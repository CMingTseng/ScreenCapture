package com.softard.wow.screencapture.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;

public abstract class Callback {
    void onInputBufferAvailable(BaseEncoder encoder, int index) {
    }

    public void onOutputFormatChanged(BaseEncoder encoder, MediaFormat format) {
    }

    public void onOutputBufferAvailable(BaseEncoder encoder, int index, MediaCodec.BufferInfo info) {
    }
}
