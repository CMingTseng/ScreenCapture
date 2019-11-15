
package com.softard.wow.screencapture.encoder;

import android.media.MediaCodec;

import java.io.IOException;

public interface BaseEncoderAction {
    void prepare() throws IOException;

    void stop();

    void release();

    void onActionError(BaseEncoderAction action, MediaCodec codec, Exception e);
}
