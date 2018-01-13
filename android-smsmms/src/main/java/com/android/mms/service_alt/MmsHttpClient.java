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

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.mms.service_alt.exception.MmsHttpException;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.ConnectionSpec;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.internal.Internal;
import com.squareup.okhttp.internal.huc.HttpURLConnectionImpl;
import com.squareup.okhttp.internal.huc.HttpsURLConnectionImpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

/**
 * MMS HTTP client for sending and downloading MMS messages
 */
public class MmsHttpClient {
    private static final String TAG = "MmsHttpClient";

    public static final String METHOD_POST = "POST";
    public static final String METHOD_GET = "GET";

    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    private static final String HEADER_USER_AGENT = "User-Agent";

    // The "Accept" header value
    private static final String HEADER_VALUE_ACCEPT =
            "*/*, application/vnd.wap.mms-message, application/vnd.wap.sic";
    // The "Content-Type" header value
    private static final String HEADER_VALUE_CONTENT_TYPE_WITH_CHARSET =
            "application/vnd.wap.mms-message; charset=utf-8";
    private static final String HEADER_VALUE_CONTENT_TYPE_WITHOUT_CHARSET =
            "application/vnd.wap.mms-message";

    private final Context mContext;
    private final SocketFactory mSocketFactory;
    private final MmsNetworkManager mHostResolver;
    private final ConnectionPool mConnectionPool;

    /**
     * Constructor
     *
     * @param context The Context object
     * @param socketFactory The socket factory for creating an OKHttp client
     * @param hostResolver The host name resolver for creating an OKHttp client
     * @param connectionPool The connection pool for creating an OKHttp client
     */
    public MmsHttpClient(Context context, SocketFactory socketFactory, MmsNetworkManager hostResolver,
            ConnectionPool connectionPool) {
        mContext = context;
        mSocketFactory = socketFactory;
        mHostResolver = hostResolver;
        mConnectionPool = connectionPool;
    }

