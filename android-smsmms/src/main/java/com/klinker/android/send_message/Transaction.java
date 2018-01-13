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

import android.app.Activity;
import android.app.PendingIntent;
import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;

import com.android.mms.MmsConfig;
import com.android.mms.service_alt.MmsNetworkManager;
import com.android.mms.service_alt.MmsRequestManager;
import com.android.mms.service_alt.SendRequest;
import com.klinker.android.logger.Log;
import android.widget.Toast;
import com.android.mms.dom.smil.parser.SmilXmlSerializer;
import com.android.mms.transaction.MmsMessageSender;
import com.android.mms.transaction.ProgressCallbackEntity;
import com.android.mms.util.DownloadManager;
import com.android.mms.util.RateController;
import com.google.android.mms.*;
import com.google.android.mms.pdu_alt.*;
import com.google.android.mms.smil.SmilHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * Class to process transaction requests for sending
 *
 * @author Jake Klinker
 */
public class Transaction {

    private static final String TAG = "Transaction";
    public static Settings settings;
    private Context context;

    private Intent explicitSentSmsReceiver;
    private Intent explicitSentMmsReceiver;
    private Intent explicitDeliveredSmsReceiver;

    private boolean saveMessage = true;

    public String SMS_SENT = ".SMS_SENT";
    public String SMS_DELIVERED = ".SMS_DELIVERED";

    public static String NOTIFY_SMS_FAILURE = ".NOTIFY_SMS_FAILURE";
    public static final String MMS_ERROR = "com.klinker.android.send_message.MMS_ERROR";
    public static final String REFRESH = "com.klinker.android.send_message.REFRESH";
    public static final String MMS_PROGRESS = "com.klinker.android.send_message.MMS_PROGRESS";
    public static final String NOTIFY_OF_DELIVERY = "com.klinker.android.send_message.NOTIFY_DELIVERY";
    public static final String NOTIFY_OF_MMS = "com.klinker.android.messaging.NEW_MMS_DOWNLOADED";

    public static final long NO_THREAD_ID = 0;

    /**
     * Sets context and initializes settings to default values
     *
     * @param context is the context of the activity or service
     */
    public Transaction(Context context) {
        this(context, new Settings());
    }

    /**
     * Sets context and settings
     *
     * @param context  is the context of the activity or service
     * @param settings is the settings object to process send requests through
     */
    public Transaction(Context context, Settings settings) {
        this.settings = settings;
        this.context = context;

        SMS_SENT = context.getPackageName() + SMS_SENT;
        SMS_DELIVERED = context.getPackageName() + SMS_DELIVERED;

        if (NOTIFY_SMS_FAILURE.equals(".NOTIFY_SMS_FAILURE")) {
            NOTIFY_SMS_FAILURE = context.getPackageName() + NOTIFY_SMS_FAILURE;
        }
    }

    /**
     * Called to send a new message depending on settings and provided Message object
     * If you want to send message as mms, call this from the UI thread
     *
     * @param message  is the message that you want to send
     * @param threadId is the thread id of who to send the message to (can also be set to Transaction.NO_THREAD_ID)
     */
    public void sendNewMessage(Message message, long threadId) {
        this.saveMessage = message.getSave();

        // if message:
        //      1) Has images attached
        // or
        //      1) is enabled to send long messages as mms
        //      2) number of pages for that sms exceeds value stored in settings for when to send the mms by
        //      3) prefer voice is disabled
        // or
        //      1) more than one address is attached
        //      2) group messaging is enabled
        //
        // then, send as MMS, else send as Voice or SMS
        if (checkMMS(message)) {
            try { Looper.prepare(); } catch (Exception e) { }
            RateController.init(context);
            DownloadManager.init(context);
            sendMmsMessage(message.getText(), message.getAddresses(), message.getImages(), message.getImageNames(), message.getParts(), message.getSubject());
        } else {
            sendSmsMessage(message.getText(), message.getAddresses(), threadId, message.getDelay());
        }

    }

