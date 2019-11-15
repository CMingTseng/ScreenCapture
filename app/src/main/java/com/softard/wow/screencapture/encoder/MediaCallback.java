package com.softard.wow.screencapture.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;

public interface MediaCallback extends BaseEncoderAction {
    void onInputBufferAvailable(BaseEncoder encoder, int index);

    void onOutputFormatChanged(BaseEncoder encoder, MediaFormat format);

    void onOutputBufferAvailable(BaseEncoder encoder, int index, MediaCodec.BufferInfo info);

    void onError(MediaCodec codec, Exception e);
}
