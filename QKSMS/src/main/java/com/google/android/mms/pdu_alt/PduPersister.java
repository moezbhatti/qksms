/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
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

package com.google.android.mms.pdu_alt;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteException;
import android.drm.DrmManagerClient;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.mms.ContentType;
import com.google.android.mms.InvalidHeaderValueException;
import com.google.android.mms.MmsException;
import com.google.android.mms.util_alt.DownloadDrmHelper;
import com.google.android.mms.util_alt.DrmConvertSession;
import com.google.android.mms.util_alt.PduCache;
import com.google.android.mms.util_alt.PduCacheEntry;
import com.google.android.mms.util_alt.SqliteWrapper;
import com.moez.QKSMS.mmssms.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This class is the high-level manager of PDU storage.
 */
public class PduPersister {
    private static final String TAG = "PduPersister";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;

    private static final long DUMMY_THREAD_ID = Long.MAX_VALUE;

    /**
     * The uri of temporary drm objects.
     */
    public static final String TEMPORARY_DRM_OBJECT_URI =
        "content://mms/" + Long.MAX_VALUE + "/part";
    /**
     * Indicate that we transiently failed to process a MM.
     */
    public static final int PROC_STATUS_TRANSIENT_FAILURE   = 1;
    /**
     * Indicate that we permanently failed to process a MM.
     */
    public static final int PROC_STATUS_PERMANENTLY_FAILURE = 2;
    /**
     * Indicate that we have successfully processed a MM.
     */
    public static final int PROC_STATUS_COMPLETED           = 3;

    private static PduPersister sPersister;
    private static final PduCache PDU_CACHE_INSTANCE;

    private static final int[] ADDRESS_FIELDS = new int[] {
            PduHeaders.BCC,
            PduHeaders.CC,
            PduHeaders.FROM,
            PduHeaders.TO
    };

    private static final String[] PDU_PROJECTION = new String[] {
            "_id",
            "msg_box",
            "thread_id",
            "retr_txt",
            "sub",
            "ct_l",
            "ct_t",
            "m_cls",
            "m_id",
            "resp_txt",
            "tr_id",
            "ct_cls",
            "d_rpt",
            "m_type",
            "v",
            "pri",
            "rr",
            "resp_st",
            "rpt_a",
            "retr_st",
            "st",
            "date",
            "d_tm",
            "exp",
            "m_size",
            "sub_cs",
            "retr_txt_cs",
    };

    private static final int PDU_COLUMN_ID                    = 0;
    private static final int PDU_COLUMN_MESSAGE_BOX           = 1;
    private static final int PDU_COLUMN_THREAD_ID             = 2;
    private static final int PDU_COLUMN_RETRIEVE_TEXT         = 3;
    private static final int PDU_COLUMN_SUBJECT               = 4;
    private static final int PDU_COLUMN_CONTENT_LOCATION      = 5;
    private static final int PDU_COLUMN_CONTENT_TYPE          = 6;
    private static final int PDU_COLUMN_MESSAGE_CLASS         = 7;
    private static final int PDU_COLUMN_MESSAGE_ID            = 8;
    private static final int PDU_COLUMN_RESPONSE_TEXT         = 9;
    private static final int PDU_COLUMN_TRANSACTION_ID        = 10;
    private static final int PDU_COLUMN_CONTENT_CLASS         = 11;
    private static final int PDU_COLUMN_DELIVERY_REPORT       = 12;
    private static final int PDU_COLUMN_MESSAGE_TYPE          = 13;
    private static final int PDU_COLUMN_MMS_VERSION           = 14;
    private static final int PDU_COLUMN_PRIORITY              = 15;
    private static final int PDU_COLUMN_READ_REPORT           = 16;
    private static final int PDU_COLUMN_READ_STATUS           = 17;
    private static final int PDU_COLUMN_REPORT_ALLOWED        = 18;
    private static final int PDU_COLUMN_RETRIEVE_STATUS       = 19;
    private static final int PDU_COLUMN_STATUS                = 20;
    private static final int PDU_COLUMN_DATE                  = 21;
    private static final int PDU_COLUMN_DELIVERY_TIME         = 22;
    private static final int PDU_COLUMN_EXPIRY                = 23;
    private static final int PDU_COLUMN_MESSAGE_SIZE          = 24;
    private static final int PDU_COLUMN_SUBJECT_CHARSET       = 25;
    private static final int PDU_COLUMN_RETRIEVE_TEXT_CHARSET = 26;

    private static final String[] PART_PROJECTION = new String[] {
            "_id",
            "chset",
            "cd",
            "cid",
            "cl",
            "ct",
            "fn",
            "name",
            "text"
    };

    private static final int PART_COLUMN_ID                  = 0;
    private static final int PART_COLUMN_CHARSET             = 1;
    private static final int PART_COLUMN_CONTENT_DISPOSITION = 2;
    private static final int PART_COLUMN_CONTENT_ID          = 3;
    private static final int PART_COLUMN_CONTENT_LOCATION    = 4;
    private static final int PART_COLUMN_CONTENT_TYPE        = 5;
    private static final int PART_COLUMN_FILENAME            = 6;
    private static final int PART_COLUMN_NAME                = 7;
    private static final int PART_COLUMN_TEXT                = 8;

    private static final HashMap<Uri, Integer> MESSAGE_BOX_MAP;
    // These map are used for convenience in persist() and load().
    private static final HashMap<Integer, Integer> CHARSET_COLUMN_INDEX_MAP;
    private static final HashMap<Integer, Integer> ENCODED_STRING_COLUMN_INDEX_MAP;
    private static final HashMap<Integer, Integer> TEXT_STRING_COLUMN_INDEX_MAP;
    private static final HashMap<Integer, Integer> OCTET_COLUMN_INDEX_MAP;
    private static final HashMap<Integer, Integer> LONG_COLUMN_INDEX_MAP;
    private static final HashMap<Integer, String> CHARSET_COLUMN_NAME_MAP;
    private static final HashMap<Integer, String> ENCODED_STRING_COLUMN_NAME_MAP;
    private static final HashMap<Integer, String> TEXT_STRING_COLUMN_NAME_MAP;
    private static final HashMap<Integer, String> OCTET_COLUMN_NAME_MAP;
    private static final HashMap<Integer, String> LONG_COLUMN_NAME_MAP;

