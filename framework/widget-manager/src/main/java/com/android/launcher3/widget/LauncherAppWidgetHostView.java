/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.widget;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import com.android.launcher3.CheckLongPressHelper;
import com.android.launcher3.OnTouchEventListener;


/**
 * {@inheritDoc}
 */
public class LauncherAppWidgetHostView extends AppWidgetHostView implements View.OnLongClickListener {
    private final CheckLongPressHelper mLongPressHelper;

    private OnTouchEventListener onTouchEventListener;

    public LauncherAppWidgetHostView(Context context) {
        super(context);
        mLongPressHelper = new CheckLongPressHelper(this, this);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mLongPressHelper.onTouchEvent(ev);

        onTouchEventListener.onTouchEvent(ev);

        return mLongPressHelper.hasPerformedLongPress();
    }

    public boolean onTouchEvent(MotionEvent ev) {
        mLongPressHelper.onTouchEvent(ev);

        onTouchEventListener.onTouchEvent(ev);

        // We want to keep receiving though events to be able to cancel long press on ACTION_UP
        return true;
    }

    @Override
    public boolean onLongClick(View view) {
        view.performLongClick();
        return true;
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mLongPressHelper.cancelLongPress();
    }

    public void setOnTouchEventListener(OnTouchEventListener onTouchEventListener) {
        this.onTouchEventListener = onTouchEventListener;
    }
}