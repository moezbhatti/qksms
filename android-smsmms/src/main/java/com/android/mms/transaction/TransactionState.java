/*
 * Copyright (C) 2007 Esmertec AG.
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.mms.transaction;

import android.net.Uri;

/**
 * TransactionState intends to encapsulate all the informations which would
 * be known by the observers of transactions. To encapsulate Transaction-
 * State into an intent, it should implement Parcelable interface.
 */
public class TransactionState {
    /**
     * Result code indicates the Transaction has not started.
     */
    public static final int INITIALIZED = 0;
    /**
     * Result code indicates the Transaction successfully complete.
     */
    public static final int SUCCESS = 1;
    /**
     * Result code indicates the Transaction failed.
     */
    public static final int FAILED  = 2;

    private Uri mContentUri;
    private int mState;

    public TransactionState() {
        mState = INITIALIZED;
        mContentUri = null;
    }

    /**
     * To represent the current state(or the result of processing) to the
     * ones who wants to know the state.
     *
     * @return Current state of the Transaction.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * To set the state of transaction. This method is only invoked by
     * the transactions.
     *
     * @param state The current state of transaction.
     */
    synchronized void setState(int state) {
        if ((state < INITIALIZED) && (state > FAILED)) {
            throw new IllegalArgumentException("Bad state: " + state);
        }
        mState = state;
    }

    /**
     * To represent the result uri of transaction such as uri of MM.
     *
     * @return Result uri.
     */
    public synchronized Uri getContentUri() {
        return mContentUri;
    }

    /**
     * To set the result uri. This method is only invoked by the transactions.
     *
     * @param uri The result uri.
     */
    synchronized void setContentUri(Uri uri) {
        mContentUri = uri;
    }
}
