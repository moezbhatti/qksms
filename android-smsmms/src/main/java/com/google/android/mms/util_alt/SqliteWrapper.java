/*
 * Copyright (C) 2015 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.mms.util_alt;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.widget.Toast;
import timber.log.Timber;

public final class SqliteWrapper {
    private static final String SQLITE_EXCEPTION_DETAIL_MESSAGE
                = "unable to open database file";

    private SqliteWrapper() {
        // Forbidden being instantiated.
    }

    // FIXME: It looks like outInfo.lowMemory does not work well as we expected.
    // after run command: adb shell fillup -p 100, outInfo.lowMemory is still false.
    private static boolean isLowMemory(Context context) {
        if (null == context) {
            return false;
        }

        ActivityManager am = (ActivityManager)
                        context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(outInfo);

        return outInfo.lowMemory;
    }

    // FIXME: need to optimize this method.
    private static boolean isLowMemory(SQLiteException e) {
        return e.getMessage().equals(SQLITE_EXCEPTION_DETAIL_MESSAGE);
    }

    public static void checkSQLiteException(Context context, SQLiteException e) {
        if (isLowMemory(e)) {
            Toast.makeText(context, "Low Memory",
                    Toast.LENGTH_SHORT).show();
        } else {
            throw e;
        }
    }

    public static Cursor query(Context context, ContentResolver resolver, Uri uri,
            String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        try {
            return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (SQLiteException e) {
            Timber.e(e, "Catch a SQLiteException when query: ");
            checkSQLiteException(context, e);
            return null;
        }
    }

    public static boolean requery(Context context, Cursor cursor) {
        try {
            return cursor.requery();
        } catch (SQLiteException e) {
            Timber.e(e, "Catch a SQLiteException when requery: ");
            checkSQLiteException(context, e);
            return false;
        }
    }
    public static int update(Context context, ContentResolver resolver, Uri uri,
            ContentValues values, String where, String[] selectionArgs) {
        try {
            return resolver.update(uri, values, where, selectionArgs);
        } catch (SQLiteException e) {
            Timber.e(e, "Catch a SQLiteException when update: ");
            checkSQLiteException(context, e);
            return -1;
        }
    }

    public static int delete(Context context, ContentResolver resolver, Uri uri,
            String where, String[] selectionArgs) {
        try {
            return resolver.delete(uri, where, selectionArgs);
        } catch (SQLiteException e) {
            Timber.e(e, "Catch a SQLiteException when delete: ");
            checkSQLiteException(context, e);
            return -1;
        }
    }

    public static Uri insert(Context context, ContentResolver resolver,
            Uri uri, ContentValues values) {
        try {
            return resolver.insert(uri, values);
        } catch (SQLiteException e) {
            Timber.e(e, "Catch a SQLiteException when insert: ");
            checkSQLiteException(context, e);
            return null;
        }
    }
}
