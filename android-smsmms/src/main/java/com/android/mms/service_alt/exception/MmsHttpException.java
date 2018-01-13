/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.mms.service_alt.exception;

/**
 * HTTP exception
 */
public class MmsHttpException extends Exception {
    // Optional HTTP status code. 0 means ignore. Otherwise this
    // should be a valid HTTP status code.
    private final int mStatusCode;

    public MmsHttpException(int statusCode) {
        super();
        mStatusCode = statusCode;
    }

    public MmsHttpException(int statusCode, String message) {
        super(message);
        mStatusCode = statusCode;
    }

    public MmsHttpException(int statusCode, Throwable cause) {
        super(cause);
        mStatusCode = statusCode;
    }

    public MmsHttpException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        mStatusCode = statusCode;
    }

    public int getStatusCode() {
        return mStatusCode;
    }
}
