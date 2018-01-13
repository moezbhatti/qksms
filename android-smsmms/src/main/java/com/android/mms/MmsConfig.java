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

package com.android.mms;

import java.io.IOException;

import com.klinker.android.send_message.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;
import com.klinker.android.logger.Log;

public class MmsConfig {
    private static final String TAG = "MmsConfig";
    private static final boolean DEBUG = true;
    private static final boolean LOCAL_LOGV = false;

    public static final String DEFAULT_HTTP_KEY_X_WAP_PROFILE = "x-wap-profile";
    public static final String DEFAULT_USER_AGENT = "Android-Mms/2.0";

    private static final String MMS_APP_PACKAGE = "com.android.mms";

    private static final String SMS_PROMO_DISMISSED_KEY = "sms_promo_dismissed_key";

    private static final int MAX_IMAGE_HEIGHT = 480;
    private static final int MAX_IMAGE_WIDTH = 640;
    private static final int MAX_TEXT_LENGTH = 2000;

    /**
     * Whether to hide MMS functionality from the user (i.e. SMS only).
     */
    private static boolean mTransIdEnabled = false;
    private static boolean mMmsEnabled = true;                  // default to true
    private static int mMaxMessageSize = 800 * 1024;            // default to 800k max size
    private static String mUserAgent = DEFAULT_USER_AGENT;
    private static String mUaProfTagName = DEFAULT_HTTP_KEY_X_WAP_PROFILE;
    private static String mUaProfUrl = null;
    private static String mHttpParams = null;
    private static String mHttpParamsLine1Key = null;
    private static String mEmailGateway = null;
    private static int mMaxImageHeight = MAX_IMAGE_HEIGHT;      // default value
    private static int mMaxImageWidth = MAX_IMAGE_WIDTH;        // default value
    private static int mRecipientLimit = Integer.MAX_VALUE;     // default value
    private static int mDefaultSMSMessagesPerThread = 10000;    // default value
    private static int mDefaultMMSMessagesPerThread = 1000;     // default value
    private static int mMinMessageCountPerThread = 2;           // default value
    private static int mMaxMessageCountPerThread = 5000;        // default value
    private static int mHttpSocketTimeout = 60*1000;            // default to 1 min
    private static int mMinimumSlideElementDuration = 7;        // default to 7 sec
    private static boolean mNotifyWapMMSC = false;
    private static boolean mAllowAttachAudio = true;

    // If mEnableMultipartSMS is true, long sms messages are always sent as multi-part sms
    // messages, with no checked limit on the number of segments.
    // If mEnableMultipartSMS is false, then as soon as the user types a message longer
    // than a single segment (i.e. 140 chars), then the message will turn into and be sent
    // as an mms message. This feature exists for carriers that don't support multi-part sms's.
    private static boolean mEnableMultipartSMS = true;
    
    // By default, the radio splits multipart sms, not the application. If the carrier or radio
    // does not support this, and the recipient gets garbled text, set this to true. If this is
    // true and mEnableMultipartSMS is false, the mSmsToMmsTextThreshold will be observed,
    // converting to mms if we reach the required number of segments.
    private static boolean mEnableSplitSMS = false;

    // If mEnableMultipartSMS is true and mSmsToMmsTextThreshold > 1, then multi-part SMS messages
    // will be converted into a single mms message. For example, if the mms_config.xml file
    // specifies <int name="smsToMmsTextThreshold">4</int>, then on the 5th sms segment, the
    // message will be converted to an mms.
    private static int mSmsToMmsTextThreshold = -1;

    private static boolean mEnableSlideDuration = true;
    private static boolean mEnableMMSReadReports = true;        // key: "enableMMSReadReports"
    private static boolean mEnableSMSDeliveryReports = true;    // key: "enableSMSDeliveryReports"
    private static boolean mEnableMMSDeliveryReports = true;    // key: "enableMMSDeliveryReports"
    private static int mMaxTextLength = -1;

    // This is the max amount of storage multiplied by mMaxMessageSize that we
    // allow of unsent messages before blocking the user from sending any more
    // MMS's.
    private static int mMaxSizeScaleForPendingMmsAllowed = 4;       // default value

    // Email gateway alias support, including the master switch and different rules
    private static boolean mAliasEnabled = false;
    private static int mAliasRuleMinChars = 2;
    private static int mAliasRuleMaxChars = 48;

    private static int mMaxSubjectLength = 40;  // maximum number of characters allowed for mms
                                                // subject

