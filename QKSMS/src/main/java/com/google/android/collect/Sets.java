/*
 * Copyright (C) 2007 The Android Open Source Project
 * Copyright (C) 2015 QK Labs
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

package com.google.android.collect;

import java.util.Collections;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Provides static methods for creating mutable {@code Set} instances easily and
 * other static methods for working with Sets.
 */
public class Sets {

    /**
     * Creates an empty {@code HashSet} instance.
     * <p/>
     * <p><b>Note:</b> if {@code E} is an {@link Enum} type, use {@link
     * java.util.EnumSet#noneOf} instead.
     * <p/>
     * <p><b>Note:</b> if you only need an <i>immutable</i> empty Set,
     * use {@link java.util.Collections#emptySet} instead.
     *
     * @return a newly-created, initially-empty {@code HashSet}
     */
    public static <K> HashSet<K> newHashSet() {
        return new HashSet<K>();
    }

    /**
     * Creates a {@code HashSet} instance containing the given elements.
     * <p/>
     * <p><b>Note:</b> due to a bug in javac 1.5.0_06, we cannot support the
     * following:
     * <p/>
     * <p>{@code Set<Base> set = Sets.newHashSet(sub1, sub2);}
     * <p/>
     * <p>where {@code sub1} and {@code sub2} are references to subtypes of {@code
     * Base}, not of {@code Base} itself. To getConversation around this, you must use:
     * <p/>
     * <p>{@code Set<Base> set = Sets.<Base>newHashSet(sub1, sub2);}
     *
     * @param elements the elements that the set should contain
     * @return a newly-created {@code HashSet} containing those elements (minus
     * duplicates)
     */
    public static <E> HashSet<E> newHashSet(E... elements) {
        int capacity = elements.length * 4 / 3 + 1;
        HashSet<E> set = new HashSet<E>(capacity);
        Collections.addAll(set, elements);
        return set;
    }

    /**
     * Creates an empty {@code SortedSet} instance.
     *
     * @return a newly-created, initially-empty {@code SortedSet}.
     */
    public static <E> SortedSet<E> newSortedSet() {
        return new TreeSet<E>();
    }

    /**
     * Creates a {@code SortedSet} instance containing the given elements.
     *
     * @param elements the elements that the set should contain
     * @return a newly-created {@code SortedSet} containing those elements (minus
     * duplicates)
     */
    public static <E> SortedSet<E> newSortedSet(E... elements) {
        SortedSet<E> set = new TreeSet<E>();
        Collections.addAll(set, elements);
        return set;
    }

}
