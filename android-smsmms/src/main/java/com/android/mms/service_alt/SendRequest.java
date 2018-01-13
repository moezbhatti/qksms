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

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.text.TextUtils;
import com.klinker.android.logger.Log;

import com.android.mms.service_alt.exception.MmsHttpException;

import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.GenericPdu;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduParser;
import com.google.android.mms.pdu_alt.PduPersister;
import com.google.android.mms.pdu_alt.SendConf;
import com.google.android.mms.pdu_alt.SendReq;
import com.google.android.mms.util_alt.SqliteWrapper;

/**
 * Request to send an MMS
 */
public class SendRequest extends MmsRequest {
    private static final String TAG = "SendRequest";

    private final Uri mPduUri;
    private byte[] mPduData;
    private final String mLocationUrl;
    private final PendingIntent mSentIntent;

    public SendRequest(RequestManager manager, int subId, Uri contentUri, String locationUrl,
            PendingIntent sentIntent, String creator, Bundle configOverrides) {
        super(manager, subId, creator, configOverrides);
        mPduUri = contentUri;
        mPduData = null;
        mLocationUrl = locationUrl;
        mSentIntent = sentIntent;
    }

    @Override
    protected byte[] doHttp(Context context, MmsNetworkManager netMgr, ApnSettings apn)
            throws MmsHttpException {
        final MmsHttpClient mmsHttpClient = netMgr.getOrCreateHttpClient();
        if (mmsHttpClient == null) {
            Log.e(TAG, "MMS network is not ready!");
            throw new MmsHttpException(0/*statusCode*/, "MMS network is not ready");
        }
        return mmsHttpClient.execute(
                mLocationUrl != null ? mLocationUrl : apn.getMmscUrl(),
                mPduData,
                MmsHttpClient.METHOD_POST,
                apn.isProxySet(),
                apn.getProxyAddress(),
                apn.getProxyPort(),
                mMmsConfig);
    }

    @Override
    protected PendingIntent getPendingIntent() {
        return mSentIntent;
    }

    @Override
    protected int getQueueType() {
        return 0;
    }

