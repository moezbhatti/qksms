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
import com.klinker.android.logger.Log;

public class HVGALayoutParameters implements LayoutParameters {
    private static final String TAG = LogTag.TAG;
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;

    private int mType = -1;

    private static int mImageHeightLandscape;
    private static int mTextHeightLandscape;
    private static int mImageHeightPortrait;
    private static int mTextHeightPortrait;
    private static int mMaxHeight;
    private static int mMaxWidth;

    public HVGALayoutParameters(Context context, int type) {
        if ((type != HVGA_LANDSCAPE) && (type != HVGA_PORTRAIT)) {
            throw new IllegalArgumentException(
                    "Bad layout type detected: " + type);
        }

        if (LOCAL_LOGV) {
            Log.v(TAG, "HVGALayoutParameters.<init>(" + type + ").");
        }
        mType = type;

        float scale = context.getResources().getDisplayMetrics().density;
        mMaxWidth = (int) (context.getResources().getConfiguration().screenWidthDp * scale + 0.5f);
        mMaxHeight =
            (int) (context.getResources().getConfiguration().screenHeightDp * scale + 0.5f);

        mImageHeightLandscape = (int) (mMaxHeight * .90f);
        mTextHeightLandscape = (int) (mMaxHeight * .10f);
        mImageHeightPortrait = (int) (mMaxWidth * .90f);
        mTextHeightPortrait = (int) (mMaxWidth * .10f);

        if (LOCAL_LOGV) {
            Log.v(TAG, "HVGALayoutParameters mMaxWidth: " + mMaxWidth +
                    " mMaxHeight: " + mMaxHeight +
                    " mImageHeightLandscape: " + mImageHeightLandscape +
                    " mTextHeightLandscape: " + mTextHeightLandscape +
                    " mImageHeightPortrait: " + mImageHeightPortrait +
                    " mTextHeightPortrait: " + mTextHeightPortrait);
        }

    }

    public int getWidth() {
        return mType == HVGA_LANDSCAPE ? mMaxWidth
                                       : mMaxHeight;
    }

    public int getHeight() {
        return mType == HVGA_LANDSCAPE ? mMaxHeight
                                       : mMaxWidth;
    }

    public int getImageHeight() {
        return mType == HVGA_LANDSCAPE ? mImageHeightLandscape
                                       : mImageHeightPortrait;
    }

    public int getTextHeight() {
        return mType == HVGA_LANDSCAPE ? mTextHeightLandscape
                                       : mTextHeightPortrait;
    }

    public int getType() {
        return mType;
    }

    public String getTypeDescription() {
        return mType == HVGA_LANDSCAPE ? "HVGA-L" : "HVGA-P";
    }
}