    static {
        MESSAGE_BOX_MAP = new HashMap<Uri, Integer>();
        MESSAGE_BOX_MAP.put(Uri.parse("content://mms/inbox"),  1);
        MESSAGE_BOX_MAP.put(Uri.parse("content://mms/sent"),   2);
        MESSAGE_BOX_MAP.put(Uri.parse("content://mms/drafts"),  3);
        MESSAGE_BOX_MAP.put(Uri.parse("content://mms/outbox"), 4);

        CHARSET_COLUMN_INDEX_MAP = new HashMap<Integer, Integer>();
        CHARSET_COLUMN_INDEX_MAP.put(PduHeaders.SUBJECT, PDU_COLUMN_SUBJECT_CHARSET);
        CHARSET_COLUMN_INDEX_MAP.put(PduHeaders.RETRIEVE_TEXT, PDU_COLUMN_RETRIEVE_TEXT_CHARSET);

        CHARSET_COLUMN_NAME_MAP = new HashMap<Integer, String>();
        CHARSET_COLUMN_NAME_MAP.put(PduHeaders.SUBJECT, "sub_cs");
        CHARSET_COLUMN_NAME_MAP.put(PduHeaders.RETRIEVE_TEXT, "retr_txt_cs");

        // Encoded string field code -> column index/name map.
        ENCODED_STRING_COLUMN_INDEX_MAP = new HashMap<Integer, Integer>();
        ENCODED_STRING_COLUMN_INDEX_MAP.put(PduHeaders.RETRIEVE_TEXT, PDU_COLUMN_RETRIEVE_TEXT);
        ENCODED_STRING_COLUMN_INDEX_MAP.put(PduHeaders.RESPONSE_TEXT, PDU_COLUMN_RESPONSE_TEXT);
        ENCODED_STRING_COLUMN_INDEX_MAP.put(PduHeaders.SUBJECT, PDU_COLUMN_SUBJECT);

        ENCODED_STRING_COLUMN_NAME_MAP = new HashMap<Integer, String>();
        ENCODED_STRING_COLUMN_NAME_MAP.put(PduHeaders.RETRIEVE_TEXT, "retr_txt");
        ENCODED_STRING_COLUMN_NAME_MAP.put(PduHeaders.RESPONSE_TEXT, "resp_txt");
        ENCODED_STRING_COLUMN_NAME_MAP.put(PduHeaders.SUBJECT, "sub");

        // Text string field code -> column index/name map.
        TEXT_STRING_COLUMN_INDEX_MAP = new HashMap<Integer, Integer>();
        TEXT_STRING_COLUMN_INDEX_MAP.put(PduHeaders.CONTENT_LOCATION, PDU_COLUMN_CONTENT_LOCATION);
        TEXT_STRING_COLUMN_INDEX_MAP.put(PduHeaders.CONTENT_TYPE, PDU_COLUMN_CONTENT_TYPE);
        TEXT_STRING_COLUMN_INDEX_MAP.put(PduHeaders.MESSAGE_CLASS, PDU_COLUMN_MESSAGE_CLASS);
        TEXT_STRING_COLUMN_INDEX_MAP.put(PduHeaders.MESSAGE_ID, PDU_COLUMN_MESSAGE_ID);
        TEXT_STRING_COLUMN_INDEX_MAP.put(PduHeaders.TRANSACTION_ID, PDU_COLUMN_TRANSACTION_ID);

        TEXT_STRING_COLUMN_NAME_MAP = new HashMap<Integer, String>();
        TEXT_STRING_COLUMN_NAME_MAP.put(PduHeaders.CONTENT_LOCATION, "ct_l");
        TEXT_STRING_COLUMN_NAME_MAP.put(PduHeaders.CONTENT_TYPE, "ct_t");
        TEXT_STRING_COLUMN_NAME_MAP.put(PduHeaders.MESSAGE_CLASS, "m_cls");
        TEXT_STRING_COLUMN_NAME_MAP.put(PduHeaders.MESSAGE_ID, "m_id");
        TEXT_STRING_COLUMN_NAME_MAP.put(PduHeaders.TRANSACTION_ID, "tr_id");

        // Octet field code -> column index/name map.
        OCTET_COLUMN_INDEX_MAP = new HashMap<Integer, Integer>();
        OCTET_COLUMN_INDEX_MAP.put(PduHeaders.CONTENT_CLASS, PDU_COLUMN_CONTENT_CLASS);
        OCTET_COLUMN_INDEX_MAP.put(PduHeaders.DELIVERY_REPORT, PDU_COLUMN_DELIVERY_REPORT);
        OCTET_COLUMN_INDEX_MAP.put(PduHeaders.MESSAGE_TYPE, PDU_COLUMN_MESSAGE_TYPE);
        OCTET_COLUMN_INDEX_MAP.put(PduHeaders.MMS_VERSION, PDU_COLUMN_MMS_VERSION);
        OCTET_COLUMN_INDEX_MAP.put(PduHeaders.PRIORITY, PDU_COLUMN_PRIORITY);
        OCTET_COLUMN_INDEX_MAP.put(PduHeaders.READ_REPORT, PDU_COLUMN_READ_REPORT);
        OCTET_COLUMN_INDEX_MAP.put(PduHeaders.READ_STATUS, PDU_COLUMN_READ_STATUS);
        OCTET_COLUMN_INDEX_MAP.put(PduHeaders.REPORT_ALLOWED, PDU_COLUMN_REPORT_ALLOWED);
        OCTET_COLUMN_INDEX_MAP.put(PduHeaders.RETRIEVE_STATUS, PDU_COLUMN_RETRIEVE_STATUS);
        OCTET_COLUMN_INDEX_MAP.put(PduHeaders.STATUS, PDU_COLUMN_STATUS);

        OCTET_COLUMN_NAME_MAP = new HashMap<Integer, String>();
        OCTET_COLUMN_NAME_MAP.put(PduHeaders.CONTENT_CLASS, "ct_cls");
        OCTET_COLUMN_NAME_MAP.put(PduHeaders.DELIVERY_REPORT, "d_rpt");
        OCTET_COLUMN_NAME_MAP.put(PduHeaders.MESSAGE_TYPE, "m_type");
        OCTET_COLUMN_NAME_MAP.put(PduHeaders.MMS_VERSION, "v");
        OCTET_COLUMN_NAME_MAP.put(PduHeaders.PRIORITY, "pri");
        OCTET_COLUMN_NAME_MAP.put(PduHeaders.READ_REPORT, "rr");
        OCTET_COLUMN_NAME_MAP.put(PduHeaders.READ_STATUS, "read_status");
        OCTET_COLUMN_NAME_MAP.put(PduHeaders.REPORT_ALLOWED, "rpt_a");
        OCTET_COLUMN_NAME_MAP.put(PduHeaders.RETRIEVE_STATUS, "retr_st");
        OCTET_COLUMN_NAME_MAP.put(PduHeaders.STATUS, "st");

        // Long field code -> column index/name map.
        LONG_COLUMN_INDEX_MAP = new HashMap<Integer, Integer>();
        LONG_COLUMN_INDEX_MAP.put(PduHeaders.DATE, PDU_COLUMN_DATE);
        LONG_COLUMN_INDEX_MAP.put(PduHeaders.DELIVERY_TIME, PDU_COLUMN_DELIVERY_TIME);
        LONG_COLUMN_INDEX_MAP.put(PduHeaders.EXPIRY, PDU_COLUMN_EXPIRY);
        LONG_COLUMN_INDEX_MAP.put(PduHeaders.MESSAGE_SIZE, PDU_COLUMN_MESSAGE_SIZE);

        LONG_COLUMN_NAME_MAP = new HashMap<Integer, String>();
        LONG_COLUMN_NAME_MAP.put(PduHeaders.DATE, "date");
        LONG_COLUMN_NAME_MAP.put(PduHeaders.DELIVERY_TIME, "d_tm");
        LONG_COLUMN_NAME_MAP.put(PduHeaders.EXPIRY, "exp");
        LONG_COLUMN_NAME_MAP.put(PduHeaders.MESSAGE_SIZE, "m_size");

        PDU_CACHE_INSTANCE = PduCache.getInstance();
     }

