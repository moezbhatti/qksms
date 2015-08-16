/*
 * Copyright (C) 2015 QK Labs
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

package com.moez.QKSMS.mmssms;

import android.net.wifi.WifiInfo;

/**
 * Class to house all of the settings that can be used to send a message
 */
public class Settings {

    public static final long MAX_ATTACHMENT_SIZE_UNLIMITED = -1;
    public static final long MAX_ATTACHMENT_SIZE_300KB = 300 * 1024;
    public static final long MAX_ATTACHMENT_SIZE_600KB = 600 * 1024;
    public static final long MAX_ATTACHMENT_SIZE_1MB = 1024 * 1024;

    // MMS options
    private String mmsc;
    private String proxy;
    private String port;
    private String userAgent;
    private String uaProfUrl;
    private String uaProfTagName;
    private boolean group;
    private long maxAttachmentSize;

    // SMS options
    private boolean deliveryReports;
    private boolean split;
    private boolean splitCounter;
    private boolean stripUnicode;
    private String signature;
    private String preText;
    private boolean sendLongAsMms;
    private int sendLongAsMmsAfter;

    // Google Voice settings
    private String account;
    private String rnrSe;

    /**
     * Default constructor to set everything to default values
     */
    public Settings() {
        this("", "", "0", true, MAX_ATTACHMENT_SIZE_UNLIMITED, false, false, false, false, false, "", "", true, 3, "", null);
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
        this.maxAttachmentSize = s.getMaxAttachmentSize();
        this.wifiMmsFix = s.getWifiMmsFix();
        this.deliveryReports = s.getDeliveryReports();
        this.split = s.getSplit();
        this.splitCounter = s.getSplitCounter();
        this.stripUnicode = s.getStripUnicode();
        this.signature = s.getSignature();
        this.preText = s.getPreText();
        this.sendLongAsMms = s.getSendLongAsMms();
        this.sendLongAsMmsAfter = s.getSendLongAsMmsAfter();
        this.account = s.getAccount();
        this.rnrSe = s.getRnrSe();
    }

    /**
     * @param mmsc               is the address contained by the apn to send MMS to
     * @param proxy              is the proxy address in the apn to send MMS through
     * @param port               is the port from the apn to send MMS through
     * @param group              is a boolean specifying whether or not to send messages with multiple recipients as a group MMS message
     * @param maxAttachmentSize  is a long specifying the maximum number of bytes that an MMS attachment can be, or -1 if no maximum
     * @param wifiMmsFix         is a boolean to toggle on and off wifi when sending MMS (MMS will not work currently when WiFi is enabled)
     * @param deliveryReports    is a boolean to retrieve delivery reports from SMS messages
     * @param split              is a boolean to manually split messages (shouldn't be necessary, but some carriers do not split on their own)
     * @param splitCounter       adds a split counter to the front of all split messages
     * @param stripUnicode       replaces many unicode characters with their gsm compatible equivalent to allow for sending 160 characters instead of 70
     * @param signature          a signature to attach at the end of each message
     * @param sendLongAsMms      if a message is too long to be multiple SMS, convert it to a single MMS
     * @param sendLongAsMmsAfter is an int of how many pages long an SMS must be before it is split
     * @param account            is the google account to send Google Voice messages through
     * @param rnrSe              is the token to use to send Google Voice messages (nullify if you don't know what this is)
     * @deprecated Constructor to create object of all values
     */
    public Settings(String mmsc, String proxy, String port, boolean group, long maxAttachmentSize, boolean wifiMmsFix, boolean deliveryReports, boolean split, boolean splitCounter, boolean stripUnicode, String signature, String preText, boolean sendLongAsMms, int sendLongAsMmsAfter, String account, String rnrSe) {
        this.mmsc = mmsc;
        this.proxy = proxy;
        this.port = port;
        this.userAgent = "";
        this.uaProfUrl = "";
        this.uaProfTagName = "";
        this.group = group;
        this.maxAttachmentSize = maxAttachmentSize;
        this.wifiMmsFix = wifiMmsFix;
        this.deliveryReports = deliveryReports;
        this.split = split;
        this.splitCounter = splitCounter;
        this.stripUnicode = stripUnicode;
        this.signature = signature;
        this.preText = preText;
        this.sendLongAsMms = sendLongAsMms;
        this.sendLongAsMmsAfter = sendLongAsMmsAfter;
        this.account = account;
        this.rnrSe = rnrSe;
    }

