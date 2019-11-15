
package com.softard.wow.screencapture.encoder;

import android.media.MediaCodec;

import java.io.IOException;

public interface BaseEncoderTask {
    void onPrepare() throws IOException;

    void stop();

    void release();

    void onError(BaseEncoderTask basktask, MediaCodec codec, MediaCodec.CodecException e);
}
