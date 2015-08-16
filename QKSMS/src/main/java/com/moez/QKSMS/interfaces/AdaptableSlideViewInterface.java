/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.moez.QKSMS.interfaces;

/**
 * The view interface of a slide which elements can be resize.
 */
public interface AdaptableSlideViewInterface extends SlideViewInterface {
    /**
     * Set the display region of the text view.
     */
    void setTextRegion(int left, int top, int width, int height);
    /**
     * Set the display region of the image view.
     */
    void setImageRegion(int left, int top, int width, int height);
    /**
     * Set the display region of the video view.
     */
    void setVideoRegion(int left, int top, int width, int height);
    /**
     * Set the listener which will be triggered when the size of
     * the view is changed.
     */
    void setOnSizeChangedListener(OnSizeChangedListener l);

    public interface OnSizeChangedListener {
        void onSizeChanged(int width, int height);
    }
}