    private final Context mContext;
    private final ContentResolver mContentResolver;
    private final DrmManagerClient mDrmManagerClient;
    private final TelephonyManager mTelephonyManager;

    private PduPersister(Context context) {
        mContext = context;
        mContentResolver = context.getContentResolver();
        mDrmManagerClient = new DrmManagerClient(context);
        mTelephonyManager = (TelephonyManager)context
                .getSystemService(Context.TELEPHONY_SERVICE);
     }

    /** Get(or create if not exist) an instance of PduPersister */
    public static PduPersister getPduPersister(Context context) {
        if ((sPersister == null) || !context.equals(sPersister.mContext)) {
            sPersister = new PduPersister(context);
        }

        return sPersister;
    }

    private void setEncodedStringValueToHeaders(
            Cursor c, int columnIndex,
            PduHeaders headers, int mapColumn) {
        String s = c.getString(columnIndex);
        if ((s != null) && (s.length() > 0)) {
            int charsetColumnIndex = CHARSET_COLUMN_INDEX_MAP.get(mapColumn);
            int charset = c.getInt(charsetColumnIndex);
            EncodedStringValue value = new EncodedStringValue(
                    charset, getBytes(s));
            headers.setEncodedStringValue(value, mapColumn);
        }
    }

    private void setTextStringToHeaders(
            Cursor c, int columnIndex,
            PduHeaders headers, int mapColumn) {
        String s = c.getString(columnIndex);
        if (s != null) {
            headers.setTextString(getBytes(s), mapColumn);
        }
    }

    private void setOctetToHeaders(
            Cursor c, int columnIndex,
            PduHeaders headers, int mapColumn) throws InvalidHeaderValueException {
        if (!c.isNull(columnIndex)) {
            int b = c.getInt(columnIndex);
            headers.setOctet(b, mapColumn);
        }
    }

    private void setLongToHeaders(
            Cursor c, int columnIndex,
            PduHeaders headers, int mapColumn) {
        if (!c.isNull(columnIndex)) {
            long l = c.getLong(columnIndex);
            headers.setLongInteger(l, mapColumn);
        }
    }

    private Integer getIntegerFromPartColumn(Cursor c, int columnIndex) {
        if (!c.isNull(columnIndex)) {
            return c.getInt(columnIndex);
        }
        return null;
    }

    private byte[] getByteArrayFromPartColumn(Cursor c, int columnIndex) {
        if (!c.isNull(columnIndex)) {
            return getBytes(c.getString(columnIndex));
        }
        return null;
    }