    // If mEnableGroupMms is true, a message with multiple recipients, regardless of contents,
    // will be sent as a single MMS message with multiple "TO" fields set for each recipient.
    // If mEnableGroupMms is false, the group MMS setting/preference will be hidden in the settings
    // activity.
    private static boolean mEnableGroupMms = true;

    public static void init(Context context) {
        if (LOCAL_LOGV) {
            Log.v(TAG, "MmsConfig.init()");
        }
        // Always put the mnc/mcc in the log so we can tell which mms_config.xml was loaded.

        loadMmsSettings(context);
    }

    public static boolean getMmsEnabled() {
        return mMmsEnabled;
    }

    public static int getMaxMessageSize() {
        if (LOCAL_LOGV) {
            Log.v(TAG, "MmsConfig.getMaxMessageSize(): " + mMaxMessageSize);
        }
       return mMaxMessageSize;
    }

    /**
     * This function returns the value of "enabledTransID" present in mms_config file.
     * In case of single segment wap push message, this "enabledTransID" indicates whether
     * TransactionID should be appended to URI or not.
     */
    public static boolean getTransIdEnabled() {
        return mTransIdEnabled;
    }

    public static String getUserAgent() {
        return mUserAgent;
    }

    public static String getUaProfTagName() {
        return mUaProfTagName;
    }

    public static String getUaProfUrl() {
        return mUaProfUrl;
    }

    public static String getHttpParams() {
        return mHttpParams;
    }

    public static String getHttpParamsLine1Key() {
        return mHttpParamsLine1Key;
    }

    public static int getHttpSocketTimeout() {
        return mHttpSocketTimeout;
    }

    public static boolean getNotifyWapMMSC() {
        return mNotifyWapMMSC;
    }

