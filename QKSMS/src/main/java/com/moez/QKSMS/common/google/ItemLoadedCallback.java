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

/**
 * Callback interface for a background item loaded request.
 *
 */
public interface ItemLoadedCallback<T> {
    /**
     * Called when an item's loading is complete. At most one of {@code result}
     * and {@code exception} should be non-null.
     *
     * @param result the object result, or {@code null} if the request failed or
     *        was cancelled.
     */
    void onItemLoaded(T result, Throwable exception);
}
