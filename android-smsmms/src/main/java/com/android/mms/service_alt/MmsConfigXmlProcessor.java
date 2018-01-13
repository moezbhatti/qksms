/*
 * Copyright (C) 2015 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.service_alt;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentValues;
import com.klinker.android.logger.Log;

import java.io.IOException;

/*
 * XML processor for mms_config.xml
 */
public class MmsConfigXmlProcessor {
    private static final String TAG = "MmsConfigXmlProcessor";

    public interface MmsConfigHandler {
        public void process(String key, String value, String type);
    }

    private static final String TAG_MMS_CONFIG = "mms_config";

    // Handler to process one mms_config key/value pair
    private MmsConfigHandler mMmsConfigHandler;

    private final StringBuilder mLogStringBuilder = new StringBuilder();

    private final XmlPullParser mInputParser;

    private MmsConfigXmlProcessor(XmlPullParser parser) {
        mInputParser = parser;
        mMmsConfigHandler = null;
    }

    public static MmsConfigXmlProcessor get(XmlPullParser parser) {
        return new MmsConfigXmlProcessor(parser);
    }

    public MmsConfigXmlProcessor setMmsConfigHandler(MmsConfigHandler handler) {
        mMmsConfigHandler = handler;
        return this;
    }

    /**
     * Move XML parser forward to next event type or the end of doc
     *
     * @param eventType
     * @return The final event type we meet
     * @throws XmlPullParserException
     * @throws IOException
     */
    private int advanceToNextEvent(int eventType) throws XmlPullParserException, IOException {
        for (;;) {
            int nextEvent = mInputParser.next();
            if (nextEvent == eventType
                    || nextEvent == XmlPullParser.END_DOCUMENT) {
                return nextEvent;
            }
        }
    }

    public void process() {
        try {
            // Find the first element
            if (advanceToNextEvent(XmlPullParser.START_TAG) != XmlPullParser.START_TAG) {
                throw new XmlPullParserException("MmsConfigXmlProcessor: expecting start tag @"
                        + xmlParserDebugContext());
            }
            // A single ContentValues object for holding the parsing result of
            // an apn element
            final ContentValues values = new ContentValues();
            String tagName = mInputParser.getName();
            // Top level tag can be "apns" (apns.xml, or APN OTA XML)
            // or "mms_config" (mms_config.xml)
            if (TAG_MMS_CONFIG.equals(tagName)) {
                // mms_config.xml resource
                processMmsConfig();
            }
        } catch (IOException e) {
            Log.e(TAG, "MmsConfigXmlProcessor: I/O failure " + e, e);
        } catch (XmlPullParserException e) {
            Log.e(TAG, "MmsConfigXmlProcessor: parsing failure " + e, e);
        }
    }

    private static String xmlParserEventString(int event) {
        switch (event) {
            case XmlPullParser.START_DOCUMENT: return "START_DOCUMENT";
            case XmlPullParser.END_DOCUMENT: return "END_DOCUMENT";
            case XmlPullParser.START_TAG: return "START_TAG";
            case XmlPullParser.END_TAG: return "END_TAG";
            case XmlPullParser.TEXT: return "TEXT";
        }
        return Integer.toString(event);
    }

    /**
     * @return The debugging information of the parser's current position
     */
    private String xmlParserDebugContext() {
        mLogStringBuilder.setLength(0);
        if (mInputParser != null) {
            try {
                final int eventType = mInputParser.getEventType();
                mLogStringBuilder.append(xmlParserEventString(eventType));
                if (eventType == XmlPullParser.START_TAG
                        || eventType == XmlPullParser.END_TAG
                        || eventType == XmlPullParser.TEXT) {
                    mLogStringBuilder.append('<').append(mInputParser.getName());
                    for (int i = 0; i < mInputParser.getAttributeCount(); i++) {
                        mLogStringBuilder.append(' ')
                            .append(mInputParser.getAttributeName(i))
                            .append('=')
                            .append(mInputParser.getAttributeValue(i));
                    }
                    mLogStringBuilder.append("/>");
                }
                return mLogStringBuilder.toString();
            } catch (XmlPullParserException e) {
                Log.e(TAG, "xmlParserDebugContext: " + e, e);
            }
        }
        return "Unknown";
    }

    /**
     * Process one mms_config.
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    private void processMmsConfig()
            throws IOException, XmlPullParserException {
        // We are at the start tag
        for (;;) {
            int nextEvent;
            // Skipping spaces
            while ((nextEvent = mInputParser.next()) == XmlPullParser.TEXT);
            if (nextEvent == XmlPullParser.START_TAG) {
                // Parse one mms config key/value
                processMmsConfigKeyValue();
            } else if (nextEvent == XmlPullParser.END_TAG) {
                break;
            } else {
                throw new XmlPullParserException("MmsConfig: expecting start or end tag @"
                        + xmlParserDebugContext());
            }
        }
    }

    /**
     * Process one mms_config key/value pair
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    private void processMmsConfigKeyValue() throws IOException, XmlPullParserException {
        final String key = mInputParser.getAttributeValue(null, "name");
        // We are at the start tag, the name of the tag is the type
        // e.g. <int name="key">value</int>
        final String type = mInputParser.getName();
        int nextEvent = mInputParser.next();
        String value = null;
        if (nextEvent == XmlPullParser.TEXT) {
            value = mInputParser.getText();
            nextEvent = mInputParser.next();
        }
        if (nextEvent != XmlPullParser.END_TAG) {
            throw new XmlPullParserException("MmsConfigXmlProcessor: expecting end tag @"
                    + xmlParserDebugContext());
        }
        if (MmsConfig.isValidKey(key, type)) {
            // We are done parsing one mms_config key/value, call the handler
            if (mMmsConfigHandler != null) {
                mMmsConfigHandler.process(key, value, type);
            }
        } else {
            Log.w(TAG, "MmsConfig: invalid key=" + key + " or type=" + type);
        }
    }
}
