/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.moez.QKSMS;

/**
 * A generic exception that is thrown by checking content restriction.
 */
public class ContentRestrictionException extends RuntimeException {
    private static final long serialVersionUID = 516136015813043499L;

    public ContentRestrictionException() {
        super();
    }

    public ContentRestrictionException(String msg) {
        super(msg);
    }

    public ContentRestrictionException(Exception cause) {
        super(cause);
    }
}