    private PduPart[] loadParts(long msgId) throws MmsException {
        Cursor c = SqliteWrapper.query(mContext, mContentResolver,
                Uri.parse("content://mms/" + msgId + "/part"),
                PART_PROJECTION, null, null, null);

        PduPart[] parts = null;

        try {
            if ((c == null) || (c.getCount() == 0)) {
                if (LOCAL_LOGV) Log.v(TAG, "loadParts(" + msgId + "): no part to load.");
                return null;
            }

            int partCount = c.getCount();
            int partIdx = 0;
            parts = new PduPart[partCount];
            while (c.moveToNext()) {
                PduPart part = new PduPart();
                Integer charset = getIntegerFromPartColumn(
                        c, PART_COLUMN_CHARSET);
                if (charset != null) {
                    part.setCharset(charset);
                }

                byte[] contentDisposition = getByteArrayFromPartColumn(
                        c, PART_COLUMN_CONTENT_DISPOSITION);
                if (contentDisposition != null) {
                    part.setContentDisposition(contentDisposition);
                }

                byte[] contentId = getByteArrayFromPartColumn(
                        c, PART_COLUMN_CONTENT_ID);
                if (contentId != null) {
                    part.setContentId(contentId);
                }

                byte[] contentLocation = getByteArrayFromPartColumn(
                        c, PART_COLUMN_CONTENT_LOCATION);
                if (contentLocation != null) {
                    part.setContentLocation(contentLocation);
                }

                byte[] contentType = getByteArrayFromPartColumn(
                        c, PART_COLUMN_CONTENT_TYPE);
                if (contentType != null) {
                    part.setContentType(contentType);
                } else {
                    throw new MmsException("Content-Type must be set.");
                }

                byte[] fileName = getByteArrayFromPartColumn(
                        c, PART_COLUMN_FILENAME);
                if (fileName != null) {
                    part.setFilename(fileName);
                }

                byte[] name = getByteArrayFromPartColumn(
                        c, PART_COLUMN_NAME);
                if (name != null) {
                    part.setName(name);
                }

                // Construct a Uri for this part.
                long partId = c.getLong(PART_COLUMN_ID);
                Uri partURI = Uri.parse("content://mms/part/" + partId);
                part.setDataUri(partURI);

                // For images/audio/video, we won't keep their data in Part
                // because their renderer accept Uri as source.
                String type = toIsoString(contentType);
                if (!ContentType.isImageType(type)
                        && !ContentType.isAudioType(type)
                        && !ContentType.isVideoType(type)) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    InputStream is = null;

                    // Store simple string values directly in the database instead of an
                    // external file.  This makes the text searchable and retrieval slightly
                    // faster.
                    if (ContentType.TEXT_PLAIN.equals(type) || ContentType.APP_SMIL.equals(type)
                            || ContentType.TEXT_HTML.equals(type)) {
                        String text = c.getString(PART_COLUMN_TEXT);
                        byte [] blob = new EncodedStringValue(text != null ? text : "")
                            .getTextString();
                        baos.write(blob, 0, blob.length);
                    } else {

                        try {
                            is = mContentResolver.openInputStream(partURI);

                            byte[] buffer = new byte[256];
                            int len = is.read(buffer);
                            while (len >= 0) {
                                baos.write(buffer, 0, len);
                                len = is.read(buffer);
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to load part data", e);
                            c.close();
                            throw new MmsException(e);
                        } finally {
                            if (is != null) {
                                try {
                                    is.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Failed to close stream", e);
                                } // Ignore
                            }
                        }
                    }
                    part.setData(baos.toByteArray());
                }
                parts[partIdx++] = part;
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return parts;
    }

    private void loadAddress(long msgId, PduHeaders headers) {
        Cursor c = SqliteWrapper.query(mContext, mContentResolver,
                Uri.parse("content://mms/" + msgId + "/addr"),
                new String[] { "address", "charset", "type" },
                null, null, null);

        if (c != null) {
            try {
                while (c.moveToNext()) {
                    String addr = c.getString(0);
                    if (!TextUtils.isEmpty(addr)) {
                        int addrType = c.getInt(2);
                        switch (addrType) {
                            case PduHeaders.FROM:
                                headers.setEncodedStringValue(
                                        new EncodedStringValue(c.getInt(1), getBytes(addr)),
                                        addrType);
                                break;
                            case PduHeaders.TO:
                            case PduHeaders.CC:
                            case PduHeaders.BCC:
                                headers.appendEncodedStringValue(
                                        new EncodedStringValue(c.getInt(1), getBytes(addr)),
                                        addrType);
                                break;
                            default:
                                Log.e(TAG, "Unknown address type: " + addrType);
                                break;
                        }
                    }
                }
            } finally {
                c.close();
            }
        }
    }

    /**
     * Load a PDU from storage by given Uri.
     *
     * @param uri The Uri of the PDU to be loaded.
     * @return A generic PDU object, it may be cast to dedicated PDU.
     * @throws MmsException Failed to load some fields of a PDU.
     */
    public GenericPdu load(Uri uri) throws MmsException {
        GenericPdu pdu = null;
        PduCacheEntry cacheEntry = null;
        int msgBox = 0;
        long threadId = DUMMY_THREAD_ID;
        try {
            synchronized(PDU_CACHE_INSTANCE) {
                if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                    if (LOCAL_LOGV) Log.v(TAG, "load: " + uri + " blocked by isUpdating()");
                    try {
                        PDU_CACHE_INSTANCE.wait();
                    } catch (InterruptedException e) {
                        Log.e(TAG, "load: ", e);
                    }
                    cacheEntry = PDU_CACHE_INSTANCE.get(uri);
                    if (cacheEntry != null) {
                        return cacheEntry.getPdu();
                    }
                }
                // Tell the cache to indicate to other callers that this item
                // is currently being updated.
                PDU_CACHE_INSTANCE.setUpdating(uri, true);
            }

            Cursor c = SqliteWrapper.query(mContext, mContentResolver, uri,
                    PDU_PROJECTION, null, null, null);
            PduHeaders headers = new PduHeaders();
            Set<Entry<Integer, Integer>> set;
            long msgId = ContentUris.parseId(uri);

            try {
                if ((c == null) || (c.getCount() != 1) || !c.moveToFirst()) {
                    throw new MmsException("Bad uri: " + uri);
                }

                msgBox = c.getInt(PDU_COLUMN_MESSAGE_BOX);
                threadId = c.getLong(PDU_COLUMN_THREAD_ID);

                set = ENCODED_STRING_COLUMN_INDEX_MAP.entrySet();
                for (Entry<Integer, Integer> e : set) {
                    setEncodedStringValueToHeaders(
                            c, e.getValue(), headers, e.getKey());
                }

                set = TEXT_STRING_COLUMN_INDEX_MAP.entrySet();
                for (Entry<Integer, Integer> e : set) {
                    setTextStringToHeaders(
                            c, e.getValue(), headers, e.getKey());
                }

                set = OCTET_COLUMN_INDEX_MAP.entrySet();
                for (Entry<Integer, Integer> e : set) {
                    setOctetToHeaders(
                            c, e.getValue(), headers, e.getKey());
                }

                set = LONG_COLUMN_INDEX_MAP.entrySet();
                for (Entry<Integer, Integer> e : set) {
                    setLongToHeaders(
                            c, e.getValue(), headers, e.getKey());
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }

            // Check whether 'msgId' has been assigned a valid value.
            if (msgId == -1L) {
                throw new MmsException("Error! ID of the message: -1.");
            }

            // Load address information of the MM.
            loadAddress(msgId, headers);

            int msgType = headers.getOctet(PduHeaders.MESSAGE_TYPE);
            PduBody body = new PduBody();

            // For PDU which type is M_retrieve.conf or Send.req, we should
            // load multiparts and put them into the body of the PDU.
            if ((msgType == PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF)
                    || (msgType == PduHeaders.MESSAGE_TYPE_SEND_REQ)) {
                PduPart[] parts = loadParts(msgId);
                if (parts != null) {
                    int partsNum = parts.length;
                    for (int i = 0; i < partsNum; i++) {
                        body.addPart(parts[i]);
                    }
                }
            }

            switch (msgType) {
            case PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND:
                pdu = new NotificationInd(headers);
                break;
            case PduHeaders.MESSAGE_TYPE_DELIVERY_IND:
                pdu = new DeliveryInd(headers);
                break;
            case PduHeaders.MESSAGE_TYPE_READ_ORIG_IND:
                pdu = new ReadOrigInd(headers);
                break;
            case PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF:
                pdu = new RetrieveConf(headers, body);
                break;
            case PduHeaders.MESSAGE_TYPE_SEND_REQ:
                pdu = new SendReq(headers, body);
                break;
            case PduHeaders.MESSAGE_TYPE_ACKNOWLEDGE_IND:
                pdu = new AcknowledgeInd(headers);
                break;
            case PduHeaders.MESSAGE_TYPE_NOTIFYRESP_IND:
                pdu = new NotifyRespInd(headers);
                break;
            case PduHeaders.MESSAGE_TYPE_READ_REC_IND:
                pdu = new ReadRecInd(headers);
                break;
            case PduHeaders.MESSAGE_TYPE_SEND_CONF:
            case PduHeaders.MESSAGE_TYPE_FORWARD_REQ:
            case PduHeaders.MESSAGE_TYPE_FORWARD_CONF:
            case PduHeaders.MESSAGE_TYPE_MBOX_STORE_REQ:
            case PduHeaders.MESSAGE_TYPE_MBOX_STORE_CONF:
            case PduHeaders.MESSAGE_TYPE_MBOX_VIEW_REQ:
            case PduHeaders.MESSAGE_TYPE_MBOX_VIEW_CONF:
            case PduHeaders.MESSAGE_TYPE_MBOX_UPLOAD_REQ:
            case PduHeaders.MESSAGE_TYPE_MBOX_UPLOAD_CONF:
            case PduHeaders.MESSAGE_TYPE_MBOX_DELETE_REQ:
            case PduHeaders.MESSAGE_TYPE_MBOX_DELETE_CONF:
            case PduHeaders.MESSAGE_TYPE_MBOX_DESCR:
            case PduHeaders.MESSAGE_TYPE_DELETE_REQ:
            case PduHeaders.MESSAGE_TYPE_DELETE_CONF:
            case PduHeaders.MESSAGE_TYPE_CANCEL_REQ:
            case PduHeaders.MESSAGE_TYPE_CANCEL_CONF:
                throw new MmsException(
                        "Unsupported PDU type: " + Integer.toHexString(msgType));

            default:
                throw new MmsException(
                        "Unrecognized PDU type: " + Integer.toHexString(msgType));
            }
        } finally {
            synchronized(PDU_CACHE_INSTANCE) {
                if (pdu != null) {
                    assert(PDU_CACHE_INSTANCE.get(uri) == null);
                    // Update the cache entry with the real info
                    cacheEntry = new PduCacheEntry(pdu, msgBox, threadId);
                    PDU_CACHE_INSTANCE.put(uri, cacheEntry);
                }
                PDU_CACHE_INSTANCE.setUpdating(uri, false);
                PDU_CACHE_INSTANCE.notifyAll(); // tell anybody waiting on this entry to go ahead
            }
        }
        return pdu;
    }

    private void persistAddress(
            long msgId, int type, EncodedStringValue[] array) {
        ContentValues values = new ContentValues(3);

        for (EncodedStringValue addr : array) {
            values.clear(); // Clear all values first.
            values.put("address", toIsoString(addr.getTextString()));
            values.put("charset", addr.getCharacterSet());
            values.put("type", type);

            Uri uri = Uri.parse("content://mms/" + msgId + "/addr");
            SqliteWrapper.insert(mContext, mContentResolver, uri, values);
        }
    }

    private static String getPartContentType(PduPart part) {
        return part.getContentType() == null ? null : toIsoString(part.getContentType());
    }

    public Uri persistPart(PduPart part, long msgId, HashMap<Uri, InputStream> preOpenedFiles)
            throws MmsException {
        Uri uri = Uri.parse("content://mms/" + msgId + "/part");
        ContentValues values = new ContentValues(8);

        int charset = part.getCharset();
        if (charset != 0 ) {
            values.put("chset", charset);
        }

        String contentType = getPartContentType(part);
        if (contentType != null) {
            // There is no "image/jpg" in Android (and it's an invalid mimetype).
            // Change it to "image/jpeg"
            if (ContentType.IMAGE_JPG.equals(contentType)) {
                contentType = ContentType.IMAGE_JPEG;
            }

            values.put("ct", contentType);
            // To ensure the SMIL part is always the first part.
            if (ContentType.APP_SMIL.equals(contentType)) {
                values.put("seq", -1);
            }
        } else {
            throw new MmsException("MIME type of the part must be set.");
        }

        if (part.getFilename() != null) {
            String fileName = new String(part.getFilename());
            values.put("fn", fileName);
        }

        if (part.getName() != null) {
            String name = new String(part.getName());
            values.put("name", name);
        }

        Object value = null;
        if (part.getContentDisposition() != null) {
            value = toIsoString(part.getContentDisposition());
            values.put("cd", (String) value);
        }

        if (part.getContentId() != null) {
            value = toIsoString(part.getContentId());
            values.put("cid", (String) value);
        }

        if (part.getContentLocation() != null) {
            value = toIsoString(part.getContentLocation());
            values.put("cl", (String) value);
        }

        Uri res = SqliteWrapper.insert(mContext, mContentResolver, uri, values);
        if (res == null) {
            throw new MmsException("Failed to persist part, return null.");
        }

        persistData(part, res, contentType, preOpenedFiles);
        // After successfully store the data, we should update
        // the dataUri of the part.
        part.setDataUri(res);

        return res;
    }

    /**
     * Save data of the part into storage. The source data may be given
     * by a byte[] or a Uri. If it's a byte[], directly save it
     * into storage, otherwise load source data from the dataUri and then
     * save it. If the data is an image, we may scale down it according
     * to user preference.
     *
     * @param part The PDU part which contains data to be saved.
     * @param uri The URI of the part.
     * @param contentType The MIME type of the part.
     * @param preOpenedFiles if not null, a map of preopened InputStreams for the parts.
     * @throws MmsException Cannot find source data or error occurred
     *         while saving the data.
     */
    private void persistData(PduPart part, Uri uri,
            String contentType, HashMap<Uri, InputStream> preOpenedFiles)
            throws MmsException {
        OutputStream os = null;
        InputStream is = null;
        DrmConvertSession drmConvertSession = null;
        Uri dataUri = null;
        String path = null;

        try {
            byte[] data = part.getData();
            if (ContentType.TEXT_PLAIN.equals(contentType)
                    || ContentType.APP_SMIL.equals(contentType)
                    || ContentType.TEXT_HTML.equals(contentType)) {
                ContentValues cv = new ContentValues();
                cv.put(Telephony.Mms.Part.TEXT, new EncodedStringValue(data).getString());
                if (mContentResolver.update(uri, cv, null, null) != 1) {
                    throw new MmsException("unable to update " + uri.toString());
                }
            } else {
                boolean isDrm = DownloadDrmHelper.isDrmConvertNeeded(contentType);
                if (isDrm) {
                    if (uri != null) {
                        try {
                            path = convertUriToPath(mContext, uri);
                            if (LOCAL_LOGV) Log.v(TAG, "drm uri: " + uri + " path: " + path);
                            File f = new File(path);
                            long len = f.length();
                            if (LOCAL_LOGV) Log.v(TAG, "drm path: " + path + " len: " + len);
                            if (len > 0) {
                                // we're not going to re-persist and re-encrypt an already
                                // converted drm file
                                return;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Can't getConversation file info for: " + part.getDataUri(), e);
                        }
                    }
                    // We haven't converted the file yet, start the conversion
                    drmConvertSession = DrmConvertSession.open(mContext, contentType);
                    if (drmConvertSession == null) {
                        throw new MmsException("Mimetype " + contentType +
                                " can not be converted.");
                    }
                }
                // uri can look like:
                // content://mms/part/98
                os = mContentResolver.openOutputStream(uri);
                if (data == null) {
                    dataUri = part.getDataUri();
                    if ((dataUri == null) || (dataUri == uri)) {
                        Log.w(TAG, "Can't find data for this part.");
                        return;
                    }
                    // dataUri can look like:
                    // content://com.google.android.gallery3d.provider/picasa/item/5720646660183715586
                    if (preOpenedFiles != null && preOpenedFiles.containsKey(dataUri)) {
                        is = preOpenedFiles.get(dataUri);
                    }
                    if (is == null) {
                        is = mContentResolver.openInputStream(dataUri);
                    }

                    if (LOCAL_LOGV) Log.v(TAG, "Saving data to: " + uri);

                    byte[] buffer = new byte[8192];
                    for (int len = 0; (len = is.read(buffer)) != -1; ) {
                        if (!isDrm) {
                            os.write(buffer, 0, len);
                        } else {
                            byte[] convertedData = drmConvertSession.convert(buffer, len);
                            if (convertedData != null) {
                                os.write(convertedData, 0, convertedData.length);
                            } else {
                                throw new MmsException("Error converting drm data.");
                            }
                        }
                    }
                } else {
                    if (LOCAL_LOGV) Log.v(TAG, "Saving data to: " + uri);
                    if (!isDrm) {
                        os.write(data);
                    } else {
                        dataUri = uri;
                        byte[] convertedData = drmConvertSession.convert(data, data.length);
                        if (convertedData != null) {
                            os.write(convertedData, 0, convertedData.length);
                        } else {
                            throw new MmsException("Error converting drm data.");
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Failed to open Input/Output stream.", e);
            throw new MmsException(e);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read/write data.", e);
            throw new MmsException(e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException while closing: " + os, e);
                } // Ignore
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException while closing: " + is, e);
                } // Ignore
            }
            if (drmConvertSession != null) {
                drmConvertSession.close(path);

                // Reset the permissions on the encrypted part file so everyone has only read
                // permission.
                File f = new File(path);
                ContentValues values = new ContentValues(0);
                SqliteWrapper.update(mContext, mContentResolver,
                                     Uri.parse("content://mms/resetFilePerm/" + f.getName()),
                                     values, null, null);
            }
        }
    }

    /**
     * This method expects uri in the following format
     *     content://media/<table_name>/<row_index> (or)
     *     file://sdcard/test.mp4
     *     http://test.com/test.mp4
     *
     * Here <table_name> shall be "video" or "audio" or "images"
     * <row_index> the index of the content in given table
     */
    static public String convertUriToPath(Context context, Uri uri) {
        String path = null;
        if (null != uri) {
            String scheme = uri.getScheme();
            if (null == scheme || scheme.equals("") ||
                    scheme.equals(ContentResolver.SCHEME_FILE)) {
                path = uri.getPath();

            } else if (scheme.equals("http")) {
                path = uri.toString();

            } else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
                String[] projection = new String[] {MediaStore.MediaColumns.DATA};
                Cursor cursor = null;
                try {
                    cursor = context.getContentResolver().query(uri, projection, null,
                            null, null);
                    if (null == cursor || 0 == cursor.getCount() || !cursor.moveToFirst()) {
                        throw new IllegalArgumentException("Given Uri could not be found" +
                                " in media store");
                    }
                    int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    path = cursor.getString(pathIndex);
                } catch (SQLiteException e) {
                    throw new IllegalArgumentException("Given Uri is not formatted in a way " +
                            "so that it can be found in media store.");
                } finally {
                    if (null != cursor) {
                        cursor.close();
                    }
                }
            } else {
                throw new IllegalArgumentException("Given Uri scheme is not supported");
            }
        }
        return path;
    }

    private void updateAddress(
            long msgId, int type, EncodedStringValue[] array) {
        // Delete old address information and then insert new ones.
        SqliteWrapper.delete(mContext, mContentResolver,
                Uri.parse("content://mms/" + msgId + "/addr"),
                "type" + "=" + type, null);

        persistAddress(msgId, type, array);
    }

    /**
     * Update headers of a SendReq.
     *
     * @param uri The PDU which need to be updated.
     * @param pdu New headers.
     * @throws MmsException Bad URI or updating failed.
     */
    public void updateHeaders(Uri uri, SendReq sendReq) {
        synchronized(PDU_CACHE_INSTANCE) {
            // If the cache item is getting updated, wait until it's done updating before
            // purging it.
            if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                if (LOCAL_LOGV) Log.v(TAG, "updateHeaders: " + uri + " blocked by isUpdating()");
                try {
                    PDU_CACHE_INSTANCE.wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "updateHeaders: ", e);
                }
            }
        }
        PDU_CACHE_INSTANCE.purge(uri);

        ContentValues values = new ContentValues(10);
        byte[] contentType = sendReq.getContentType();
        if (contentType != null) {
            values.put("ct_t", toIsoString(contentType));
        }

        long date = sendReq.getDate();
        if (date != -1) {
            values.put("date", date);
        }

        int deliveryReport = sendReq.getDeliveryReport();
        if (deliveryReport != 0) {
            values.put("d_rpt", deliveryReport);
        }

        long expiry = sendReq.getExpiry();
        if (expiry != -1) {
            values.put("exp", expiry);
        }

        byte[] msgClass = sendReq.getMessageClass();
        if (msgClass != null) {
            values.put("m_cls", toIsoString(msgClass));
        }

        int priority = sendReq.getPriority();
        if (priority != 0) {
            values.put("pri", priority);
        }

        int readReport = sendReq.getReadReport();
        if (readReport != 0) {
            values.put("rr", readReport);
        }

        byte[] transId = sendReq.getTransactionId();
        if (transId != null) {
            values.put("tr_id", toIsoString(transId));
        }

        EncodedStringValue subject = sendReq.getSubject();
        if (subject != null) {
            values.put("sub", toIsoString(subject.getTextString()));
            values.put("sub_cs", subject.getCharacterSet());
        } else {
            values.put("sub", "");
        }

        long messageSize = sendReq.getMessageSize();
        if (messageSize > 0) {
            values.put("m_size", messageSize);
        }

        PduHeaders headers = sendReq.getPduHeaders();
        HashSet<String> recipients = new HashSet<String>();
        for (int addrType : ADDRESS_FIELDS) {
            EncodedStringValue[] array = null;
            if (addrType == PduHeaders.FROM) {
                EncodedStringValue v = headers.getEncodedStringValue(addrType);
                if (v != null) {
                    array = new EncodedStringValue[1];
                    array[0] = v;
                }
            } else {
                array = headers.getEncodedStringValues(addrType);
            }

            if (array != null) {
                long msgId = ContentUris.parseId(uri);
                updateAddress(msgId, addrType, array);
                if (addrType == PduHeaders.TO) {
                    for (EncodedStringValue v : array) {
                        if (v != null) {
                            recipients.add(v.getString());
                        }
                    }
                }
            }
        }
        if (!recipients.isEmpty()) {
            long threadId = Utils.getOrCreateThreadId(mContext, recipients);
            values.put("thread_id", threadId);
        }

        SqliteWrapper.update(mContext, mContentResolver, uri, values, null, null);
    }

    private void updatePart(Uri uri, PduPart part, HashMap<Uri, InputStream> preOpenedFiles)
            throws MmsException {
        ContentValues values = new ContentValues(7);

        int charset = part.getCharset();
        if (charset != 0 ) {
            values.put("chset", charset);
        }

        String contentType = null;
        if (part.getContentType() != null) {
            contentType = toIsoString(part.getContentType());
            values.put("ct", contentType);
        } else {
            throw new MmsException("MIME type of the part must be set.");
        }

        if (part.getFilename() != null) {
            String fileName = new String(part.getFilename());
            values.put("fn", fileName);
        }

        if (part.getName() != null) {
            String name = new String(part.getName());
            values.put("name", name);
        }

        Object value = null;
        if (part.getContentDisposition() != null) {
            value = toIsoString(part.getContentDisposition());
            values.put("cd", (String) value);
        }

        if (part.getContentId() != null) {
            value = toIsoString(part.getContentId());
            values.put("cid", (String) value);
        }

        if (part.getContentLocation() != null) {
            value = toIsoString(part.getContentLocation());
            values.put("cl", (String) value);
        }

        SqliteWrapper.update(mContext, mContentResolver, uri, values, null, null);

        // Only update the data when:
        // 1. New binary data supplied or
        // 2. The Uri of the part is different from the current one.
        if ((part.getData() != null)
                || (uri != part.getDataUri())) {
            persistData(part, uri, contentType, preOpenedFiles);
        }
    }

    /**
     * Update all parts of a PDU.
     *
     * @param uri The PDU which need to be updated.
     * @param body New message body of the PDU.
     * @param preOpenedFiles if not null, a map of preopened InputStreams for the parts.
     * @throws MmsException Bad URI or updating failed.
     */
    public void updateParts(Uri uri, PduBody body, HashMap<Uri, InputStream> preOpenedFiles)
            throws MmsException {
        try {
            PduCacheEntry cacheEntry;
            synchronized(PDU_CACHE_INSTANCE) {
                if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                    if (LOCAL_LOGV) Log.v(TAG, "updateParts: " + uri + " blocked by isUpdating()");
                    try {
                        PDU_CACHE_INSTANCE.wait();
                    } catch (InterruptedException e) {
                        Log.e(TAG, "updateParts: ", e);
                    }
                    cacheEntry = PDU_CACHE_INSTANCE.get(uri);
                    if (cacheEntry != null) {
                        ((MultimediaMessagePdu) cacheEntry.getPdu()).setBody(body);
                    }
                }
                // Tell the cache to indicate to other callers that this item
                // is currently being updated.
                PDU_CACHE_INSTANCE.setUpdating(uri, true);
            }

            ArrayList<PduPart> toBeCreated = new ArrayList<PduPart>();
            HashMap<Uri, PduPart> toBeUpdated = new HashMap<Uri, PduPart>();

            int partsNum = body.getPartsNum();
            StringBuilder filter = new StringBuilder().append('(');
            for (int i = 0; i < partsNum; i++) {
                PduPart part = body.getPart(i);
                Uri partUri = part.getDataUri();
                if ((partUri == null) || !partUri.getAuthority().startsWith("mms")) {
                    toBeCreated.add(part);
                } else {
                    toBeUpdated.put(partUri, part);

                    // Don't use 'i > 0' to determine whether we should append
                    // 'AND' since 'i = 0' may be skipped in another branch.
                    if (filter.length() > 1) {
                        filter.append(" AND ");
                    }

                    filter.append("_id");
                    filter.append("!=");
                    DatabaseUtils.appendEscapedSQLString(filter, partUri.getLastPathSegment());
                }
            }
            filter.append(')');

            long msgId = ContentUris.parseId(uri);

            // Remove the parts which doesn't exist anymore.
            SqliteWrapper.delete(mContext, mContentResolver,
                    Uri.parse(Uri.parse("content://mms") + "/" + msgId + "/part"),
                    filter.length() > 2 ? filter.toString() : null, null);

            // Create new parts which didn't exist before.
            for (PduPart part : toBeCreated) {
                persistPart(part, msgId, preOpenedFiles);
            }

            // Update the modified parts.
            for (Entry<Uri, PduPart> e : toBeUpdated.entrySet()) {
                updatePart(e.getKey(), e.getValue(), preOpenedFiles);
            }
        } finally {
            synchronized(PDU_CACHE_INSTANCE) {
                PDU_CACHE_INSTANCE.setUpdating(uri, false);
                PDU_CACHE_INSTANCE.notifyAll();
            }
        }
    }

    /**
     * Persist a PDU object to specific location in the storage.
     *
     * @param pdu The PDU object to be stored.
     * @param uri Where to store the given PDU object.
     * @param createThreadId if true, this function may create a thread id for the recipients
     * @param groupMmsEnabled if true, all of the recipients addressed in the PDU will be used
     *  to create the associated thread. When false, only the sender will be used in finding or
     *  creating the appropriate thread or conversation.
     * @param preOpenedFiles if not null, a map of preopened InputStreams for the parts.
     * @return A Uri which can be used to access the stored PDU.
     */

    public Uri persist(GenericPdu pdu, Uri uri, boolean createThreadId, boolean groupMmsEnabled,
            HashMap<Uri, InputStream> preOpenedFiles)
            throws MmsException {
        if (uri == null) {
            throw new MmsException("Uri may not be null.");
        }
        long msgId = -1;
        try {
            msgId = ContentUris.parseId(uri);
        } catch (NumberFormatException e) {
            // the uri ends with "inbox" or something else like that
        }
        boolean existingUri = msgId != -1;

        if (!existingUri && MESSAGE_BOX_MAP.get(uri) == null) {
            throw new MmsException(
                    "Bad destination, must be one of "
                    + "content://mms/inbox, content://mms/sent, "
                    + "content://mms/drafts, content://mms/outbox, "
                    + "content://mms/temp.");
        }
        synchronized(PDU_CACHE_INSTANCE) {
            // If the cache item is getting updated, wait until it's done updating before
            // purging it.
            if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                if (LOCAL_LOGV) Log.v(TAG, "persist: " + uri + " blocked by isUpdating()");
                try {
                    PDU_CACHE_INSTANCE.wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "persist1: ", e);
                }
            }
        }
        PDU_CACHE_INSTANCE.purge(uri);