    public static final void beginDocument(XmlPullParser parser, String firstElementName) throws XmlPullParserException, IOException
    {
        int type;
        while ((type=parser.next()) != parser.START_TAG
                   && type != parser.END_DOCUMENT) {
            ;
        }

        if (type != parser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }

        if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() +
                    ", expected " + firstElementName);
        }
    }

    public static final void nextElement(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        int type;
        while ((type=parser.next()) != parser.START_TAG
                   && type != parser.END_DOCUMENT) {
            ;
        }
    }

    private static void loadMmsSettings(Context context) {
        XmlResourceParser parser = context.getResources().getXml(R.xml.mms_config);

        try {
            beginDocument(parser, "mms_config");

            while (true) {
                nextElement(parser);
                String tag = parser.getName();
                if (tag == null) {
                    break;
                }
                String name = parser.getAttributeName(0);
                String value = parser.getAttributeValue(0);
                String text = null;
                if (parser.next() == XmlPullParser.TEXT) {
                    text = parser.getText();
                }

                if (DEBUG) {
                    Log.v(TAG, "tag: " + tag + " value: " + value + " - " +
                            text);
                }
                if ("name".equalsIgnoreCase(name)) {
                    if ("bool".equals(tag)) {
                        // bool config tags go here
                        if ("enabledMMS".equalsIgnoreCase(value)) {
                            mMmsEnabled = "true".equalsIgnoreCase(text);
                        } else if ("enabledTransID".equalsIgnoreCase(value)) {
                            mTransIdEnabled = "true".equalsIgnoreCase(text);
                        } else if ("enabledNotifyWapMMSC".equalsIgnoreCase(value)) {
                            mNotifyWapMMSC = "true".equalsIgnoreCase(text);
                        } else if ("aliasEnabled".equalsIgnoreCase(value)) {
                            mAliasEnabled = "true".equalsIgnoreCase(text);
                        } else if ("allowAttachAudio".equalsIgnoreCase(value)) {
                            mAllowAttachAudio = "true".equalsIgnoreCase(text);
                        } else if ("enableMultipartSMS".equalsIgnoreCase(value)) {
                            mEnableMultipartSMS = "true".equalsIgnoreCase(text);
                        } else if ("enableSplitSMS".equalsIgnoreCase(value)) {
                            mEnableSplitSMS = "true".equalsIgnoreCase(text);
                        } else if ("enableSlideDuration".equalsIgnoreCase(value)) {
                            mEnableSlideDuration = "true".equalsIgnoreCase(text);
                        } else if ("enableMMSReadReports".equalsIgnoreCase(value)) {
                            mEnableMMSReadReports = "true".equalsIgnoreCase(text);
                        } else if ("enableSMSDeliveryReports".equalsIgnoreCase(value)) {
                            mEnableSMSDeliveryReports = "true".equalsIgnoreCase(text);
                        } else if ("enableMMSDeliveryReports".equalsIgnoreCase(value)) {
                            mEnableMMSDeliveryReports = "true".equalsIgnoreCase(text);
                        } else if ("enableGroupMms".equalsIgnoreCase(value)) {
                            mEnableGroupMms = "true".equalsIgnoreCase(text);
                        }
                    } else if ("int".equals(tag)) {
                        // int config tags go here
                        if ("maxMessageSize".equalsIgnoreCase(value)) {
                            mMaxMessageSize = Integer.parseInt(text);
                        } else if ("maxImageHeight".equalsIgnoreCase(value)) {
                            mMaxImageHeight = Integer.parseInt(text);
                        } else if ("maxImageWidth".equalsIgnoreCase(value)) {
                            mMaxImageWidth = Integer.parseInt(text);
                        } else if ("defaultSMSMessagesPerThread".equalsIgnoreCase(value)) {
                            mDefaultSMSMessagesPerThread = Integer.parseInt(text);
                        } else if ("defaultMMSMessagesPerThread".equalsIgnoreCase(value)) {
                            mDefaultMMSMessagesPerThread = Integer.parseInt(text);
                        } else if ("minMessageCountPerThread".equalsIgnoreCase(value)) {
                            mMinMessageCountPerThread = Integer.parseInt(text);
                        } else if ("maxMessageCountPerThread".equalsIgnoreCase(value)) {
                            mMaxMessageCountPerThread = Integer.parseInt(text);
                        } else if ("recipientLimit".equalsIgnoreCase(value)) {
                            mRecipientLimit = Integer.parseInt(text);
                            if (mRecipientLimit < 0) {
                                mRecipientLimit = Integer.MAX_VALUE;
                            }
                        } else if ("httpSocketTimeout".equalsIgnoreCase(value)) {
                            mHttpSocketTimeout = Integer.parseInt(text);
                        } else if ("minimumSlideElementDuration".equalsIgnoreCase(value)) {
                            mMinimumSlideElementDuration = Integer.parseInt(text);
                        } else if ("maxSizeScaleForPendingMmsAllowed".equalsIgnoreCase(value)) {
                            mMaxSizeScaleForPendingMmsAllowed = Integer.parseInt(text);
                        } else if ("aliasMinChars".equalsIgnoreCase(value)) {
                            mAliasRuleMinChars = Integer.parseInt(text);
                        } else if ("aliasMaxChars".equalsIgnoreCase(value)) {
                            mAliasRuleMaxChars = Integer.parseInt(text);
                        } else if ("smsToMmsTextThreshold".equalsIgnoreCase(value)) {
                            mSmsToMmsTextThreshold = Integer.parseInt(text);
                        } else if ("maxMessageTextSize".equalsIgnoreCase(value)) {
                            mMaxTextLength = Integer.parseInt(text);
                        } else if ("maxSubjectLength".equalsIgnoreCase(value)) {
                            mMaxSubjectLength = Integer.parseInt(text);
                        }
                    } else if ("string".equals(tag)) {
                        // string config tags go here
                        if ("userAgent".equalsIgnoreCase(value)) {
                            mUserAgent = text;
                        } else if ("uaProfTagName".equalsIgnoreCase(value)) {
                            mUaProfTagName = text;
                        } else if ("uaProfUrl".equalsIgnoreCase(value)) {
                            mUaProfUrl = text;
                        } else if ("httpParams".equalsIgnoreCase(value)) {
                            mHttpParams = text;
                        } else if ("httpParamsLine1Key".equalsIgnoreCase(value)) {
                            mHttpParamsLine1Key = text;
                        } else if ("emailGatewayNumber".equalsIgnoreCase(value)) {
                            mEmailGateway = text;
                        }
                    }
                }
            }
        } catch (XmlPullParserException e) {
            Log.e(TAG, "loadMmsSettings caught ", e);
        } catch (NumberFormatException e) {
            Log.e(TAG, "loadMmsSettings caught ", e);
        } catch (IOException e) {
            Log.e(TAG, "loadMmsSettings caught ", e);
        } finally {
            parser.close();
        }

        String errorStr = null;

        if (getMmsEnabled() && mUaProfUrl == null) {
            errorStr = "uaProfUrl";
        }

        if (errorStr != null) {
            String err =
                String.format("MmsConfig.loadMmsSettings mms_config.xml missing %s setting",
                        errorStr);
            Log.e(TAG, err);
        }
    }

    public static void setUserAgent(String userAgent) {
        MmsConfig.mUserAgent = userAgent;
    }

    public static void setUaProfUrl(String url) {
        MmsConfig.mUaProfUrl = url;
    }

    public static void setUaProfTagName(String tagName) {
        MmsConfig.mUaProfTagName = tagName;
    }

}