    @Override
    protected Uri persistIfRequired(Context context, int result, byte[] response) {
        Log.d(TAG, "SendRequest.persistIfRequired");
        if (mPduData == null) {
            Log.e(TAG, "SendRequest.persistIfRequired: empty PDU");
            return null;
        }
        final long identity = Binder.clearCallingIdentity();
        try {
            final boolean supportContentDisposition = mMmsConfig.getSupportMmsContentDisposition();
            // Persist the request PDU first
            GenericPdu pdu = (new PduParser(mPduData, supportContentDisposition)).parse();
            if (pdu == null) {
                Log.e(TAG, "SendRequest.persistIfRequired: can't parse input PDU");
                return null;
            }
            if (!(pdu instanceof SendReq)) {
                Log.d(TAG, "SendRequest.persistIfRequired: not SendReq");
                return null;
            }
//            final PduPersister persister = PduPersister.getPduPersister(context);
//            final Uri messageUri = persister.persist(
//                    pdu,
//                    Telephony.Mms.Sent.CONTENT_URI,
//                    true/*createThreadId*/,
//                    true/*groupMmsEnabled*/,
//                    null/*preOpenedFiles*/);
//            if (messageUri == null) {
//                Log.e(TAG, "SendRequest.persistIfRequired: can not persist message");
//                return null;
//            }
            // Update the additional columns based on the send result
            final ContentValues values = new ContentValues();
            SendConf sendConf = null;
            if (response != null && response.length > 0) {
                pdu = (new PduParser(response, supportContentDisposition)).parse();
                if (pdu != null && pdu instanceof SendConf) {
                    sendConf = (SendConf) pdu;
                }
            }
            if (result != Activity.RESULT_OK
                    || sendConf == null
                    || sendConf.getResponseStatus() != PduHeaders.RESPONSE_STATUS_OK) {
                // Since we can't persist a message directly into FAILED box,
                // we have to update the column after we persist it into SENT box.
                // The gap between the state change is tiny so I would not expect
                // it to cause any serious problem
                // TODO: we should add a "failed" URI for this in MmsProvider?
                values.put(Telephony.Mms.MESSAGE_BOX, Telephony.Mms.MESSAGE_BOX_FAILED);
            } else {
                values.put(Telephony.Mms.MESSAGE_BOX, Telephony.Mms.MESSAGE_BOX_SENT);
            }
            if (sendConf != null) {
                values.put(Telephony.Mms.RESPONSE_STATUS, sendConf.getResponseStatus());
                Log.v(TAG, "response status: " + sendConf.getResponseStatus());
                values.put(Telephony.Mms.MESSAGE_ID,
                        PduPersister.toIsoString(sendConf.getMessageId()));
            }
            values.put(Telephony.Mms.DATE, System.currentTimeMillis() / 1000L);
            values.put(Telephony.Mms.READ, 1);
            values.put(Telephony.Mms.SEEN, 1);
            if (!TextUtils.isEmpty(mCreator)) {
                values.put(Telephony.Mms.CREATOR, mCreator);
            }
            if (SubscriptionIdChecker.getInstance(context).canUseSubscriptionId()) {
                values.put(Telephony.Mms.SUBSCRIPTION_ID, mSubId);
            }
            if (SqliteWrapper.update(context, context.getContentResolver(), mPduUri, values,
                    null/*where*/, null/*selectionArg*/) != 1) {
                Log.e(TAG, "SendRequest.persistIfRequired: failed to update message");
            }
            return mPduUri;
//        } catch (MmsException e) {
//            Log.e(TAG, "SendRequest.persistIfRequired: can not persist message", e);
        } catch (RuntimeException e) {
            Log.e(TAG, "SendRequest.persistIfRequired: unexpected parsing failure", e);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
        return null;
    }

    /**
     * Read the pdu from the file descriptor and cache pdu bytes in request
     * @return true if pdu read successfully
     */
    private boolean readPduFromContentUri() {
        if (mPduData != null) {
            return true;
        }
        final int bytesTobeRead = mMmsConfig.getMaxMessageSize();
        mPduData = mRequestManager.readPduFromContentUri(mPduUri, bytesTobeRead);
        return (mPduData != null);
    }

    /**
     * Transfer the received response to the caller (for send requests the pdu is small and can
     *  just include bytes as extra in the "returned" intent).
     *
     * @param fillIn the intent that will be returned to the caller
     * @param response the pdu to transfer
     */
    @Override
    protected boolean transferResponse(Intent fillIn, byte[] response) {
        // SendConf pdus are always small and can be included in the intent
        if (response != null) {
            fillIn.putExtra(SmsManager.EXTRA_MMS_DATA, response);
        }
        return true;
    }

    /**
     * Read the data from the file descriptor if not yet done
     * @return whether data successfully read
     */
    @Override
    protected boolean prepareForHttpRequest() {
        return readPduFromContentUri();
    }

    /**
     * Try sending via the carrier app
     *
     * @param context the context
     * @param carrierMessagingServicePackage the carrier messaging service sending the MMS
     */
    public void trySendingByCarrierApp(Context context, String carrierMessagingServicePackage) {
//        final CarrierSendManager carrierSendManger = new CarrierSendManager();
//        final CarrierSendCompleteCallback sendCallback = new CarrierSendCompleteCallback(
//                context, carrierSendManger);
//        carrierSendManger.sendMms(context, carrierMessagingServicePackage, sendCallback);
    }

    @Override
    protected void revokeUriPermission(Context context) {
        try {
            context.revokeUriPermission(mPduUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (NullPointerException e) {
            Log.e(TAG, "error revoking permissions", e);
        }
    }
}
