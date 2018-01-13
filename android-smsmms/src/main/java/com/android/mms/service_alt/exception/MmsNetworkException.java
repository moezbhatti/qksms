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
 * MMS network exception
 */
public class MmsNetworkException extends Exception {

    public MmsNetworkException() {
        super();
    }

    public MmsNetworkException(String message) {
        super(message);
    }

    public MmsNetworkException(Throwable cause) {
        super(cause);
    }

    public MmsNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
