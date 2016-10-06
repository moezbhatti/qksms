package com.moez.QKSMS.common;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import rx.Observable;

public class CursorObservable extends Observable<Cursor> {

    public CursorObservable(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        super(subscriber -> {
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);

                if (cursor == null) {
                    subscriber.onCompleted();
                    return;
                }

                while (cursor.moveToNext() && !subscriber.isUnsubscribed()) {
                    subscriber.onNext(cursor);
                }

                subscriber.onCompleted();
            } catch (Exception e) {
                e.printStackTrace();
                subscriber.onError(e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        });
    }
}
