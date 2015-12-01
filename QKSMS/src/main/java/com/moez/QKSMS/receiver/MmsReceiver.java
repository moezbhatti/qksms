package com.moez.QKSMS.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.android.mms.transaction.TransactionSettings;

public class MmsReceiver extends BroadcastReceiver {
    private final String TAG = "MmsReceiver";

    private ConnectivityManager connectivityManager;
    private TransactionSettings transactionSettings;

    @Override
    public void onReceive(Context context, Intent intent) {
        transactionSettings = new TransactionSettings(context, connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS).getExtraInfo());
        //byte[] rawPdu = HttpUtils.httpConnection(context, mContentLocation, null, HttpUtils.HTTP_GET_METHOD, transactionSettings.isProxySet(), transactionSettings.getProxyAddress(), transactionSettings.getProxyPort());
    }

    /*private boolean beginMmsConnectivity() {
        try {
            int result = connectivityManager.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, Phone.FEATURE_ENABLE_MMS);
            NetworkInfo info = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
            boolean isAvailable = info != null && info.isConnected() && result == Phone.APN_ALREADY_ACTIVE && !Phone.REASON_VOICE_CALL_ENDED.equals(info.getReason());
            return isAvailable;
        } catch(Exception e) {
            return false;
        }
    }

    private static void ensureRouteToHost(ConnectivityManager cm, String url, TransactionSettings settings) throws IOException {
        int inetAddr;
        if (settings.isProxySet()) {
            String proxyAddr = settings.getProxyAddress();
            inetAddr = lookupHost(proxyAddr);
            if (inetAddr == -1) {
                throw new IOException("Cannot establish route for " + url + ": Unknown host");
            } else {
                if (!cm.requestRouteToHost(ConnectivityManager.TYPE_MOBILE_MMS, inetAddr))
                    throw new IOException("Cannot establish route to proxy " + inetAddr);
            }
        } else {
            Uri uri = Uri.parse(url);
            inetAddr = lookupHost(uri.getHost());
            if (inetAddr == -1) {
                throw new IOException("Cannot establish route for " + url + ": Unknown host");
            } else {
                if (!cm.requestRouteToHost(ConnectivityManager.TYPE_MOBILE_MMS, inetAddr))
                    throw new IOException("Cannot establish route to " + inetAddr + " for " + url);
            }
        }
    }

    private static int lookupHost(String hostname) {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            return -1;
        }
        byte[] addrBytes;
        int addr;
        addrBytes = inetAddress.getAddress();
        addr = ((addrBytes[3] & 0xff) << 24) | ((addrBytes[2] & 0xff) << 16) | ((addrBytes[1] & 0xff) << 8) | (addrBytes[0] & 0xff);
        return addr;
    }

    private static void ensureRouteToHostFancy(ConnectivityManager cm, String url, TransactionSettings settings) throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method m = cm.getClass().getMethod("requestRouteToHostAddress", new Class[] { int.class, InetAddress.class });
        InetAddress inetAddr;
        if (settings.isProxySet()) {
            String proxyAddr = settings.getProxyAddress();
            try {
                inetAddr = InetAddress.getByName(proxyAddr);
            } catch (UnknownHostException e) {
                throw new IOException("Cannot establish route for " + url + ": Unknown proxy " + proxyAddr);
            }
            if (!(Boolean) m.invoke(cm, new Object[] { ConnectivityManager.TYPE_MOBILE_MMS, inetAddr }))
                throw new IOException("Cannot establish route to proxy " + inetAddr);
        } else {
            Uri uri = Uri.parse(url);
            try {
                inetAddr = InetAddress.getByName(uri.getHost());
            } catch (UnknownHostException e) {
                throw new IOException("Cannot establish route for " + url + ": Unknown host");
            }
            if (!(Boolean) m.invoke(cm, new Object[] { ConnectivityManager.TYPE_MOBILE_MMS, inetAddr }))
                throw new IOException("Cannot establish route to " + inetAddr + " for " + url);
        }
    }

    private HashSet<String> getRecipients(GenericPdu pdu) {
        PduHeaders header = pdu.getPduHeaders();
        HashMap<Integer, EncodedStringValue[]> addressMap = new HashMap<>(ADDRESS_FIELDS.length);
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
        HashSet<String> recipients = new HashSet<>();
        loadRecipients(PduHeaders.FROM, recipients, addressMap, false);
        loadRecipients(PduHeaders.TO, recipients, addressMap, true);
        return recipients;
    }

    private void loadRecipients(int addressType, HashSet<String> recipients, HashMap<Integer, EncodedStringValue[]> addressMap, boolean excludeMyNumber) {
        EncodedStringValue[] array = addressMap.getConversation(addressType);
        if (array == null) {
            return;
        }
        // If the TO recipients is only a single address, then we can skip loadRecipients when
        // we're excluding our own number because we know that address is our own.
        if (excludeMyNumber && array.length == 1) {
            return;
        }
        String myNumber = excludeMyNumber ? TelephonyManager.getLine1Number() : null;
        for (EncodedStringValue v : array) {
            if (v != null) {
                String number = v.getString();
                if ((myNumber == null || !PhoneNumberUtils.compare(number, myNumber)) && !recipients.contains(number)) {
                    // Only add numbers which aren't my own number.
                    recipients.add(number);
                }
            }
        }
    }

    private void processPduAttachments() throws Exception {
        if (mGenericPdu instanceof MultimediaMessagePdu) {
            PduBody body = ((MultimediaMessagePdu) mGenericPdu).getBody();
            if (body != null) {
                int partsNum = body.getPartsNum();
                for (int i = 0; i < partsNum; i++) {
                    try {
                        PduPart part = body.getPart(i);
                        if (part == null || part.getData() == null || part.getContentType() == null || part.getName() == null)
                            continue;
                        String partType = new String(part.getContentType());
                        String partName = new String(part.getName());
                        Log.d(TAG, "Part Name: " + partName);
                        Log.d(TAG, "Part Type: " + partType);
                        if (ContentType.isTextType(partType)) {
                        } else if (ContentType.isImageType(partType)) {
                        } else if (ContentType.isVideoType(partType)) {
                        } else if (ContentType.isAudioType(partType)) {
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Bad part shouldn't ruin the party for the other parts
                    }
                }
            }
        } else {
            Log.d(TAG, "Not a MultimediaMessagePdu PDU");
        }
    }*/
}