        PduHeaders header = pdu.getPduHeaders();
        PduBody body = null;
        ContentValues values = new ContentValues();
        Set<Entry<Integer, String>> set;

        set = ENCODED_STRING_COLUMN_NAME_MAP.entrySet();
        for (Entry<Integer, String> e : set) {
            int field = e.getKey();
            EncodedStringValue encodedString = header.getEncodedStringValue(field);
            if (encodedString != null) {
                String charsetColumn = CHARSET_COLUMN_NAME_MAP.get(field);
                values.put(e.getValue(), toIsoString(encodedString.getTextString()));
                values.put(charsetColumn, encodedString.getCharacterSet());
            }
        }

        set = TEXT_STRING_COLUMN_NAME_MAP.entrySet();
        for (Entry<Integer, String> e : set){
            byte[] text = header.getTextString(e.getKey());
            if (text != null) {
                values.put(e.getValue(), toIsoString(text));
            }
        }

        set = OCTET_COLUMN_NAME_MAP.entrySet();
        for (Entry<Integer, String> e : set){
            int b = header.getOctet(e.getKey());
            if (b != 0) {
                values.put(e.getValue(), b);
            }
        }

        set = LONG_COLUMN_NAME_MAP.entrySet();
        for (Entry<Integer, String> e : set){
            long l = header.getLongInteger(e.getKey());
            if (l != -1L) {
                values.put(e.getValue(), l);
            }
        }

