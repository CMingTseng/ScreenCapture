package com.softard.wow.screencapture.recorder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

public class RecordAction {
    public static final int MSG_PREPARE = 0;
    public static final int MSG_START = 1;
    public static final int MSG_FEED_INPUT = 2;
    public static final int MSG_DRAIN_OUTPUT = 3;
    public static final int MSG_RELEASE_OUTPUT = 4;
    public static final int MSG_STOP = 5;
    public static final int MSG_RELEASE = 6;
    public static final int MSG_ERROR = -1;

    @IntDef(value = {
            MSG_PREPARE, MSG_START, MSG_FEED_INPUT, MSG_DRAIN_OUTPUT, MSG_RELEASE_OUTPUT, MSG_STOP, MSG_RELEASE, MSG_ERROR
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Action {
    }
}
