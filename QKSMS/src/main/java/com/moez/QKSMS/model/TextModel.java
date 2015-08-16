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
import android.util.Log;

import com.android.mms.dom.smil.SmilMediaElementImpl;
import com.google.android.mms.pdu_alt.CharacterSets;
import com.google.android.mms.smil.SmilHelper;

import org.w3c.dom.events.Event;
import org.w3c.dom.smil.ElementTime;

import java.io.UnsupportedEncodingException;

public class TextModel extends RegionMediaModel {
    private static final String TAG = "Mms/text";

    private CharSequence mText;
    private final int mCharset;

    public TextModel(Context context, String contentType, String src, RegionModel region) {
        this(context, contentType, src, CharacterSets.UTF_8, new byte[0], region);
    }

    public TextModel(Context context, String contentType, String src,
            int charset, byte[] data, RegionModel region) {
        super(context, SmilHelper.ELEMENT_TAG_TEXT, contentType, src,
                data != null ? data : new byte[0], region);

        if (charset == CharacterSets.ANY_CHARSET) {
            // By default, we use ISO_8859_1 to decode the data
            // which character set wasn't set.
            charset = CharacterSets.ISO_8859_1;
        }
        mCharset = charset;
        mText = extractTextFromData(data);
    }

    private CharSequence extractTextFromData(byte[] data) {
        if (data != null) {
            try {
                if (CharacterSets.ANY_CHARSET == mCharset) {
                    return new String(data); // system default encoding.
                } else {
                    String name = CharacterSets.getMimeName(mCharset);
                    return new String(data, name);
                }
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Unsupported encoding: " + mCharset, e);
                return new String(data); // system default encoding.
            }
        }
        return "";
    }

    public String getText() {
        if (mText == null) {
            mText = extractTextFromData(getData());
        }

        // If our internal CharSequence is not already a String,
        // re-save it as a String so subsequent calls to getText will
        // be less expensive.
        if (!(mText instanceof String)) {
            mText = mText.toString();
        }

        return mText.toString();
    }

    public void setText(CharSequence text) {
        mText = text;
        notifyModelChanged(true);
    }

    public void cloneText() {
        mText = new String((mText != null ? mText.toString() : ""));
    }

    public int getCharset() {
        return mCharset;
    }

    // EventListener Interface
    public void handleEvent(Event evt) {
        if (evt.getType().equals(SmilMediaElementImpl.SMIL_MEDIA_START_EVENT)) {
            mVisible = true;
        } else if (mFill != ElementTime.FILL_FREEZE) {
            mVisible = false;
        }

        notifyModelChanged(false);
    }
}
