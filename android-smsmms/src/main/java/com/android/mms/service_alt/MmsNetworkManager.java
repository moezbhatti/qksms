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
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkInfo;
import android.net.SSLCertificateSocketFactory;
import android.os.Build;
import android.os.SystemClock;

import com.klinker.android.logger.Log;

import com.android.mms.service_alt.exception.MmsNetworkException;
import com.squareup.okhttp.ConnectionPool;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MmsNetworkManager implements com.squareup.okhttp.internal.Network {
    private static final String TAG = "MmsNetworkManager";
    // Timeout used to call ConnectivityManager.requestNetwork
    private static final int NETWORK_REQUEST_TIMEOUT_MILLIS = 60 * 1000;
    // Wait timeout for this class, a little bit longer than the above timeout
    // to make sure we don't bail prematurely
    private static final int NETWORK_ACQUIRE_TIMEOUT_MILLIS =
            NETWORK_REQUEST_TIMEOUT_MILLIS + (5 * 1000);

    // Borrowed from {@link android.net.Network}
    private static final boolean httpKeepAlive =
            Boolean.parseBoolean(System.getProperty("http.keepAlive", "true"));
    private static final int httpMaxConnections =
            httpKeepAlive ? Integer.parseInt(System.getProperty("http.maxConnections", "5")) : 0;
    private static final long httpKeepAliveDurationMs =
            Long.parseLong(System.getProperty("http.keepAliveDuration", "300000"));  // 5 minutes.

    private final Context mContext;

    // The requested MMS {@link android.net.Network} we are holding
    // We need this when we unbind from it. This is also used to indicate if the
    // MMS network is available.
    private Network mNetwork;
    // The current count of MMS requests that require the MMS network
    // If mMmsRequestCount is 0, we should release the MMS network.
    private int mMmsRequestCount;
    // This is really just for using the capability
    private NetworkRequest mNetworkRequest;
    // The callback to register when we request MMS network
    private ConnectivityManager.NetworkCallback mNetworkCallback;

    private volatile ConnectivityManager mConnectivityManager;

    // The OkHttp's ConnectionPool used by the HTTP client associated with this network manager
    private ConnectionPool mConnectionPool;

    // The MMS HTTP client for this network
    private MmsHttpClient mMmsHttpClient;

    // The SIM ID which we use to connect
    private final int mSubId;

    private boolean permissionError = false;

    public MmsNetworkManager(Context context, int subId) {
        mContext = context;
        mNetworkCallback = null;
        mNetwork = null;
        mMmsRequestCount = 0;
        mConnectivityManager = null;
        mConnectionPool = null;
        mMmsHttpClient = null;
        mSubId = subId;

        if (!MmsRequest.useWifi(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                mNetworkRequest = new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_MMS)
                        .setNetworkSpecifier(Integer.toString(mSubId))
                        .build();
            } else {
                mNetworkRequest = new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_MMS)
                        .build();
            }
        } else {
            mNetworkRequest = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();
        }

        MmsConfigManager.getInstance().init(context);
    }

    /**
     * Acquire the MMS network
     *
     * @throws MmsNetworkException if we fail to acquire it
     */
    public Network acquireNetwork() throws MmsNetworkException {
        synchronized (this) {
            mMmsRequestCount += 1;
            if (mNetwork != null) {
                // Already available
                Log.d(TAG, "MmsNetworkManager: already available");
                return mNetwork;
            }
            Log.d(TAG, "MmsNetworkManager: start new network request");
            // Not available, so start a new request
            newRequest();
            final long shouldEnd = SystemClock.elapsedRealtime() + NETWORK_ACQUIRE_TIMEOUT_MILLIS;
            long waitTime = NETWORK_ACQUIRE_TIMEOUT_MILLIS;
            while (waitTime > 0) {
                try {
                    this.wait(waitTime);
                } catch (InterruptedException e) {
                    Log.w(TAG, "MmsNetworkManager: acquire network wait interrupted");
                }
                if (mNetwork != null || permissionError) {
                    // Success
                    return mNetwork;
                }
                // Calculate remaining waiting time to make sure we wait the full timeout period
                waitTime = shouldEnd - SystemClock.elapsedRealtime();
            }
            // Timed out, so release the request and fail
            Log.d(TAG, "MmsNetworkManager: timed out");
            releaseRequestLocked(mNetworkCallback);
            throw new MmsNetworkException("Acquiring network timed out");
        }
    }

    /**
     * Release the MMS network when nobody is holding on to it.
     */
    public void releaseNetwork() {
        synchronized (this) {
            if (mMmsRequestCount > 0) {
                mMmsRequestCount -= 1;
                Log.d(TAG, "MmsNetworkManager: release, count=" + mMmsRequestCount);
                if (mMmsRequestCount < 1) {
                    releaseRequestLocked(mNetworkCallback);
                }
            }
        }
    }

    /**
     * Start a new {@link NetworkRequest} for MMS
     */
    private void newRequest() {
        final ConnectivityManager connectivityManager = getConnectivityManager();
        mNetworkCallback = new ConnectivityManager.NetworkCallback() {

            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                Log.d(TAG, "NetworkCallbackListener.onAvailable: network=" + network);
                synchronized (MmsNetworkManager.this) {
                    mNetwork = network;
                    MmsNetworkManager.this.notifyAll();
                }
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                Log.d(TAG, "NetworkCallbackListener.onLost: network=" + network);
                synchronized (MmsNetworkManager.this) {
                    releaseRequestLocked(this);
                    MmsNetworkManager.this.notifyAll();
                }
            }

//            @Override
//            public void onUnavailable() {
//                super.onUnavailable();
//                Log.d(TAG, "NetworkCallbackListener.onUnavailable");
//                synchronized (MmsNetworkManager.this) {
//                    releaseRequestLocked(this);
//                    MmsNetworkManager.this.notifyAll();
//                }
//            }
        };

        try {
            connectivityManager.requestNetwork(
                    mNetworkRequest, mNetworkCallback);
        } catch (SecurityException e) {
            Log.e(TAG, "permission exception... skipping it for testing purposes", e);
            permissionError = true;
        }
    }

    /**
     * Release the current {@link NetworkRequest} for MMS
     *
     * @param callback the {@link ConnectivityManager.NetworkCallback} to unregister
     */
    private void releaseRequestLocked(ConnectivityManager.NetworkCallback callback) {
        if (callback != null) {
            final ConnectivityManager connectivityManager = getConnectivityManager();

            try {
                connectivityManager.unregisterNetworkCallback(callback);
            } catch (Exception e) {
                Log.e(TAG, "couldn't unregister", e);
            }
        }
        resetLocked();
    }

    /**
     * Reset the state
     */
    private void resetLocked() {
        mNetworkCallback = null;
        mNetwork = null;
        mMmsRequestCount = 0;
        // Currently we follow what android.net.Network does with ConnectionPool,
        // which is per Network object. So if Network changes, we should clear
        // out the ConnectionPool and thus the MmsHttpClient (since it is linked
        // to a specific ConnectionPool).
        mConnectionPool = null;
        mMmsHttpClient = null;
    }

    private static final InetAddress[] EMPTY_ADDRESS_ARRAY = new InetAddress[0];

    @Override
    public InetAddress[] resolveInetAddresses(String host) throws UnknownHostException {
        Network network = null;
        synchronized (this) {
            if (mNetwork == null) {
                return EMPTY_ADDRESS_ARRAY;
            }
            network = mNetwork;
        }
        return network.getAllByName(host);
    }

    private ConnectivityManager getConnectivityManager() {
        if (mConnectivityManager == null) {
            mConnectivityManager = (ConnectivityManager) mContext.getSystemService(
                    Context.CONNECTIVITY_SERVICE);
        }
        return mConnectivityManager;
    }

    private ConnectionPool getOrCreateConnectionPoolLocked() {
        if (mConnectionPool == null) {
            mConnectionPool = new ConnectionPool(httpMaxConnections, httpKeepAliveDurationMs);
        }
        return mConnectionPool;
    }

    /**
     * Get an MmsHttpClient for the current network
     *
     * @return The MmsHttpClient instance
     */
    public MmsHttpClient getOrCreateHttpClient() {
        synchronized (this) {
            if (mMmsHttpClient == null) {
                if (mNetwork != null) {
                    // Create new MmsHttpClient for the current Network
                    mMmsHttpClient = new MmsHttpClient(
                            mContext,
                            mNetwork.getSocketFactory(),
                            MmsNetworkManager.this,
                            getOrCreateConnectionPoolLocked());
                } else if (permissionError) {
                    mMmsHttpClient = new MmsHttpClient(
                            mContext,
                            new SSLCertificateSocketFactory(NETWORK_REQUEST_TIMEOUT_MILLIS),
                            MmsNetworkManager.this,
                            getOrCreateConnectionPoolLocked());
                }
            }
            return mMmsHttpClient;
        }
    }

    /**
     * Get the APN name for the active network
     *
     * @return The APN name if available, otherwise null
     */
    public String getApnName() {
        Network network = null;
        synchronized (this) {
            if (mNetwork == null) {
                Log.d(TAG, "MmsNetworkManager: getApnName: network not available");
                mNetworkRequest = new NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build();
                return null;
            }
            network = mNetwork;
        }
        String apnName = null;
        final ConnectivityManager connectivityManager = getConnectivityManager();
        NetworkInfo mmsNetworkInfo = connectivityManager.getNetworkInfo(network);
        if (mmsNetworkInfo != null) {
            apnName = mmsNetworkInfo.getExtraInfo();
        }
        Log.d(TAG, "MmsNetworkManager: getApnName: " + apnName);
        return apnName;
    }

}
