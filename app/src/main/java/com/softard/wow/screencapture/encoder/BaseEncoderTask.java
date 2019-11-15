
package com.softard.wow.screencapture.encoder;

import java.io.IOException;

public interface BaseEncoderTask {
    void onPrepare() throws IOException;

    void stop();

    void release();

    void setCallback(Callback callback);

    interface Callback {
        void onError(BaseEncoderTask encoder, Exception exception);
    }
}
