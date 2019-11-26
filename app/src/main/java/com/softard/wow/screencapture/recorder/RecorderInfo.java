package com.softard.wow.screencapture.recorder;

import android.media.MediaCodec;
import android.media.MediaFormat;

public class RecorderInfo {
    public MediaFormat mFormat;
    public int mIndex;
    public MediaCodec.BufferInfo mInfo;
    public Exception mException;
}
