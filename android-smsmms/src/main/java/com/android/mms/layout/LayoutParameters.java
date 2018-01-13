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

package com.android.mms.layout;

public interface LayoutParameters {
    /* Layouts type definition */
    public static final int UNKNOWN        = -1;
    public static final int HVGA_LANDSCAPE = 10;
    public static final int HVGA_PORTRAIT  = 11;

    /* Parameters for known layouts */
    public static final int HVGA_LANDSCAPE_WIDTH  = 480;
    public static final int HVGA_LANDSCAPE_HEIGHT = 320;
    public static final int HVGA_PORTRAIT_WIDTH   = 320;
    public static final int HVGA_PORTRAIT_HEIGHT  = 480;

    /**
     * Get the width of current layout.
     */
    int getWidth();
    /**
     * Get the height of current layout.
     */
    int getHeight();
    /**
     * Get the width of the image region of current layout.
     */
    int getImageHeight();
    /**
     * Get the height of the text region of current layout.
     */
    int getTextHeight();
    /**
     * Get the type of current layout.
     */
    int getType();
    /**
     * Get the type description of current layout.
     */
    String getTypeDescription();
}
