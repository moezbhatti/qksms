package com.moez.QKSMS.common.utils;

import android.content.Context;
import android.content.pm.PackageManager;

public class PackageUtils {

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