        HashMap<Integer, EncodedStringValue[]> addressMap =
                new HashMap<Integer, EncodedStringValue[]>(ADDRESS_FIELDS.length);
        // Save address information.
        for (int addrType : ADDRESS_FIELDS) {
            EncodedStringValue[] array = null;
            if (addrType == PduHeaders.FROM) {
                EncodedStringValue v = header.getEncodedStringValue(addrType);
                if (v != null) {
                    array = new EncodedStringValue[1];
                    array[0] = v;
                }
            } else {
                array = header.getEncodedStringValues(addrType);
            }
            addressMap.put(addrType, array);
        }

        HashSet<String> recipients = new HashSet<String>();
        int msgType = pdu.getMessageType();
        // Here we only allocate thread ID for M-Notification.ind,
        // M-Retrieve.conf and M-Send.req.
        // Some of other PDU types may be allocated a thread ID outside
        // this scope.
        if ((msgType == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND)
                || (msgType == PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF)
                || (msgType == PduHeaders.MESSAGE_TYPE_SEND_REQ)) {
            switch (msgType) {
                case PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND:
                case PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF:
                    loadRecipients(PduHeaders.FROM, recipients, addressMap, false);

                    // For received messages when group MMS is enabled, we want to associate this
                    // message with the thread composed of all the recipients -- all but our own
                    // number, that is. This includes the person who sent the
                    // message or the FROM field (above) in addition to the other people the message
                    // was addressed to or the TO & CC fields. Our own number is in that TO field
                    // and we have to ignore it in loadRecipients.
                    if (groupMmsEnabled) {
                        loadRecipients(PduHeaders.TO, recipients, addressMap, true);
                        loadRecipients(PduHeaders.CC, recipients, addressMap, true);
                    }
                    break;
                case PduHeaders.MESSAGE_TYPE_SEND_REQ:
                    loadRecipients(PduHeaders.TO, recipients, addressMap, false);
                    break;
            }
            long threadId = DUMMY_THREAD_ID;
            if (createThreadId && !recipients.isEmpty()) {
                // Given all the recipients associated with this message, find (or create) the
                // correct thread.
                threadId = Utils.getOrCreateThreadId(mContext, recipients);
            }
            values.put("thread_id", threadId);
        }