    /**
     * @param mmsc               is the address contained by the apn to send MMS to
     * @param proxy              is the proxy address in the apn to send MMS through
     * @param port               is the port from the apn to send MMS through
     * @param group              is a boolean specifying whether or not to send messages with multiple recipients as a group MMS message
     * @param maxAttachmentSize  is a long specifying the maximum number of bytes that an MMS attachment can be, or -1 if no maximum
     * @param deliveryReports    is a boolean to retrieve delivery reports from SMS messages
     * @param split              is a boolean to manually split messages (shouldn't be necessary, but some carriers do not split on their own)
     * @param splitCounter       adds a split counter to the front of all split messages
     * @param stripUnicode       replaces many unicode characters with their gsm compatible equivalent to allow for sending 160 characters instead of 70
     * @param signature          a signature to attach at the end of each message
     * @param preText            text to be inserted before a message
     * @param sendLongAsMms      if a message is too long to be multiple SMS, convert it to a single MMS
     * @param sendLongAsMmsAfter is an int of how many pages long an SMS must be before it is split
     * @param account            is the google account to send Google Voice messages through
     * @param rnrSe              is the token to use to send Google Voice messages (nullify if you don't know what this is)
     */
    public Settings(String mmsc, String proxy, String port, boolean group, long maxAttachmentSize, boolean deliveryReports, boolean split, boolean splitCounter, boolean stripUnicode, String signature, String preText, boolean sendLongAsMms, int sendLongAsMmsAfter, String account, String rnrSe) {
        this(mmsc, proxy, port, group, maxAttachmentSize, false, deliveryReports, split, splitCounter, stripUnicode, signature, preText, sendLongAsMms, sendLongAsMmsAfter, account, rnrSe);
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
     * Sets the max attachment size
     *
     * @param maxAttachmentSize  is a long specifying the maximum number of bytes that an MMS attachment can be, or -1 if no maximum
     */
    public void setMaxAttachmentSize(long maxAttachmentSize) {
        this.maxAttachmentSize = maxAttachmentSize;
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
     * Sets the Google account to send Voice messages through
     *
     * @param account is the google account to send Google Voice messages through
     */
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * Sets the token to use to authenticate voice messages
     *
     * @param rnrSe is the token to use to send Google Voice messages (nullify if you don't know what this is)
     */
    public void setRnrSe(String rnrSe) {
        this.rnrSe = rnrSe;
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
     * @return maxAttachmentSize  is a long specifying the maximum number of bytes that an MMS attachment can be, or -1 if no maximum
     */
    public long getMaxAttachmentSize() {
        return this.maxAttachmentSize;
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
     * @return Google account to send Voice messages through
     */
    public String getAccount() {
        return this.account;
    }

    /**
     * @return auth token to be used for Voice messages
     */
    public String getRnrSe() {
        return this.rnrSe;
    }

    /**
     * @deprecated
     */
    private boolean wifiMmsFix;

    /**
     * @deprecated
     */
    public WifiInfo currentWifi;

    /**
     * @deprecated
     */
    public boolean currentWifiState;

    /**
     * @deprecated
     */
    public DisconnectWifi discon;

    /**
     * @deprecated
     */
    public boolean currentDataState;

    /**
     * @param wifiMmsFix is a boolean to toggle on and off wifi when sending MMS
     * @deprecated Sets wifi mms fix
     */
    public void setWifiMmsFix(boolean wifiMmsFix) {
        this.wifiMmsFix = wifiMmsFix;
    }

    /**
     * @return whether or not to toggle wifi when sending MMS
     * @deprecated
     */
    public boolean getWifiMmsFix() {
        return this.wifiMmsFix;
    }
}
