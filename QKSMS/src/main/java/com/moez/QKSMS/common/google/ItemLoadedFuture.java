/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.moez.QKSMS.common.google;

import android.net.Uri;

/**
 * Interface for querying the state of a pending item loading request.
 *
 */
public interface ItemLoadedFuture {
    /**
     * Returns whether the associated task has invoked its callback. Note that
     * in some implementations this value only indicates whether the load
     * request was satisfied synchronously via a cache rather than
     * asynchronously.
     */
    boolean isDone();

    void setIsDone(boolean done);

    void cancel(Uri uri);
}
