/*
 * Copyright 2013 Jacob Klinker
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

package com.klinker.android.send_message;

import android.net.wifi.WifiInfo;
import android.os.Build;

import com.klinker.android.logger.Log;

/**
 * Class to house all of the settings that can be used to send a message
 *
 * @author Jake Klinker
 */
public class Settings {

    public static final int DEFAULT_SUBSCRIPTION_ID = -1;

    // MMS options
    private String mmsc;
    private String proxy;
    private String port;
    private String userAgent;
    private String uaProfUrl;
    private String uaProfTagName;
    private boolean group;
    private boolean useSystemSending;

    // SMS options
    private boolean deliveryReports;
    private boolean split;
    private boolean splitCounter;
    private boolean stripUnicode;
    private String signature;
    private String preText;
    private boolean sendLongAsMms;
    private int sendLongAsMmsAfter;

    // SIM options
    private int subscriptionId = DEFAULT_SUBSCRIPTION_ID;

    /**
     * Default constructor to set everything to default values
     */
    public Settings() {
        this("", "", "0", true, false, false, false, false, "", "", true, 3, true, DEFAULT_SUBSCRIPTION_ID);
    }

    /**
     * Copy constuctor
     * @param s is the Settings object to copy from
     */
    public Settings(Settings s) {
        this.mmsc = s.getMmsc();
        this.proxy = s.getProxy();
        this.port = s.getPort();
        this.userAgent = s.getAgent();
        this.uaProfUrl = s.getUserProfileUrl();
        this.uaProfTagName = s.getUaProfTagName();
        this.group = s.getGroup();
        this.deliveryReports = s.getDeliveryReports();
        this.split = s.getSplit();
        this.splitCounter = s.getSplitCounter();
        this.stripUnicode = s.getStripUnicode();
        this.signature = s.getSignature();
        this.preText = s.getPreText();
        this.sendLongAsMms = s.getSendLongAsMms();
        this.sendLongAsMmsAfter = s.getSendLongAsMmsAfter();
        this.subscriptionId = s.getSubscriptionId();
    }

    /**
     * @param mmsc               is the address contained by the apn to send MMS to
     * @param proxy              is the proxy address in the apn to send MMS through
     * @param port               is the port from the apn to send MMS through
     * @param group              is a boolean specifying whether or not to send messages with multiple recipients as a group MMS message
     * @param deliveryReports    is a boolean to retrieve delivery reports from SMS messages
     * @param split              is a boolean to manually split messages (shouldn't be necessary, but some carriers do not split on their own)
     * @param splitCounter       adds a split counter to the front of all split messages
     * @param stripUnicode       replaces many unicode characters with their gsm compatible equivalent to allow for sending 160 characters instead of 70
     * @param signature          a signature to attach at the end of each message
     * @param sendLongAsMms      if a message is too long to be multiple SMS, convert it to a single MMS
     * @param sendLongAsMmsAfter is an int of how many pages long an SMS must be before it is split
     * @param subscriptionId     is the ID for the SIM card. Can be null unless you are trying to use dual SIM support
     */
    public Settings(String mmsc, String proxy, String port, boolean group,
                    boolean deliveryReports, boolean split, boolean splitCounter,
                    boolean stripUnicode, String signature, String preText, boolean sendLongAsMms,
                    int sendLongAsMmsAfter, boolean useSystemSending, Integer subscriptionId) {
        this.mmsc = mmsc;
        this.proxy = proxy;
        this.port = port;
        this.userAgent = "";
        this.uaProfUrl = "";
        this.uaProfTagName = "";
        this.group = group;
        this.deliveryReports = deliveryReports;
        this.split = split;
        this.splitCounter = splitCounter;
        this.stripUnicode = stripUnicode;
        this.signature = signature;
        this.preText = preText;
        this.sendLongAsMms = sendLongAsMms;
        this.sendLongAsMmsAfter = sendLongAsMmsAfter;
        setUseSystemSending(useSystemSending);

        this.subscriptionId = subscriptionId != null ? subscriptionId : DEFAULT_SUBSCRIPTION_ID;
    }

    /**
     * Sets MMSC
     *
     * @param mmsc is the mmsc from the apns
     */
    public void setMmsc(String mmsc) {
        this.mmsc = mmsc;
    }

    /**
     * Sets the MMS Proxy
     *
     * @param proxy is the proxy from the apns
     */
    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    /**
     * Sets the Port
     *
     * @param port is the port from the apns
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * Sets the user agent
     *
     * @param agent is the agent to send http request with
     */
    public void setAgent(String agent) { this.userAgent = agent; }

    /**
     * Sets the user agent profile url
     *
     * @param userProfileUrl is the user agent profile url
     */
    public void setUserProfileUrl(String userProfileUrl) { this.uaProfUrl = userProfileUrl; }

    /**
     * Sets the user agent profile tag name
     *
     * @param tagName the tag name to use
     */
    public void setUaProfTagName(String tagName) { this.uaProfTagName = tagName; }

    /**
     * Sets group MMS messages
     *
     * @param group is a boolean specifying whether or not to send messages with multiple recipients as a group MMS message
     */
    public void setGroup(boolean group) {
        this.group = group;
    }