    /**
     * Optional: define a {@link BroadcastReceiver} that will get started when Android notifies us that the SMS has
     * been marked as "sent". If you do not define a receiver here, it will look for the .SMS_SENT receiver
     * that was defined in the AndroidManifest, as discussed in the README.md.
     *
     * @param intent the receiver that you want to start when the message gets marked as sent.
     */
    public Transaction setExplicitBroadcastForSentSms(Intent intent) {
        explicitSentSmsReceiver = intent;
        return this;
    }

    /**
     * Optional: define a {@link BroadcastReceiver} that will get started when Android notifies us that the MMS has
     * been marked as "sent". If you do not define a receiver here, it will look for the .MMS_SENT receiver
     * that was defined in the AndroidManifest, as discussed in the README.md.
     *
     * @param intent the receiver that you want to start when the message gets marked as sent.
     */
    public Transaction setExplicitBroadcastForSentMms(Intent intent) {
        explicitSentMmsReceiver = intent;
        return this;
    }

    /**
     * Optional: define a {@link BroadcastReceiver} that will get started when Android notifies us that the SMS has
     * been marked as "delivered". If you do not define a receiver here, it will look for the .SMS_DELIVERED
     * receiver that was defined in the AndroidManifest, as discussed in the README.md.
     * <p/>
     * Providing a receiver here does not guarantee that it will ever get started. If the {@link Settings}
     * object does not have delivery reports turned on, this receiver will never get called.
     *
     * @param intent the receiver that you want to start when the message gets marked as sent.
     */
    public Transaction setExplicitBroadcastForDeliveredSms(Intent intent) {
        explicitDeliveredSmsReceiver = intent;
        return this;
    }

