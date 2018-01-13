/*
 * Copyright 2014 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.layout;

import com.android.mms.logs.LogTag;

import android.content.Context;
import android.content.res.Configuration;
import com.klinker.android.logger.Log;

/**
 * MMS presentation layout management.
 */
public class LayoutManager {
    private static final String TAG = LogTag.TAG;
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;

    private final Context mContext;
    private LayoutParameters mLayoutParams;

    private static LayoutManager sInstance;

    private LayoutManager(Context context) {
        mContext = context;
        initLayoutParameters(context.getResources().getConfiguration());
    }

    private void initLayoutParameters(Configuration configuration) {
        mLayoutParams = getLayoutParameters(
                configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                ? LayoutParameters.HVGA_PORTRAIT
                : LayoutParameters.HVGA_LANDSCAPE);

        if (LOCAL_LOGV) {
            Log.v(TAG, "LayoutParameters: " + mLayoutParams.getTypeDescription()
                    + ": " + mLayoutParams.getWidth() + "x" + mLayoutParams.getHeight());
        }
    }

    private LayoutParameters getLayoutParameters(int displayType) {
        switch (displayType) {
            case LayoutParameters.HVGA_LANDSCAPE:
                return new HVGALayoutParameters(mContext, LayoutParameters.HVGA_LANDSCAPE);
            case LayoutParameters.HVGA_PORTRAIT:
                return new HVGALayoutParameters(mContext, LayoutParameters.HVGA_PORTRAIT);
        }

        throw new IllegalArgumentException(
                "Unsupported display type: " + displayType);
    }

    public static void init(Context context) {
        if (LOCAL_LOGV) {
            Log.v(TAG, "DefaultLayoutManager.init()");
        }

        if (sInstance != null) {
            Log.w(TAG, "Already initialized.");
        }
        sInstance = new LayoutManager(context);
    }

    public static LayoutManager getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("Uninitialized.");
        }
        return sInstance;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (LOCAL_LOGV) {
            Log.v(TAG, "-> LayoutManager.onConfigurationChanged().");
        }
        initLayoutParameters(newConfig);
    }

    public int getLayoutType() {
        return mLayoutParams.getType();
    }

    public int getLayoutWidth() {
        return mLayoutParams.getWidth();
    }

    public int getLayoutHeight() {
        return mLayoutParams.getHeight();
    }

    public LayoutParameters getLayoutParameters() {
        return mLayoutParams;
    }
}
