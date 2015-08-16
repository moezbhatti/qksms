package com.moez.QKSMS.common.utils;

public class ArrayUtils {

    public static <T> int indexOf(T[] arr, T v) {
        for (int i = 0; i < arr.length; i++) {
            if (v == null && arr[i] == null) {
                return i;
            } else if (v != null) {
                if (v.equals(arr[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

}