    private void sendSmsMessage(String text, String[] addresses, long threadId, int delay) {
        Log.v("send_transaction", "message text: " + text);
        Uri messageUri = null;
        int messageId = 0;
        if (saveMessage) {
            Log.v("send_transaction", "saving message");
            // add signature to original text to be saved in database (does not strip unicode for saving though)
            if (!settings.getSignature().equals("")) {
                text += "\n" + settings.getSignature();
            }

            // save the message for each of the addresses
            for (int i = 0; i < addresses.length; i++) {
                Calendar cal = Calendar.getInstance();
                ContentValues values = new ContentValues();
                values.put("address", addresses[i]);
                values.put("body", settings.getStripUnicode() ? StripAccents.stripAccents(text) : text);
                values.put("date", cal.getTimeInMillis() + "");
                values.put("read", 1);
                values.put("type", 4);

                // attempt to create correct thread id if one is not supplied
                if (threadId == NO_THREAD_ID || addresses.length > 1) {
                    threadId = Utils.getOrCreateThreadId(context, addresses[i]);
                }

                Log.v("send_transaction", "saving message with thread id: " + threadId);

                values.put("thread_id", threadId);
                messageUri = context.getContentResolver().insert(Uri.parse("content://sms/"), values);

                Log.v("send_transaction", "inserted to uri: " + messageUri);

                Cursor query = context.getContentResolver().query(messageUri, new String[] {"_id"}, null, null, null);
                if (query != null && query.moveToFirst()) {
                    messageId = query.getInt(0);
                    query.close();
                }

                Log.v("send_transaction", "message id: " + messageId);

                // set up sent and delivered pending intents to be used with message request
                Intent sentIntent;
                if (explicitSentSmsReceiver == null) {
                    sentIntent = new Intent(SMS_SENT);
                    BroadcastUtils.addClassName(context, sentIntent, SMS_SENT);
                } else {
                    sentIntent = explicitSentSmsReceiver;
                }

                sentIntent.putExtra("message_uri", messageUri == null ? "" : messageUri.toString());
                PendingIntent sentPI = PendingIntent.getBroadcast(
                        context, messageId, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Intent deliveredIntent;
                if (explicitDeliveredSmsReceiver == null) {
                    deliveredIntent = new Intent(SMS_DELIVERED);
                    BroadcastUtils.addClassName(context, deliveredIntent, SMS_DELIVERED);
                } else {
                    deliveredIntent = explicitDeliveredSmsReceiver;
                }

                deliveredIntent.putExtra("message_uri", messageUri == null ? "" : messageUri.toString());
                PendingIntent deliveredPI = PendingIntent.getBroadcast(
                        context, messageId, deliveredIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                ArrayList<PendingIntent> sPI = new ArrayList<PendingIntent>();
                ArrayList<PendingIntent> dPI = new ArrayList<PendingIntent>();

                String body = text;

                // edit the body of the text if unicode needs to be stripped
                if (settings.getStripUnicode()) {
                    body = StripAccents.stripAccents(body);
                }

                if (!settings.getPreText().equals("")) {
                    body = settings.getPreText() + " " + body;
                }

                SmsManager smsManager = SmsManagerFactory.createSmsManager(settings);
                Log.v("send_transaction", "found sms manager");

                if (settings.getSplit()) {
                    Log.v("send_transaction", "splitting message");
                    // figure out the length of supported message
                    int[] splitData = SmsMessage.calculateLength(body, false);

                    // we take the current length + the remaining length to get the total number of characters
                    // that message set can support, and then divide by the number of message that will require
                    // to get the length supported by a single message
                    int length = (body.length() + splitData[2]) / splitData[0];
                    Log.v("send_transaction", "length: " + length);

                    boolean counter = false;
                    if (settings.getSplitCounter() && body.length() > length) {
                        counter = true;
                        length -= 6;
                    }

                    // get the split messages
                    String[] textToSend = splitByLength(body, length, counter);

                    // send each message part to each recipient attached to message
                    for (int j = 0; j < textToSend.length; j++) {
                        ArrayList<String> parts = smsManager.divideMessage(textToSend[j]);

                        for (int k = 0; k < parts.size(); k++) {
                            sPI.add(saveMessage ? sentPI : null);
                            dPI.add(settings.getDeliveryReports() && saveMessage ? deliveredPI : null);
                        }

                        Log.v("send_transaction", "sending split message");
                        sendDelayedSms(smsManager, addresses[i], parts, sPI, dPI, delay, messageUri);
                    }
                } else {
                    Log.v("send_transaction", "sending without splitting");
                    // send the message normally without forcing anything to be split
                    ArrayList<String> parts = smsManager.divideMessage(body);

                    for (int j = 0; j < parts.size(); j++) {
                        sPI.add(saveMessage ? sentPI : null);
                        dPI.add(settings.getDeliveryReports() && saveMessage ? deliveredPI : null);
                    }

                    if (Utils.isDefaultSmsApp(context)) {
                        try {
                            Log.v("send_transaction", "sent message");
                            sendDelayedSms(smsManager, addresses[i], parts, sPI, dPI, delay, messageUri);
                        } catch (Exception e) {
                            // whoops...
                            Log.v("send_transaction", "error sending message");
                            Log.e(TAG, "exception thrown", e);

                            try {
                                ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "Message could not be sent", Toast.LENGTH_LONG).show();
                                    }
                                });
                            } catch (Exception f) { }
                        }
                    } else {
                        // not default app, so just fire it off right away for the hell of it
                        smsManager.sendMultipartTextMessage(addresses[i], null, parts, sPI, dPI);
                    }
                }
            }
        }
    }

    private void sendDelayedSms(final SmsManager smsManager, final String address,
                                final ArrayList<String> parts, final ArrayList<PendingIntent> sPI,
                                final ArrayList<PendingIntent> dPI, final int delay, final Uri messageUri) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                } catch (Exception e) { }

                if (checkIfMessageExistsAfterDelay(messageUri)) {
                    Log.v("send_transaction", "message sent after delay");
                    try {
                        smsManager.sendMultipartTextMessage(address, null, parts, sPI, dPI);
                    } catch (Exception e) {
                        Log.e(TAG, "exception thrown", e);
                    }
                } else {
                    Log.v("send_transaction", "message not sent after delay, no longer exists");
                }
            }
        }).start();
    }

    private boolean checkIfMessageExistsAfterDelay(Uri messageUti) {
        Cursor query = context.getContentResolver().query(messageUti, new String[] {"_id"}, null, null, null);
        if (query != null && query.moveToFirst()) {
            query.close();
            return true;
        } else {
            return false;
        }
    }

    private void sendMmsMessage(String text, String[] addresses, Bitmap[] image, String[] imageNames, List<Message.Part> parts, String subject) {
        // merge the string[] of addresses into a single string so they can be inserted into the database easier
        String address = "";

        for (int i = 0; i < addresses.length; i++) {
            address += addresses[i] + " ";
        }

        address = address.trim();

        // create the parts to send
        ArrayList<MMSPart> data = new ArrayList<MMSPart>();

        for (int i = 0; i < image.length; i++) {
            // turn bitmap into byte array to be stored
            byte[] imageBytes = Message.bitmapToByteArray(image[i]);

            MMSPart part = new MMSPart();
            part.MimeType = "image/jpeg";
            part.Name = (imageNames != null) ? imageNames[i] : ("image_" + System.currentTimeMillis());
            part.Data = imageBytes;
            data.add(part);
        }

        // add any extra media according to their mimeType set in the message
        //      eg. videos, audio, contact cards, location maybe?
        if (parts != null) {
            for (Message.Part p : parts) {
                MMSPart part = new MMSPart();
                if (p.getName() != null) {
                    part.Name = p.getName();
                } else {
                    part.Name = p.getContentType().split("/")[0];
                }
                part.MimeType = p.getContentType();
                part.Data = p.getMedia();
                data.add(part);
            }
        }

        if (text != null && !text.equals("")) {
            // add text to the end of the part and send
            MMSPart part = new MMSPart();
            part.Name = "text";
            part.MimeType = "text/plain";
            part.Data = text.getBytes();
            data.add(part);
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            MessageInfo info = null;

            try {
                info = getBytes(context, saveMessage, address.split(" "),
                        data.toArray(new MMSPart[data.size()]), subject);
                MmsMessageSender sender = new MmsMessageSender(context, info.location, info.bytes.length);
                sender.sendMessage(info.token);

                IntentFilter filter = new IntentFilter();
                filter.addAction(ProgressCallbackEntity.PROGRESS_STATUS_ACTION);
                BroadcastReceiver receiver = new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context context, Intent intent) {
                        int progress = intent.getIntExtra("progress", -3);
                        Log.v("sending_mms_library", "progress: " + progress);

                        // send progress broadcast to update ui if desired...
                        Intent progressIntent = new Intent(MMS_PROGRESS);
                        progressIntent.putExtra("progress", progress);
                        BroadcastUtils.sendExplicitBroadcast(context, progressIntent, MMS_PROGRESS);

                        if (progress == ProgressCallbackEntity.PROGRESS_COMPLETE) {
                            BroadcastUtils.sendExplicitBroadcast(context, new Intent(), REFRESH);

                            try {
                                context.unregisterReceiver(this);
                            } catch (Exception e) {
                                // TODO fix me
                                // receiver is not registered force close error... hmm.
                            }
                        } else if (progress == ProgressCallbackEntity.PROGRESS_ABORT) {
                            // This seems to get called only after the progress has reached 100 and
                            // then something else goes wrong, so here we will try and send again
                            // and see if it works
                            Log.v("sending_mms_library", "sending aborted for some reason...");
                        }
                    }

                };

                context.registerReceiver(receiver, filter);
            } catch (Throwable e) {
                Log.e(TAG, "exception thrown", e);
            }
        } else {
            Log.v(TAG, "using lollipop method for sending sms");

            if (settings.getUseSystemSending()) {
                Log.v(TAG, "using system method for sending");
                sendMmsThroughSystem(context, subject, data, addresses, explicitSentMmsReceiver);
            } else {
                try {
                    MessageInfo info = getBytes(context, saveMessage, address.split(" "),
                            data.toArray(new MMSPart[data.size()]), subject);
                    MmsRequestManager requestManager = new MmsRequestManager(context, info.bytes);
                    SendRequest request = new SendRequest(requestManager, Utils.getDefaultSubscriptionId(),
                            info.location, null, null, null, null);
                    MmsNetworkManager manager = new MmsNetworkManager(context, Utils.getDefaultSubscriptionId());
                    request.execute(context, manager);
                } catch (Exception e) {
                    Log.e(TAG, "error sending mms", e);
                }
            }
        }
    }

    public static MessageInfo getBytes(Context context, boolean saveMessage, String[] recipients,
                                       MMSPart[] parts, String subject)
                throws MmsException {
        final SendReq sendRequest = new SendReq();

        // create send request addresses
        for (int i = 0; i < recipients.length; i++) {
            final EncodedStringValue[] phoneNumbers = EncodedStringValue.extract(recipients[i]);

            if (phoneNumbers != null && phoneNumbers.length > 0) {
                sendRequest.addTo(phoneNumbers[0]);
            }
        }

        if (subject != null) {
            sendRequest.setSubject(new EncodedStringValue(subject));
        }

        sendRequest.setDate(Calendar.getInstance().getTimeInMillis() / 1000L);

        try {
            sendRequest.setFrom(new EncodedStringValue(Utils.getMyPhoneNumber(context)));
        } catch (Exception e) {
            Log.e(TAG, "error getting from address", e);
        }

        final PduBody pduBody = new PduBody();

        // assign parts to the pdu body which contains sending data
        long size = 0;
        if (parts != null) {
            for (int i = 0; i < parts.length; i++) {
                MMSPart part = parts[i];
                if (part != null) {
                    try {
                        PduPart partPdu = new PduPart();
                        partPdu.setName(part.Name.getBytes());
                        partPdu.setContentType(part.MimeType.getBytes());

                        if (part.MimeType.startsWith("text")) {
                            partPdu.setCharset(CharacterSets.UTF_8);
                        }
                        // Set Content-Location.
                        partPdu.setContentLocation(part.Name.getBytes());
                        int index = part.Name.lastIndexOf(".");
                        String contentId = (index == -1) ? part.Name
                                : part.Name.substring(0, index);
                        partPdu.setContentId(contentId.getBytes());
                        partPdu.setData(part.Data);

                        pduBody.addPart(partPdu);
                        size += ((2 * part.Name.getBytes().length) + part.MimeType.getBytes().length + part.Data.length + contentId.getBytes().length);
                    } catch (Exception e) {
                    }
                }
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SmilXmlSerializer.serialize(SmilHelper.createSmilDocument(pduBody), out);
        PduPart smilPart = new PduPart();
        smilPart.setContentId("smil".getBytes());
        smilPart.setContentLocation("smil.xml".getBytes());
        smilPart.setContentType(ContentType.APP_SMIL.getBytes());
        smilPart.setData(out.toByteArray());
        pduBody.addPart(0, smilPart);

        sendRequest.setBody(pduBody);

        Log.v(TAG, "setting message size to " + size + " bytes");
        sendRequest.setMessageSize(size);

        // add everything else that could be set
        sendRequest.setPriority(PduHeaders.PRIORITY_NORMAL);
        sendRequest.setDeliveryReport(PduHeaders.VALUE_NO);
        sendRequest.setExpiry(1000 * 60 * 60 * 24 * 7);
        sendRequest.setMessageClass(PduHeaders.MESSAGE_CLASS_PERSONAL_STR.getBytes());
        sendRequest.setReadReport(PduHeaders.VALUE_NO);

        // create byte array which will actually be sent
        final PduComposer composer = new PduComposer(context, sendRequest);
        final byte[] bytesToSend;

        try {
            bytesToSend = composer.make();
        } catch (OutOfMemoryError e) {
            throw new MmsException("Out of memory!");
        }

        MessageInfo info = new MessageInfo();
        info.bytes = bytesToSend;

        if (saveMessage) {
            try {
                PduPersister persister = PduPersister.getPduPersister(context);
                info.location = persister.persist(sendRequest, Uri.parse("content://mms/outbox"), true, settings.getGroup(), null);
            } catch (Exception e) {
                Log.v("sending_mms_library", "error saving mms message");
                Log.e(TAG, "exception thrown", e);

                // use the old way if something goes wrong with the persister
                insert(context, recipients, parts, subject);
            }
        }

        try {
            Cursor query = context.getContentResolver().query(info.location, new String[] {"thread_id"}, null, null, null);
            if (query != null && query.moveToFirst()) {
                info.token = query.getLong(query.getColumnIndex("thread_id"));
                query.close();
            } else {
                // just default sending token for what I had before
                info.token = 4444L;
            }
        } catch (Exception e) {
            Log.e(TAG, "exception thrown", e);
            info.token = 4444L;
        }

        return info;
    }

    public static final long DEFAULT_EXPIRY_TIME = 7 * 24 * 60 * 60;
    public static final int DEFAULT_PRIORITY = PduHeaders.PRIORITY_NORMAL;

    private static void sendMmsThroughSystem(Context context, String subject, List<MMSPart> parts,
                                             String[] addresses, Intent explicitSentMmsReceiver) {
        try {
            final String fileName = "send." + String.valueOf(Math.abs(new Random().nextLong())) + ".dat";
            File mSendFile = new File(context.getCacheDir(), fileName);

            SendReq sendReq = buildPdu(context, addresses, subject, parts);
            PduPersister persister = PduPersister.getPduPersister(context);
            Uri messageUri = persister.persist(sendReq, Uri.parse("content://mms/outbox"),
                    true, settings.getGroup(), null);

            Intent intent;
            if (explicitSentMmsReceiver == null) {
                intent = new Intent(MmsSentReceiver.MMS_SENT);
                BroadcastUtils.addClassName(context, intent, MmsSentReceiver.MMS_SENT);
            } else {
                intent = explicitSentMmsReceiver;
            }

            intent.putExtra(MmsSentReceiver.EXTRA_CONTENT_URI, messageUri.toString());
            intent.putExtra(MmsSentReceiver.EXTRA_FILE_PATH, mSendFile.getPath());
            final PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Uri writerUri = (new Uri.Builder())
                    .authority(context.getPackageName() + ".MmsFileProvider")
                    .path(fileName)
                    .scheme(ContentResolver.SCHEME_CONTENT)
                    .build();
            FileOutputStream writer = null;
            Uri contentUri = null;
            try {
                writer = new FileOutputStream(mSendFile);
                writer.write(new PduComposer(context, sendReq).make());
                contentUri = writerUri;
            } catch (final IOException e) {
                Log.e(TAG, "Error writing send file", e);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                    }
                }
            }

            Bundle configOverrides = new Bundle();
            configOverrides.putBoolean(SmsManager.MMS_CONFIG_GROUP_MMS_ENABLED, settings.getGroup());
            String httpParams = MmsConfig.getHttpParams();
            if (!TextUtils.isEmpty(httpParams)) {
                configOverrides.putString(SmsManager.MMS_CONFIG_HTTP_PARAMS, httpParams);
            }
            configOverrides.putInt(SmsManager.MMS_CONFIG_MAX_MESSAGE_SIZE, MmsConfig.getMaxMessageSize());

            if (contentUri != null) {
                SmsManagerFactory.createSmsManager(settings).sendMultimediaMessage(context,
                        contentUri, null, configOverrides, pendingIntent);
            } else {
                Log.e(TAG, "Error writing sending Mms");
                try {
                    pendingIntent.send(SmsManager.MMS_ERROR_IO_ERROR);
                } catch (PendingIntent.CanceledException ex) {
                    Log.e(TAG, "Mms pending intent cancelled?", ex);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "error using system sending method", e);
        }
    }

    private static SendReq buildPdu(Context context, String[] recipients, String subject,
                                    List<MMSPart> parts) {
        final SendReq req = new SendReq();
        // From, per spec
        final String lineNumber = Utils.getMyPhoneNumber(context);
        if (!TextUtils.isEmpty(lineNumber)) {
            req.setFrom(new EncodedStringValue(lineNumber));
        }
        // To
        for (String recipient : recipients) {
            req.addTo(new EncodedStringValue(recipient));
        }
        // Subject
        if (!TextUtils.isEmpty(subject)) {
            req.setSubject(new EncodedStringValue(subject));
        }
        // Date
        req.setDate(System.currentTimeMillis() / 1000);
        // Body
        PduBody body = new PduBody();
        // Add text part. Always add a smil part for compatibility, without it there
        // may be issues on some carriers/client apps
        int size = 0;
        for (int i = 0; i < parts.size(); i++) {
            MMSPart part = parts.get(i);
            size += addTextPart(body, part, i);
        }

        // add a SMIL document for compatibility
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SmilXmlSerializer.serialize(SmilHelper.createSmilDocument(body), out);
        PduPart smilPart = new PduPart();
        smilPart.setContentId("smil".getBytes());
        smilPart.setContentLocation("smil.xml".getBytes());
        smilPart.setContentType(ContentType.APP_SMIL.getBytes());
        smilPart.setData(out.toByteArray());
        body.addPart(0, smilPart);

        req.setBody(body);
        // Message size
        req.setMessageSize(size);
        // Message class
        req.setMessageClass(PduHeaders.MESSAGE_CLASS_PERSONAL_STR.getBytes());
        // Expiry
        req.setExpiry(DEFAULT_EXPIRY_TIME);
        try {
            // Priority
            req.setPriority(DEFAULT_PRIORITY);
            // Delivery report
            req.setDeliveryReport(PduHeaders.VALUE_NO);
            // Read report
            req.setReadReport(PduHeaders.VALUE_NO);
        } catch (InvalidHeaderValueException e) {}

        return req;
    }

    private static int addTextPart(PduBody pb, MMSPart p, int id) {
        String filename = p.Name;
        final PduPart part = new PduPart();
        // Set Charset if it's a text media.
        if (p.MimeType.startsWith("text")) {
            part.setCharset(CharacterSets.UTF_8);
        }
        // Set Content-Type.
        part.setContentType(p.MimeType.getBytes());
        // Set Content-Location.
        part.setContentLocation(filename.getBytes());
        int index = filename.lastIndexOf(".");
        String contentId = (index == -1) ? filename
                : filename.substring(0, index);
        part.setContentId(contentId.getBytes());
        part.setData(p.Data);
        pb.addPart(part);

        return part.getData().length;
    }

    public static class MessageInfo {
        public long token;
        public Uri location;
        public byte[] bytes;
    }

    // splits text and adds split counter when applicable
    private String[] splitByLength(String s, int chunkSize, boolean counter) {
        int arraySize = (int) Math.ceil((double) s.length() / chunkSize);

        String[] returnArray = new String[arraySize];

        int index = 0;
        for (int i = 0; i < s.length(); i = i + chunkSize) {
            if (s.length() - i < chunkSize) {
                returnArray[index++] = s.substring(i);
            } else {
                returnArray[index++] = s.substring(i, i + chunkSize);
            }
        }

        if (counter && returnArray.length > 1) {
            for (int i = 0; i < returnArray.length; i++) {
                returnArray[i] = "(" + (i + 1) + "/" + returnArray.length + ") " + returnArray[i];
            }
        }

        return returnArray;
    }

    private static Uri insert(Context context, String[] to, MMSPart[] parts, String subject) {
        try {
            Uri destUri = Uri.parse("content://mms");

            Set<String> recipients = new HashSet<String>();
            recipients.addAll(Arrays.asList(to));
            long thread_id = Utils.getOrCreateThreadId(context, recipients);

            // Create a dummy sms
            ContentValues dummyValues = new ContentValues();
            dummyValues.put("thread_id", thread_id);
            dummyValues.put("body", " ");
            Uri dummySms = context.getContentResolver().insert(Uri.parse("content://sms/sent"), dummyValues);

            // Create a new message entry
            long now = System.currentTimeMillis();
            ContentValues mmsValues = new ContentValues();
            mmsValues.put("thread_id", thread_id);
            mmsValues.put("date", now / 1000L);
            mmsValues.put("msg_box", 4);
            //mmsValues.put("m_id", System.currentTimeMillis());
            mmsValues.put("read", true);
            mmsValues.put("sub", subject != null ? subject : "");
            mmsValues.put("sub_cs", 106);
            mmsValues.put("ct_t", "application/vnd.wap.multipart.related");

            long imageBytes = 0;

            for (MMSPart part : parts) {
                imageBytes += part.Data.length;
            }

            mmsValues.put("exp", imageBytes);

            mmsValues.put("m_cls", "personal");
            mmsValues.put("m_type", 128); // 132 (RETRIEVE CONF) 130 (NOTIF IND) 128 (SEND REQ)
            mmsValues.put("v", 19);
            mmsValues.put("pri", 129);
            mmsValues.put("tr_id", "T" + Long.toHexString(now));
            mmsValues.put("resp_st", 128);

            // Insert message
            Uri res = context.getContentResolver().insert(destUri, mmsValues);
            String messageId = res.getLastPathSegment().trim();

            // Create part
            for (MMSPart part : parts) {
                if (part.MimeType.startsWith("image")) {
                    createPartImage(context, messageId, part.Data, part.MimeType);
                } else if (part.MimeType.startsWith("text")) {
                    createPartText(context, messageId, new String(part.Data, "UTF-8"));
                }
            }

            // Create addresses
            for (String addr : to) {
                createAddr(context, messageId, addr);
            }

            //res = Uri.parse(destUri + "/" + messageId);

            // Delete dummy sms
            context.getContentResolver().delete(dummySms, null, null);

            return res;
        } catch (Exception e) {
            Log.v("sending_mms_library", "still an error saving... :(");
            Log.e(TAG, "exception thrown", e);
        }

        return null;
    }

    // create the image part to be stored in database
    private static Uri createPartImage(Context context, String id, byte[] imageBytes, String mimeType) throws Exception {
        ContentValues mmsPartValue = new ContentValues();
        mmsPartValue.put("mid", id);
        mmsPartValue.put("ct", mimeType);
        mmsPartValue.put("cid", "<" + System.currentTimeMillis() + ">");
        Uri partUri = Uri.parse("content://mms/" + id + "/part");
        Uri res = context.getContentResolver().insert(partUri, mmsPartValue);

        // Add data to part
        OutputStream os = context.getContentResolver().openOutputStream(res);
        ByteArrayInputStream is = new ByteArrayInputStream(imageBytes);
        byte[] buffer = new byte[256];

        for (int len = 0; (len = is.read(buffer)) != -1; ) {
            os.write(buffer, 0, len);
        }

        os.close();
        is.close();

        return res;
    }

    // create the text part to be stored in database
    private static Uri createPartText(Context context, String id, String text) throws Exception {
        ContentValues mmsPartValue = new ContentValues();
        mmsPartValue.put("mid", id);
        mmsPartValue.put("ct", "text/plain");
        mmsPartValue.put("cid", "<" + System.currentTimeMillis() + ">");
        mmsPartValue.put("text", text);
        Uri partUri = Uri.parse("content://mms/" + id + "/part");
        Uri res = context.getContentResolver().insert(partUri, mmsPartValue);

        return res;
    }

    // add address to the request
    private static Uri createAddr(Context context, String id, String addr) throws Exception {
        ContentValues addrValues = new ContentValues();
        addrValues.put("address", addr);
        addrValues.put("charset", "106");
        addrValues.put("type", 151); // TO
        Uri addrUri = Uri.parse("content://mms/" + id + "/addr");
        Uri res = context.getContentResolver().insert(addrUri, addrValues);

        return res;
    }

    /**
     * A method for checking whether or not a certain message will be sent as mms depending on its contents and the settings
     *
     * @param message is the message that you are checking against
     * @return true if the message will be mms, otherwise false
     */
    public boolean checkMMS(Message message) {
        return message.getImages().length != 0 ||
                (message.getParts().size() != 0) ||
                (settings.getSendLongAsMms() && Utils.getNumPages(settings, message.getText()) > settings.getSendLongAsMmsAfter()) ||
                (message.getAddresses().length > 1 && settings.getGroup()) ||
                message.getSubject() != null;
    }

}