        // Save parts first to avoid inconsistent message is loaded
        // while saving the parts.
        long dummyId = System.currentTimeMillis(); // Dummy ID of the msg.

        // Figure out if this PDU is a text-only message
        boolean textOnly = true;

        // Get body if the PDU is a RetrieveConf or SendReq.
        if (pdu instanceof MultimediaMessagePdu) {
            body = ((MultimediaMessagePdu) pdu).getBody();
            // Start saving parts if necessary.
            if (body != null) {
                int partsNum = body.getPartsNum();
                if (partsNum > 2) {
                    // For a text-only message there will be two parts: 1-the SMIL, 2-the text.
                    // Down a few lines below we're checking to make sure we've only got SMIL or
                    // text. We also have to check then we don't have more than two parts.
                    // Otherwise, a slideshow with two text slides would be marked as textOnly.
                    textOnly = false;
                }
                for (int i = 0; i < partsNum; i++) {
                    PduPart part = body.getPart(i);
                    persistPart(part, dummyId, preOpenedFiles);

                    // If we've got anything besides text/plain or SMIL part, then we've got
                    // an mms message with some other type of attachment.
                    String contentType = getPartContentType(part);
                    if (contentType != null && !ContentType.APP_SMIL.equals(contentType)
                            && !ContentType.TEXT_PLAIN.equals(contentType)) {
                        textOnly = false;
                    }
                }
            }
        }
        // Record whether this mms message is a simple plain text or not. This is a hint for the
        // UI.
        //values.put(Mms.TEXT_ONLY, textOnly ? 1 : 0);

