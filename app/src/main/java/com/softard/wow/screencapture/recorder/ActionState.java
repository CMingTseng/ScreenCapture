package com.softard.wow.screencapture.recorder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

public class ActionState {
    public static final int ACTION_FAIL = -1;
    public static final int ACTION_START = 0;
    public static final int ACTION_PROCESS = 1;
    public static final int ACTION_FINISH = 2;

    @IntDef(value = {
            ACTION_FAIL, ACTION_START, ACTION_PROCESS, ACTION_FINISH
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ActionResult {
    }

    @RecordAction.Action
    public int mState = RecordAction.MSG_PREPARE;
    @ActionResult
    public int mResult = ACTION_START;
}
