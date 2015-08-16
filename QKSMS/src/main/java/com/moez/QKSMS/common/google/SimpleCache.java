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

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;

/**
 * A simple cache with the option of using {@link java.lang.ref.SoftReference SoftReferences} to play well with
 * the garbage collector and an LRU cache eviction algorithm to limit the number
 * of {@link java.lang.ref.SoftReference SoftReferences}.
 * <p>
 * The interface of this class is a subset of {@link java.util.Map}.
 *
 * from Peter Balwin and books app.
 */
public class SimpleCache<K, V> {

    /**
     * A simple LRU cache to prevent the number of {@link java.util.Map.Entry} instances
     * from growing infinitely.
     */
    @SuppressWarnings("serial")
    private class SoftReferenceMap extends LinkedHashMap<K, SoftReference<V>> {

        private final int mMaxCapacity;

        public SoftReferenceMap(int initialCapacity, int maxCapacity, float loadFactor) {
            super(initialCapacity, loadFactor, true);
            mMaxCapacity = maxCapacity;
        }

        @Override
        protected boolean removeEldestEntry(Entry<K, SoftReference<V>> eldest) {
            return size() > mMaxCapacity;
        }
    }

    @SuppressWarnings("serial")
    private class HardReferenceMap extends LinkedHashMap<K, V> {

        private final int mMaxCapacity;

        public HardReferenceMap(int initialCapacity, int maxCapacity, float loadFactor) {
            super(initialCapacity, loadFactor, true);
            mMaxCapacity = maxCapacity;
        }

        @Override
        protected boolean removeEldestEntry(Entry<K, V> eldest) {
            return size() > mMaxCapacity;
        }
    }

    private static <V> V unwrap(SoftReference<V> ref) {
        return ref != null ? ref.get() : null;
    }

    private final SoftReferenceMap mSoftReferences;
    private final HardReferenceMap mHardReferences;

    /**
     * Constructor.
     *
     * @param initialCapacity the initial capacity for the cache.
     * @param maxCapacity the maximum capacity for the
     *            cache (this value may be large if soft references are used because
     *            {@link java.lang.ref.SoftReference SoftReferences} don't consume much memory compared to the
     *            larger data they typically contain).
     * @param loadFactor the initial load balancing factor for the internal
     *            {@link java.util.LinkedHashMap}
     */
    public SimpleCache(int initialCapacity, int maxCapacity, float loadFactor,
            boolean useHardReferences) {
        if (useHardReferences) {
            mSoftReferences = null;
            mHardReferences = new HardReferenceMap(initialCapacity, maxCapacity, loadFactor);
        } else {
            mSoftReferences = new SoftReferenceMap(initialCapacity, maxCapacity, loadFactor);
            mHardReferences = null;
        }
    }

    /**
     * See {@link java.util.Map#get(Object)}.
     */
    public V get(Object key) {
        return mSoftReferences != null ? unwrap(mSoftReferences.get(key))
                : mHardReferences.get(key);
    }

    /**
     * See {@link java.util.Map#put(Object, Object)}.
     */
    public V put(K key, V value) {
        return mSoftReferences != null ?
                unwrap(mSoftReferences.put(key, new SoftReference<V>(value)))
                : mHardReferences.put(key, value);
    }

    /**
     * See {@link java.util.Map#clear()}.
     */
    public void clear() {
        if (mSoftReferences != null) {
            mSoftReferences.clear();
        } else {
            mHardReferences.clear();
        }
    }

    /**
     * See {@link java.util.Map#remove(Object)}.
     */
    public V remove(K key) {
        if (mSoftReferences != null) {
            return unwrap(mSoftReferences.remove(key));
        } else {
            return mHardReferences.remove(key);
        }
    }

}
