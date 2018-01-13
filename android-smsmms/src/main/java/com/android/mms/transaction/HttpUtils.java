/*
 * Copyright 2014 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.transaction;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Config;
import com.klinker.android.logger.Log;

import com.android.mms.logs.LogTag;
import com.android.mms.MmsConfig;

public class HttpUtils {
    private static final String TAG = LogTag.TRANSACTION;

    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;

    public static final int HTTP_POST_METHOD = 1;
    public static final int HTTP_GET_METHOD = 2;

    private static final int MMS_READ_BUFFER = 4096;

    // This is the value to use for the "Accept-Language" header.
    // Once it becomes possible for the user to change the locale
    // setting, this should no longer be static.  We should call
    // getHttpAcceptLanguage instead.
    private static final String HDR_VALUE_ACCEPT_LANGUAGE;

    static {
        HDR_VALUE_ACCEPT_LANGUAGE = getCurrentAcceptLanguage(Locale.getDefault());
    }

    // Definition for necessary HTTP headers.
    private static final String HDR_KEY_ACCEPT = "Accept";
    private static final String HDR_KEY_ACCEPT_LANGUAGE = "Accept-Language";

    private static final String HDR_VALUE_ACCEPT =
        "*/*, application/vnd.wap.mms-message, application/vnd.wap.sic";

    private HttpUtils() {
        // To forbidden instantiate this class.
    }

    /**
     * A helper method to send or retrieve data through HTTP protocol.
     *
     * @param token The token to identify the sending progress.
     * @param url The URL used in a GET request. Null when the method is
     *         HTTP_POST_METHOD.
     * @param pdu The data to be POST. Null when the method is HTTP_GET_METHOD.
     * @param method HTTP_POST_METHOD or HTTP_GET_METHOD.
     * @return A byte array which contains the response data.
     *         If an HTTP error code is returned, an IOException will be thrown.
     * @throws java.io.IOException if any error occurred on network interface or
     *         an HTTP error code(&gt;=400) returned from the server.
     */
    public static byte[] httpConnection(Context context, long token,
            String url, byte[] pdu, int method, boolean isProxySet,
            String proxyHost, int proxyPort) throws IOException {
        if (url == null) {
            throw new IllegalArgumentException("URL must not be null.");
        }

        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "httpConnection: params list");
            Log.v(TAG, "\ttoken\t\t= " + token);
            Log.v(TAG, "\turl\t\t= " + url);
            Log.v(TAG, "\tmethod\t\t= "
                    + ((method == HTTP_POST_METHOD) ? "POST"
                            : ((method == HTTP_GET_METHOD) ? "GET" : "UNKNOWN")));
            Log.v(TAG, "\tisProxySet\t= " + isProxySet);
            Log.v(TAG, "\tproxyHost\t= " + proxyHost);
            Log.v(TAG, "\tproxyPort\t= " + proxyPort);
            // TODO Print out binary data more readable.
            //Log.v(TAG, "\tpdu\t\t= " + Arrays.toString(pdu));
        }

        AndroidHttpClient client = null;

        try {
            // Make sure to use a proxy which supports CONNECT.
            URI hostUrl = new URI(url);
            HttpHost target = new HttpHost(
                    hostUrl.getHost(), hostUrl.getPort(),
                    HttpHost.DEFAULT_SCHEME_NAME);

            client = createHttpClient(context);
            HttpRequest req = null;
            switch(method) {
                case HTTP_POST_METHOD:
                    ProgressCallbackEntity entity = new ProgressCallbackEntity(
                                                        context, token, pdu);
                    // Set request content type.
                    entity.setContentType("application/vnd.wap.mms-message");

                    HttpPost post = new HttpPost(url);
                    post.setEntity(entity);
                    req = post;
                    break;
                case HTTP_GET_METHOD:
                    req = new HttpGet(url);
                    break;
                default:
                    Log.e(TAG, "Unknown HTTP method: " + method
                            + ". Must be one of POST[" + HTTP_POST_METHOD
                            + "] or GET[" + HTTP_GET_METHOD + "].");
                    return null;
            }

            // Set route parameters for the request.
            HttpParams params = client.getParams();
            if (isProxySet) {
                ConnRouteParams.setDefaultProxy(
                        params, new HttpHost(proxyHost, proxyPort));
            }
            req.setParams(params);

            // Set necessary HTTP headers for MMS transmission.
            req.addHeader(HDR_KEY_ACCEPT, HDR_VALUE_ACCEPT);
            {
                String xWapProfileTagName = MmsConfig.getUaProfTagName();
                String xWapProfileUrl = MmsConfig.getUaProfUrl();
                if (xWapProfileUrl != null) {
                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.d(LogTag.TRANSACTION,
                                "[HttpUtils] httpConn: xWapProfUrl=" + xWapProfileUrl);
                    }
                    req.addHeader(xWapProfileTagName, xWapProfileUrl);
                }
            }

            // Extra http parameters. Split by '|' to get a list of value pairs.
            // Separate each pair by the first occurrence of ':' to obtain a name and
            // value. Replace the occurrence of the string returned by
            // MmsConfig.getHttpParamsLine1Key() with the users telephone number inside
            // the value.
            String extraHttpParams = MmsConfig.getHttpParams();

            if (extraHttpParams != null) {
                String line1Number = ((TelephonyManager)context
                        .getSystemService(Context.TELEPHONY_SERVICE))
                        .getLine1Number();
                String line1Key = MmsConfig.getHttpParamsLine1Key();
                String paramList[] = extraHttpParams.split("\\|");

                for (String paramPair : paramList) {
                    String splitPair[] = paramPair.split(":", 2);

                    if (splitPair.length == 2) {
                        String name = splitPair[0].trim();
                        String value = splitPair[1].trim();

                        if (line1Key != null) {
                            value = value.replace(line1Key, line1Number);
                        }
                        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(value)) {
                            req.addHeader(name, value);
                        }
                    }
                }
            }
            req.addHeader(HDR_KEY_ACCEPT_LANGUAGE, HDR_VALUE_ACCEPT_LANGUAGE);

            HttpResponse response = client.execute(target, req);
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != 200) { // HTTP 200 is success.
                throw new IOException("HTTP error: " + status.getReasonPhrase());
            }

            HttpEntity entity = response.getEntity();
            byte[] body = null;
            if (entity != null) {
                try {
                    if (entity.getContentLength() > 0) {
                        body = new byte[(int) entity.getContentLength()];
                        DataInputStream dis = new DataInputStream(entity.getContent());
                        try {
                            dis.readFully(body);
                        } finally {
                            try {
                                dis.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Error closing input stream: " + e.getMessage());
                            }
                        }
                    }
                    if (entity.isChunked()) {
                        Log.v(TAG, "httpConnection: transfer encoding is chunked");
                        int bytesTobeRead = MmsConfig.getMaxMessageSize();
                        byte[] tempBody = new byte[bytesTobeRead];
                        DataInputStream dis = new DataInputStream(entity.getContent());
                        try {
                            int bytesRead = 0;
                            int offset = 0;
                            boolean readError = false;
                            do {
                                try {
                                    bytesRead = dis.read(tempBody, offset, bytesTobeRead);
                                } catch (IOException e) {
                                    readError = true;
                                    Log.e(TAG, "httpConnection: error reading input stream"
                                        + e.getMessage());
                                    break;
                                }
                                if (bytesRead > 0) {
                                    bytesTobeRead -= bytesRead;
                                    offset += bytesRead;
                                }
                            } while (bytesRead >= 0 && bytesTobeRead > 0);
                            if (bytesRead == -1 && offset > 0 && !readError) {
                                // offset is same as total number of bytes read
                                // bytesRead will be -1 if the data was read till the eof
                                body = new byte[offset];
                                System.arraycopy(tempBody, 0, body, 0, offset);
                                Log.v(TAG, "httpConnection: Chunked response length ["
                                    + Integer.toString(offset) + "]");
                            } else {
                                Log.e(TAG, "httpConnection: Response entity too large or empty");
                            }
                        } finally {
                            try {
                                dis.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Error closing input stream: " + e.getMessage());
                            }
                        }
                    }
                } finally {
                    if (entity != null) {
                        entity.consumeContent();
                    }
                }
            }
            return body;
        } catch (URISyntaxException e) {
            handleHttpConnectionException(e, url);
        } catch (IllegalStateException e) {
            handleHttpConnectionException(e, url);
        } catch (IllegalArgumentException e) {
            handleHttpConnectionException(e, url);
        } catch (SocketException e) {
            handleHttpConnectionException(e, url);
        } catch (Exception e) {
            handleHttpConnectionException(e, url);
        }
        finally {
            if (client != null) {
                client.close();
            }
        }
        return null;
    }

    private static void handleHttpConnectionException(Exception exception, String url)
            throws IOException {
        // Inner exception should be logged to make life easier.
        Log.e(TAG, "Url: " + url + "\n" + exception.getMessage());
        IOException e = new IOException(exception.getMessage());
        e.initCause(exception);
        throw e;
    }

    private static AndroidHttpClient createHttpClient(Context context) {
        String userAgent = MmsConfig.getUserAgent();
        AndroidHttpClient client = AndroidHttpClient.newInstance(userAgent, context);
        HttpParams params = client.getParams();
        HttpProtocolParams.setContentCharset(params, "UTF-8");

        // set the socket timeout
        int soTimeout = MmsConfig.getHttpSocketTimeout();

        if (Log.isLoggable(LogTag.TRANSACTION, Log.DEBUG)) {
            Log.d(TAG, "[HttpUtils] createHttpClient w/ socket timeout " + soTimeout + " ms, "
                    + ", UA=" + userAgent);
        }
        HttpConnectionParams.setSoTimeout(params, soTimeout);
        return client;
    }

    private static final String ACCEPT_LANG_FOR_US_LOCALE = "en-US";

    /**
     * Return the Accept-Language header.  Use the current locale plus
     * US if we are in a different locale than US.
     * This code copied from the browser's WebSettings.java
     * @return Current AcceptLanguage String.
     */
    public static String getCurrentAcceptLanguage(Locale locale) {
        StringBuilder buffer = new StringBuilder();
        addLocaleToHttpAcceptLanguage(buffer, locale);

        if (!Locale.US.equals(locale)) {
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append(ACCEPT_LANG_FOR_US_LOCALE);
        }

        return buffer.toString();
    }

    /**
     * Convert obsolete language codes, including Hebrew/Indonesian/Yiddish,
     * to new standard.
     */
    private static String convertObsoleteLanguageCodeToNew(String langCode) {
        if (langCode == null) {
            return null;
        }
        if ("iw".equals(langCode)) {
            // Hebrew
            return "he";
        } else if ("in".equals(langCode)) {
            // Indonesian
            return "id";
        } else if ("ji".equals(langCode)) {
            // Yiddish
            return "yi";
        }
        return langCode;
    }

    private static void addLocaleToHttpAcceptLanguage(StringBuilder builder,
                                                      Locale locale) {
        String language = convertObsoleteLanguageCodeToNew(locale.getLanguage());
        if (language != null) {
            builder.append(language);
            String country = locale.getCountry();
            if (country != null) {
                builder.append("-");
                builder.append(country);
            }
        }
    }
}