        Uri res = null;
        if (existingUri) {
            res = uri;
            SqliteWrapper.update(mContext, mContentResolver, res, values, null, null);
        } else {
            res = SqliteWrapper.insert(mContext, mContentResolver, uri, values);
            if (res == null) {
                throw new MmsException("persist() failed: return null.");
            }
            // Get the real ID of the PDU and update all parts which were
            // saved with the dummy ID.
            msgId = ContentUris.parseId(res);
        }

        values = new ContentValues(1);
        values.put("mid", msgId);
        SqliteWrapper.update(mContext, mContentResolver,
                             Uri.parse("content://mms/" + dummyId + "/part"),
                             values, null, null);
        // We should return the longest URI of the persisted PDU, for
        // example, if input URI is "content://mms/inbox" and the _ID of
        // persisted PDU is '8', we should return "content://mms/inbox/8"
        // instead of "content://mms/8".
        // FIXME: Should the MmsProvider be responsible for this???
        if (!existingUri) {
            res = Uri.parse(uri + "/" + msgId);
        }

        // Save address information.
        for (int addrType : ADDRESS_FIELDS) {
            EncodedStringValue[] array = addressMap.get(addrType);
            if (array != null) {
                persistAddress(msgId, addrType, array);
            }
        }

        return res;
    }

    /**
     * For a given address type, extract the recipients from the headers.
     *
     * @param addressType can be PduHeaders.FROM or PduHeaders.TO
     * @param recipients a HashSet that is loaded with the recipients from the FROM or TO headers
     * @param addressMap a HashMap of the addresses from the ADDRESS_FIELDS header
     * @param excludeMyNumber if true, the number of this phone will be excluded from recipients
     */
    private void loadRecipients(int addressType, HashSet<String> recipients,
            HashMap<Integer, EncodedStringValue[]> addressMap, boolean excludeMyNumber) {
        EncodedStringValue[] array = addressMap.get(addressType);
        if (array == null) {
            return;
        }
        // If the TO recipients is only a single address, then we can skip loadRecipients when
        // we're excluding our own number because we know that address is our own.
        if (excludeMyNumber && array.length == 1) {
            return;
        }
        String myNumber = excludeMyNumber ? mTelephonyManager.getLine1Number() : null;
        for (EncodedStringValue v : array) {
            if (v != null) {
                String number = v.getString();
                if ((myNumber == null || !PhoneNumberUtils.compare(number, myNumber)) &&
                        !recipients.contains(number)) {
                    // Only add numbers which aren't my own number.
                    recipients.add(number);
                }
            }
        }
    }

    /**
     * Move a PDU object from one location to another.
     *
     * @param from Specify the PDU object to be moved.
     * @param to The destination location, should be one of the following:
     *        "content://mms/inbox", "content://mms/sent",
     *        "content://mms/drafts", "content://mms/outbox",
     *        "content://mms/trash".
     * @return New Uri of the moved PDU.
     * @throws MmsException Error occurred while moving the message.
     */
    public Uri move(Uri from, Uri to) throws MmsException {
        // Check whether the 'msgId' has been assigned a valid value.
        long msgId = ContentUris.parseId(from);
        if (msgId == -1L) {
            throw new MmsException("Error! ID of the message: -1.");
        }

        // Get corresponding int value of destination box.
        Integer msgBox = MESSAGE_BOX_MAP.get(to);
        if (msgBox == null) {
            throw new MmsException(
                    "Bad destination, must be one of "
                    + "content://mms/inbox, content://mms/sent, "
                    + "content://mms/drafts, content://mms/outbox, "
                    + "content://mms/temp.");
        }

        ContentValues values = new ContentValues(1);
        values.put("msg_box", msgBox);
        SqliteWrapper.update(mContext, mContentResolver, from, values, null, null);
        return ContentUris.withAppendedId(to, msgId);
    }

    /**
     * Wrap a byte[] into a String.
     */
    public static String toIsoString(byte[] bytes) {
        try {
            return new String(bytes, CharacterSets.MIMENAME_ISO_8859_1);
        } catch (UnsupportedEncodingException e) {
            // Impossible to reach here!
            Log.e(TAG, "ISO_8859_1 must be supported!", e);
            return "";
        }
    }

    /**
     * Unpack a given String into a byte[].
     */
    public static byte[] getBytes(String data) {
        try {
            return data.getBytes(CharacterSets.MIMENAME_ISO_8859_1);
        } catch (UnsupportedEncodingException e) {
            // Impossible to reach here!
            Log.e(TAG, "ISO_8859_1 must be supported!", e);
            return new byte[0];
        }
    }

    /**
     * Remove all objects in the temporary path.
     */
    public void release() {
        Uri uri = Uri.parse(TEMPORARY_DRM_OBJECT_URI);
        SqliteWrapper.delete(mContext, mContentResolver, uri, null, null);
    }

    /**
     * Find all messages to be sent or downloaded before certain time.
     */
    public Cursor getPendingMessages(long dueTime) {
        Uri.Builder uriBuilder = Telephony.MmsSms.PendingMessages.CONTENT_URI.buildUpon();
        uriBuilder.appendQueryParameter("protocol", "mms");

        String selection = "err_type" + " < ?"
                + " AND " + "due_time" + " <= ?";

        String[] selectionArgs = new String[] {
                String.valueOf(10),
                String.valueOf(dueTime)
        };

        return SqliteWrapper.query(mContext, mContentResolver,
                uriBuilder.build(), null, selection, selectionArgs,
                "due_time");
    }
}