    /**
     * Sets whether to receive delivery reports from SMS messages
     *
     * @param deliveryReports is a boolean to retrieve delivery reports from SMS messages
     */
    public void setDeliveryReports(boolean deliveryReports) {
        this.deliveryReports = deliveryReports;
    }

    /**
     * Sets whether to manually split an SMS or not
     *
     * @param split is a boolean to manually split messages (shouldn't be necessary, but some carriers do not split on their own)
     */
    public void setSplit(boolean split) {
        this.split = split;
    }

    /**
     * Adds a split counter to the front of each split SMS
     *
     * @param splitCounter adds a split counter to the front of all split messages
     */
    public void setSplitCounter(boolean splitCounter) {
        this.splitCounter = splitCounter;
    }

    /**
     * Sets whether or not unicode characters should be sent or converted to their GSM compatible alternative
     *
     * @param stripUnicode replaces many unicode characters with their gsm compatible equivalent to allow for sending 160 characters instead of 70
     */
    public void setStripUnicode(boolean stripUnicode) {
        this.stripUnicode = stripUnicode;
    }

    /**
     * Sets a signature to be attached to each message
     *
     * @param signature a signature to attach at the end of each message
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * Sets the text to be sent before an SMS message
     *
     * @param preText text to be attached to the beginning of each message
     */
    public void setPreText(String preText) {
        this.preText = preText;
    }

    /**
     * Sets whether long SMS or Voice messages should instead be sent by a single MMS
     *
     * @param sendLongAsMms if a message is too long to be multiple SMS, convert it to a single MMS
     */
    public void setSendLongAsMms(boolean sendLongAsMms) {
        this.sendLongAsMms = sendLongAsMms;
    }

    /**
     * Sets when we should convert SMS or Voice into an MMS message
     *
     * @param sendLongAsMmsAfter is an int of how many pages long an SMS must be before it is split
     */
    public void setSendLongAsMmsAfter(int sendLongAsMmsAfter) {
        this.sendLongAsMmsAfter = sendLongAsMmsAfter;
    }

    /**
     * @param useSystemSending whether or not to use the system sending method on Lollipop+ devices
     */
    public void setUseSystemSending(boolean useSystemSending) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.useSystemSending = useSystemSending;
        } else {
            this.useSystemSending = false;
            Log.e("Settings", "System sending only available on Lollipop+ devices");
        }
    }

    /**
     * Set the subscription ID that should be used for sending. It isn't applied to receiving at this time.
     *
     * @param subscriptionId null if you do not want to use one.
     */
    public void setSubscriptionId(Integer subscriptionId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1 || subscriptionId == null) {
            // we won't allow you to go away from the default if your device doesn't support it
            this.subscriptionId = DEFAULT_SUBSCRIPTION_ID;
        } else {
            this.subscriptionId = subscriptionId;
        }
    }

    /**
     * @return MMSC to send through
     */
    public String getMmsc() {
        return this.mmsc;
    }

    /**
     * @return the proxy to send MMS through
     */
    public String getProxy() {
        return this.proxy;
    }

    /**
     * @return the port to send MMS through
     */
    public String getPort() {
        return this.port;
    }

    /**
     * @return the user agent to send mms with
     */
    public String getAgent() { return this.userAgent; }

    /**
     * @return the user agent profile url to send mms with
     */
    public String getUserProfileUrl() { return this.uaProfUrl; }

    /**
     * @return the user agent profile tag name
     */
    public String getUaProfTagName() { return this.uaProfTagName; }

    /**
     * @return whether or not to send Group MMS or multiple SMS/Voice messages
     */
    public boolean getGroup() {
        return this.group;
    }

    /**
     * @return whether or not to request delivery reports on SMS messages
     */
    public boolean getDeliveryReports() {
        return this.deliveryReports;
    }

    /**
     * @return whether or not SMS should be split manually
     */
    public boolean getSplit() {
        return this.split;
    }

    /**
     * @return whether or not a split counter should be attached to manually split messages
     */
    public boolean getSplitCounter() {
        return this.splitCounter;
    }

    /**
     * @return whether or not unicode chars should be substituted with gms characters
     */
    public boolean getStripUnicode() {
        return this.stripUnicode;
    }

    /**
     * @return the signature attached to SMS messages
     */
    public String getSignature() {
        return this.signature;
    }

    /**
     * @return the text attached to the beginning of each SMS
     */
    public String getPreText() {
        return this.preText;
    }

    /**
     * @return whether or not to send long SMS or Voice as single MMS
     */
    public boolean getSendLongAsMms() {
        return this.sendLongAsMms;
    }

    /**
     * @return number of pages sms must be to send instead as MMS
     */
    public int getSendLongAsMmsAfter() {
        return this.sendLongAsMmsAfter;
    }

    /**
     * @return whether or not to use the system sending method on Lollipop+ devices
     */
    public boolean getUseSystemSending() {
        return useSystemSending;
    }

    /**
     * @return the subscription ID or the default if one isn't set.
     */
    public int getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * Enables or disables logging for debug purposes logs will be written both to your devices
     * logcat log and an external file you can specify so that other users can assist with debugging easily
     * if you would like
     *
     * @param debugLogging true to enable debug logging when sending and receiving messages
     * @param path the external path on the sd card to write the log to
     */
    public static void setDebugLogging(boolean debugLogging, String path) {
        Log.setDebug(debugLogging);
        Log.setPath(path);
    }
}