    /**
     * Execute an MMS HTTP request, either a POST (sending) or a GET (downloading)
     *
     * @param urlString The request URL, for sending it is usually the MMSC, and for downloading
     *                  it is the message URL
     * @param pdu For POST (sending) only, the PDU to send
     * @param method HTTP method, POST for sending and GET for downloading
     * @param isProxySet Is there a proxy for the MMSC
     * @param proxyHost The proxy host
     * @param proxyPort The proxy port
     * @param mmsConfig The MMS config to use
     * @return The HTTP response body
     * @throws MmsHttpException For any failures
     */
    public byte[] execute(String urlString, byte[] pdu, String method, boolean isProxySet,
            String proxyHost, int proxyPort, MmsConfig.Overridden mmsConfig)
            throws MmsHttpException {
        Log.d(TAG, "HTTP: " + method + " " + redactUrlForNonVerbose(urlString)
                + (isProxySet ? (", proxy=" + proxyHost + ":" + proxyPort) : "")
                + ", PDU size=" + (pdu != null ? pdu.length : 0));
        checkMethod(method);
        HttpURLConnection connection = null;
        try {
            Proxy proxy = null;
            if (isProxySet) {
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            }
            final URL url = new URL(urlString);
            // Now get the connection
            connection = openConnection(url, proxy);
            connection.setDoInput(true);
            connection.setConnectTimeout(mmsConfig.getHttpSocketTimeout());
            // ------- COMMON HEADERS ---------
            // Header: Accept
            connection.setRequestProperty(HEADER_ACCEPT, HEADER_VALUE_ACCEPT);
            // Header: Accept-Language
            connection.setRequestProperty(
                    HEADER_ACCEPT_LANGUAGE, getCurrentAcceptLanguage(Locale.getDefault()));
            // Header: User-Agent
            final String userAgent = mmsConfig.getUserAgent();
            Log.i(TAG, "HTTP: User-Agent=" + userAgent);
            connection.setRequestProperty(HEADER_USER_AGENT, userAgent);
            // Header: x-wap-profile
            final String uaProfUrlTagName = mmsConfig.getUaProfTagName();
            final String uaProfUrl = mmsConfig.getUaProfUrl();
            if (uaProfUrl != null) {
                Log.i(TAG, "HTTP: UaProfUrl=" + uaProfUrl);
                connection.setRequestProperty(uaProfUrlTagName, uaProfUrl);
            }
            // Add extra headers specified by mms_config.xml's httpparams
            addExtraHeaders(connection, mmsConfig);
            // Different stuff for GET and POST
            if (METHOD_POST.equals(method)) {
                if (pdu == null || pdu.length < 1) {
                    Log.e(TAG, "HTTP: empty pdu");
                    throw new MmsHttpException(0/*statusCode*/, "Sending empty PDU");
                }
                connection.setDoOutput(true);
                connection.setRequestMethod(METHOD_POST);
                if (mmsConfig.getSupportHttpCharsetHeader()) {
                    connection.setRequestProperty(HEADER_CONTENT_TYPE,
                            HEADER_VALUE_CONTENT_TYPE_WITH_CHARSET);
                } else {
                    connection.setRequestProperty(HEADER_CONTENT_TYPE,
                            HEADER_VALUE_CONTENT_TYPE_WITHOUT_CHARSET);
                }
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    logHttpHeaders(connection.getRequestProperties());
                }
                connection.setFixedLengthStreamingMode(pdu.length);
                // Sending request body
                final OutputStream out =
                        new BufferedOutputStream(connection.getOutputStream());
                out.write(pdu);
                out.flush();
                out.close();
            } else if (METHOD_GET.equals(method)) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    logHttpHeaders(connection.getRequestProperties());
                }
                connection.setRequestMethod(METHOD_GET);
            }
            // Get response
            final int responseCode = connection.getResponseCode();
            final String responseMessage = connection.getResponseMessage();
            Log.d(TAG, "HTTP: " + responseCode + " " + responseMessage);
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                logHttpHeaders(connection.getHeaderFields());
            }
            if (responseCode / 100 != 2) {
                throw new MmsHttpException(responseCode, responseMessage);
            }
            final InputStream in = new BufferedInputStream(connection.getInputStream());
            final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            final byte[] buf = new byte[4096];
            int count = 0;
            while ((count = in.read(buf)) > 0) {
                byteOut.write(buf, 0, count);
            }
            in.close();
            final byte[] responseBody = byteOut.toByteArray();
            Log.d(TAG, "HTTP: response size="
                    + (responseBody != null ? responseBody.length : 0));
            return responseBody;
        } catch (MalformedURLException e) {
            final String redactedUrl = redactUrlForNonVerbose(urlString);
            Log.e(TAG, "HTTP: invalid URL " + redactedUrl, e);
            throw new MmsHttpException(0/*statusCode*/, "Invalid URL " + redactedUrl, e);
        } catch (ProtocolException e) {
            final String redactedUrl = redactUrlForNonVerbose(urlString);
            Log.e(TAG, "HTTP: invalid URL protocol " + redactedUrl, e);
            throw new MmsHttpException(0/*statusCode*/, "Invalid URL protocol " + redactedUrl, e);
        } catch (IOException e) {
            Log.e(TAG, "HTTP: IO failure", e);
            throw new MmsHttpException(0/*statusCode*/, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Open an HTTP connection
     *
     * TODO: The following code is borrowed from android.net.Network.openConnection
     * Once that method supports proxy, we should use that instead
     * Also we should remove the associated HostResolver and ConnectionPool from
     * MmsNetworkManager
     *
     * @param url The URL to connect to
     * @param proxy The proxy to use
     * @return The opened HttpURLConnection
     * @throws MalformedURLException If URL is malformed
     */
    private HttpURLConnection openConnection(URL url, final Proxy proxy) throws MalformedURLException {
        final String protocol = url.getProtocol();
        OkHttpClient okHttpClient;
        if (protocol.equals("http")) {
            okHttpClient = new OkHttpClient();
            okHttpClient.setFollowRedirects(false);
            okHttpClient.setProtocols(Arrays.asList(Protocol.HTTP_1_1));
            okHttpClient.setProxySelector(new ProxySelector() {
                @Override
                public List<Proxy> select(URI uri) {
                    if (proxy != null) {
                        return Arrays.asList(proxy);
                    } else {
                        return new ArrayList<Proxy>();
                    }
                }

                @Override
                public void connectFailed(URI uri, SocketAddress address, IOException failure) {

                }
            });
            okHttpClient.setAuthenticator(new com.squareup.okhttp.Authenticator() {
                @Override
                public Request authenticate(Proxy proxy, Response response) throws IOException {
                    return null;
                }

                @Override
                public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                    return null;
                }
            });
            okHttpClient.setConnectionSpecs(Arrays.asList(ConnectionSpec.CLEARTEXT));
            okHttpClient.setConnectionPool(new ConnectionPool(3, 60000));
            okHttpClient.setSocketFactory(SocketFactory.getDefault());
            Internal.instance.setNetwork(okHttpClient, mHostResolver);

            if (proxy != null) {
                okHttpClient.setProxy(proxy);
            }

            return new HttpURLConnectionImpl(url, okHttpClient);
        } else if (protocol.equals("https")) {
            okHttpClient = new OkHttpClient();
            okHttpClient.setProtocols(Arrays.asList(Protocol.HTTP_1_1));
            HostnameVerifier verifier = HttpsURLConnection.getDefaultHostnameVerifier();
            okHttpClient.setHostnameVerifier(verifier);
            okHttpClient.setSslSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
            okHttpClient.setProxySelector(new ProxySelector() {
                @Override
                public List<Proxy> select(URI uri) {
                    return Arrays.asList(proxy);
                }

                @Override
                public void connectFailed(URI uri, SocketAddress address, IOException failure) {

                }
            });
            okHttpClient.setAuthenticator(new com.squareup.okhttp.Authenticator() {
                @Override
                public Request authenticate(Proxy proxy, Response response) throws IOException {
                    return null;
                }

                @Override
                public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                    return null;
                }
            });
            okHttpClient.setConnectionSpecs(Arrays.asList(ConnectionSpec.CLEARTEXT));
            okHttpClient.setConnectionPool(new ConnectionPool(3, 60000));
            Internal.instance.setNetwork(okHttpClient, mHostResolver);

            return new HttpsURLConnectionImpl(url, okHttpClient);
        } else {
            throw new MalformedURLException("Invalid URL or unrecognized protocol " + protocol);
        }
    }

    private static void logHttpHeaders(Map<String, List<String>> headers) {
        final StringBuilder sb = new StringBuilder();
        if (headers != null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                final String key = entry.getKey();
                final List<String> values = entry.getValue();
                if (values != null) {
                    for (String value : values) {
                        sb.append(key).append('=').append(value).append('\n');
                    }
                }
            }
            Log.v(TAG, "HTTP: headers\n" + sb.toString());
        }
    }

    private static void checkMethod(String method) throws MmsHttpException {
        if (!METHOD_GET.equals(method) && !METHOD_POST.equals(method)) {
            throw new MmsHttpException(0/*statusCode*/, "Invalid method " + method);
        }
    }

    private static final String ACCEPT_LANG_FOR_US_LOCALE = "en-US";

    /**
     * Return the Accept-Language header.  Use the current locale plus
     * US if we are in a different locale than US.
     * This code copied from the browser's WebSettings.java
     *
     * @return Current AcceptLanguage String.
     */
    public static String getCurrentAcceptLanguage(Locale locale) {
        final StringBuilder buffer = new StringBuilder();
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

    private static void addLocaleToHttpAcceptLanguage(StringBuilder builder, Locale locale) {
        final String language = convertObsoleteLanguageCodeToNew(locale.getLanguage());
        if (language != null) {
            builder.append(language);
            final String country = locale.getCountry();
            if (country != null) {
                builder.append("-");
                builder.append(country);
            }
        }
    }

    private static final Pattern MACRO_P = Pattern.compile("##(\\S+)##");
    /**
     * Resolve the macro in HTTP param value text
     * For example, "something##LINE1##something" is resolved to "something9139531419something"
     *
     * @param value The HTTP param value possibly containing macros
     * @return The HTTP param with macro resolved to real value
     */
    private static String resolveMacro(Context context, String value,
            MmsConfig.Overridden mmsConfig) {
        if (TextUtils.isEmpty(value)) {
            return value;
        }
        final Matcher matcher = MACRO_P.matcher(value);
        int nextStart = 0;
        StringBuilder replaced = null;
        while (matcher.find()) {
            if (replaced == null) {
                replaced = new StringBuilder();
            }
            final int matchedStart = matcher.start();
            if (matchedStart > nextStart) {
                replaced.append(value.substring(nextStart, matchedStart));
            }
            final String macro = matcher.group(1);
            final String macroValue = mmsConfig.getHttpParamMacro(context, macro);
            if (macroValue != null) {
                replaced.append(macroValue);
            } else {
                Log.w(TAG, "HTTP: invalid macro " + macro);
            }
            nextStart = matcher.end();
        }
        if (replaced != null && nextStart < value.length()) {
            replaced.append(value.substring(nextStart));
        }
        return replaced == null ? value : replaced.toString();
    }

    /**
     * Add extra HTTP headers from mms_config.xml's httpParams, which is a list of key/value
     * pairs separated by "|". Each key/value pair is separated by ":". Value may contain
     * macros like "##LINE1##" or "##NAI##" which is resolved with methods in this class
     *
     * @param connection The HttpURLConnection that we add headers to
     * @param mmsConfig The MmsConfig object
     */
    private void addExtraHeaders(HttpURLConnection connection, MmsConfig.Overridden mmsConfig) {
        final String extraHttpParams = mmsConfig.getHttpParams();
        if (!TextUtils.isEmpty(extraHttpParams)) {
            // Parse the parameter list
            String paramList[] = extraHttpParams.split("\\|");
            for (String paramPair : paramList) {
                String splitPair[] = paramPair.split(":", 2);
                if (splitPair.length == 2) {
                    final String name = splitPair[0].trim();
                    final String value = resolveMacro(mContext, splitPair[1].trim(), mmsConfig);
                    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(value)) {
                        // Add the header if the param is valid
                        connection.setRequestProperty(name, value);
                    }
                }
            }
        }
    }

    /**
     * Redact the URL for non-VERBOSE logging. Replace url with only the host part and the length
     * of the input URL string.
     *
     * @param urlString
     * @return
     */
    public static String redactUrlForNonVerbose(String urlString) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            // Don't redact for VERBOSE level logging
            return urlString;
        }
        if (TextUtils.isEmpty(urlString)) {
            return urlString;
        }
        String protocol = "http";
        String host = "";
        try {
            final URL url = new URL(urlString);
            protocol = url.getProtocol();
            host = url.getHost();
        } catch (MalformedURLException e) {
            // Ignore
        }
        // Print "http://host[length]"
        final StringBuilder sb = new StringBuilder();
        sb.append(protocol).append("://").append(host)
                .append("[").append(urlString.length()).append("]");
        return sb.toString();
    }
}
