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

package com.moez.QKSMS.model;

import android.content.Context;
import android.net.Uri;

import com.google.android.mms.MmsException;

public abstract class RegionMediaModel extends MediaModel {
    protected RegionModel mRegion;
    protected boolean mVisible = true;

    public RegionMediaModel(Context context, String tag, Uri uri,
                            RegionModel region) throws MmsException {
        this(context, tag, null, null, uri, region);
    }

    public RegionMediaModel(Context context, String tag, String contentType,
                            String src, Uri uri, RegionModel region) throws MmsException {
        super(context, tag, contentType, src, uri);
        mRegion = region;
    }

    public RegionMediaModel(Context context, String tag, String contentType,
                            String src, byte[] data, RegionModel region) {
        super(context, tag, contentType, src, data);
        mRegion = region;
    }

    public RegionModel getRegion() {
        return mRegion;
    }

    public void setRegion(RegionModel region) {
        mRegion = region;
        notifyModelChanged(true);
    }

    /**
     * @return the mVisible
     */
    public boolean isVisible() {
        return mVisible;
    }

    /**
     * @param visible the mVisible to set
     */
    public void setVisible(boolean visible) {
        mVisible = visible;
    }
}
