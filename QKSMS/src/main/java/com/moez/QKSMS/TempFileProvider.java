// Copyright 2011 Google Inc.
// All Rights Reserved.

package com.moez.QKSMS;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * The TempFileProvider manages a uri, backed by a file, for passing to the camera app for
 * capturing pictures and videos and storing the data in a file in the messaging app.
 */
public class TempFileProvider extends ContentProvider {
    private static String TAG = "TempFileProvider";

    /**
     * The content:// style URL for this table
     */
    public static final Uri SCRAP_CONTENT_URI = Uri.parse("content://mms_temp_file/scrapSpace");

    private static final int MMS_SCRAP_SPACE = 1;
    private static final UriMatcher sURLMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURLMatcher.addURI("mms_temp_file", "scrapSpace", MMS_SCRAP_SPACE);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values,
            String selection, String[] selectionArgs) {
        return 0;
    }

    private ParcelFileDescriptor getTempStoreFd(String mode) {
        String fileName = getScrapPath(getContext());
        ParcelFileDescriptor pfd = null;

        try {
            File file = new File(fileName);

            // make sure the path is valid and directories created for this file.
            File parentFile = file.getParentFile();
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                Log.e(TAG, "[TempFileProvider] tempStoreFd: " + parentFile.getPath() +
                        "does not exist!");
                return null;
            }

            int modeFlags;
            if (mode.equals("r")) {
                modeFlags = ParcelFileDescriptor.MODE_READ_ONLY;
            } else {
                modeFlags = ParcelFileDescriptor.MODE_READ_WRITE
                            | ParcelFileDescriptor.MODE_CREATE
                            | ParcelFileDescriptor.MODE_TRUNCATE;
            }
            pfd = ParcelFileDescriptor.open(file, modeFlags);
        } catch (Exception ex) {
            Log.e(TAG, "getTempStoreFd: error creating pfd for " + fileName, ex);
        }

        return pfd;
    }

    @Override
    public String getType(Uri uri) {
        return "*/*";
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        // if the url is "content://mms/takePictureTempStore", then it means the requester
        // wants a file descriptor to write image data to.

        ParcelFileDescriptor fd = null;
        int match = sURLMatcher.match(uri);

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.d(TAG, "openFile: uri=" + uri + ", mode=" + mode);
        }

        switch (match) {
            case MMS_SCRAP_SPACE:
                fd = getTempStoreFd(mode);
                break;
        }

        return fd;
    }


    /**
     * This is the scrap file we use to store the media attachment when the user
     * chooses to capture a photo to be attached . We pass {#link@Uri} to the Camera app,
     * which streams the captured image to the uri. Internally we write the media content
     * to this file. It's named '.temp.jpg' so Gallery won't pick it up.
     */
    public static String getScrapPath(Context context, String fileName) {
        return context.getExternalCacheDir().getAbsolutePath() + "/" + fileName;
    }

    public static String getScrapPath(Context context) {
        return getScrapPath(context, ".temp.jpg");
    }

    /**
     * renameScrapFile renames the single scrap file to a new name so newer uses of the scrap
     * file won't overwrite the previously captured data.
     * @param fileExtension file extension for the temp file, typically ".jpg" or ".3gp"
     * @param uniqueIdentifier a separator to add to the file to make it unique,
     *        such as the slide number. This parameter can be empty or null.
     * @return uri of renamed file. If there's an error renaming, null will be returned
     */
    public static Uri renameScrapFile(String fileExtension, String uniqueIdentifier,
            Context context) {
        String filePath = getScrapPath(context);
        // There's only a single scrap file, but there can be several slides. We rename
        // the scrap file to a new scrap file with the slide number as part of the filename.

        // Replace the filename ".temp.jpg" with ".temp#.[jpg | 3gp]" where # is the unique
        // identifier. The content of the file may be a picture or a .3gp video.
        if (uniqueIdentifier == null) {
            uniqueIdentifier = "";
        }
        File newTempFile = new File(getScrapPath(context, ".temp" + uniqueIdentifier +
                fileExtension));
        File oldTempFile = new File(filePath);
        // remove any existing file before rename
        boolean deleted = newTempFile.delete();
        if (!oldTempFile.renameTo(newTempFile)) {
            return null;
        }
        return Uri.fromFile(newTempFile);
    }

    /**
     * Pass in a path to a file and this function will return true if it thinks the path
     * points to one of its scrap files.
     * @param path full path of a file
     * @return true if path is a scrap file path
     */
    public static boolean isTempFile(String path) {
        // An admittedly weak determination of a temp file, but sufficient for current needs.
        // For now, the penalty of returning true for a file that isn't a temp file is simply
        // not storing the file's thumbnail in an on-disk thumbnail cache.
        return path.contains(".temp");
    }
}
