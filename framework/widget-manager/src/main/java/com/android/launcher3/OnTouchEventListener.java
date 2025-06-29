package com.android.launcher3;

import android.view.MotionEvent;

public interface OnTouchEventListener {
    void onTouchEvent(MotionEvent ev, Boolean canScrollVertically);

}
