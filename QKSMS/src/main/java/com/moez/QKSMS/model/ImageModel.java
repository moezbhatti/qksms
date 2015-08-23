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

package com.moez.QKSMS.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.android.mms.dom.smil.SmilMediaElementImpl;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.PduPart;
import com.google.android.mms.pdu_alt.PduPersister;
import com.google.android.mms.smil.SmilHelper;
import com.moez.QKSMS.ContentRestrictionException;
import com.moez.QKSMS.ExceedMessageSizeException;
import com.moez.QKSMS.LogTag;
import com.moez.QKSMS.QKSMSApp;
import com.moez.QKSMS.MmsConfig;
import com.moez.QKSMS.common.google.ItemLoadedCallback;
import com.moez.QKSMS.common.google.ItemLoadedFuture;
import com.moez.QKSMS.common.google.ThumbnailManager;
import com.moez.QKSMS.common.google.UriImage;

import org.w3c.dom.events.Event;
import org.w3c.dom.smil.ElementTime;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class ImageModel extends RegionMediaModel {
    private static final String TAG = "Mms/image";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;

    private static final int PICTURE_SIZE_LIMIT = 100 * 1024;

    /**
     * These are the image content types that MMS supports. Anything else needs to be transcoded
     * into one of these content types before being sent over MMS.
     */
    private static final Set<String> SUPPORTED_MMS_IMAGE_CONTENT_TYPES =
        new HashSet<>(Arrays.asList(new String[] {
                "image/jpeg",
            }));

    private int mWidth;
    private int mHeight;
    private SoftReference<Bitmap> mFullSizeBitmapCache = new SoftReference<>(null);
    private ItemLoadedFuture mItemLoadedFuture;

    public ImageModel(Context context, Uri uri, RegionModel region)
            throws MmsException {
        super(context, SmilHelper.ELEMENT_TAG_IMAGE, uri, region);
        initModelFromUri(uri);
        checkContentRestriction();
    }

    public ImageModel(Context context, String contentType, String src,
            Uri uri, RegionModel region) throws MmsException {
        super(context, SmilHelper.ELEMENT_TAG_IMAGE,
                contentType, src, uri, region);
        decodeImageBounds(uri);
    }

    private void initModelFromUri(Uri uri) throws MmsException {
        UriImage uriImage = new UriImage(mContext, uri);

        mContentType = uriImage.getContentType();
        if (TextUtils.isEmpty(mContentType)) {
            throw new MmsException("Type of media is unknown.");
        }
        mSrc = uriImage.getSrc();
        mWidth = uriImage.getWidth();
        mHeight = uriImage.getHeight();

        if (LOCAL_LOGV) {
            Log.v(TAG, "New ImageModel created:"
                    + " mSrc=" + mSrc
                    + " mContentType=" + mContentType
                    + " mUri=" + uri);
        }
    }

    private void decodeImageBounds(Uri uri) {
        UriImage uriImage = new UriImage(mContext, uri);
        mWidth = uriImage.getWidth();
        mHeight = uriImage.getHeight();

        if (LOCAL_LOGV) {
            Log.v(TAG, "Image bounds: " + mWidth + "x" + mHeight);
        }
    }

    // EventListener Interface
    @Override
    public void handleEvent(Event evt) {
        if (evt.getType().equals(SmilMediaElementImpl.SMIL_MEDIA_START_EVENT)) {
            mVisible = true;
        } else if (mFill != ElementTime.FILL_FREEZE) {
            mVisible = false;
        }

        notifyModelChanged(false);
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    protected void checkContentRestriction() throws ContentRestrictionException {
        ContentRestriction cr = ContentRestrictionFactory.getContentRestriction();
        cr.checkImageContentType(mContentType);
    }

    public ItemLoadedFuture loadThumbnailBitmap(ItemLoadedCallback callback) {
        ThumbnailManager thumbnailManager = QKSMSApp.getApplication().getThumbnailManager();
        mItemLoadedFuture = thumbnailManager.getThumbnail(getUri(), callback);
        return mItemLoadedFuture;
    }

    public void cancelThumbnailLoading() {
        if (mItemLoadedFuture != null && !mItemLoadedFuture.isDone()) {
            if (Log.isLoggable(LogTag.APP, Log.DEBUG)) {
                Log.v(TAG, "cancelThumbnailLoading for: " + this);
            }
            mItemLoadedFuture.cancel(getUri());
            mItemLoadedFuture = null;
        }
    }

    private Bitmap createBitmap(int thumbnailBoundsLimit, Uri uri) {
        byte[] data = UriImage.getResizedImageData(mWidth, mHeight,
                thumbnailBoundsLimit, thumbnailBoundsLimit, PICTURE_SIZE_LIMIT, uri, mContext);
        if (LOCAL_LOGV) {
            Log.v(TAG, "createBitmap size: " + (data == null ? data : data.length));
        }
        return data == null ? null : BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public Bitmap getBitmap(int width, int height)  {
        Bitmap bm = mFullSizeBitmapCache.get();
        if (bm == null) {
            try {
                bm = createBitmap(Math.max(width, height), getUri());
                if (bm != null) {
                    mFullSizeBitmapCache = new SoftReference<Bitmap>(bm);
                }
            } catch (OutOfMemoryError ex) {
                // fall through and return a null bitmap. The callers can handle a null
                // result and show R.drawable.ic_missing_thumbnail_picture
            }
        }
        return bm;
    }

    @Override
    public boolean getMediaResizable() {
        return true;
    }

    @Override
    protected void resizeMedia(int byteLimit, long messageId) throws MmsException {
        UriImage image = new UriImage(mContext, getUri());

        int widthLimit = MmsConfig.getMaxImageWidth();
        int heightLimit = MmsConfig.getMaxImageHeight();
        int size = getMediaSize();
        // In mms_config.xml, the max width has always been declared larger than the max height.
        // Swap the width and height limits if necessary so we scale the picture as little as
        // possible.
        if (image.getHeight() > image.getWidth()) {
            int temp = widthLimit;
            widthLimit = heightLimit;
            heightLimit = temp;
        }

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.v(TAG, "resizeMedia size: " + size + " image.getWidth(): "
                    + image.getWidth() + " widthLimit: " + widthLimit
                    + " image.getHeight(): " + image.getHeight()
                    + " heightLimit: " + heightLimit
                    + " image.getContentType(): " + image.getContentType());
        }

        // Check if we're already within the limits - in which case we don't need to resize.
        // The size can be zero here, even when the media has content. See the comment in
        // MediaModel.initMediaSize. Sometimes it'll compute zero and it's costly to read the
        // whole stream to compute the size. When we call getResizedImageAsPart(), we'll correctly
        // set the size.
        if (size != 0 && size <= byteLimit &&
                image.getWidth() <= widthLimit &&
                image.getHeight() <= heightLimit &&
                SUPPORTED_MMS_IMAGE_CONTENT_TYPES.contains(image.getContentType())) {
            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                Log.v(TAG, "resizeMedia - already sized");
            }
            return;
        }

        PduPart part = image.getResizedImageAsPart(
                widthLimit,
                heightLimit,
                byteLimit);

        if (part == null) {
            throw new ExceedMessageSizeException("Not enough memory to turn image into part: " +
                    getUri());
        }

        // Update the content type because it may have changed due to resizing/recompressing
        mContentType = new String(part.getContentType());

        String src = getSrc();
        byte[] srcBytes = src.getBytes();
        part.setContentLocation(srcBytes);
        int period = src.lastIndexOf(".");
        byte[] contentId = period != -1 ? src.substring(0, period).getBytes() : srcBytes;
        part.setContentId(contentId);

        PduPersister persister = PduPersister.getPduPersister(mContext);
        this.mSize = part.getData().length;

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.v(TAG, "resizeMedia mSize: " + mSize);
        }

        Uri newUri = persister.persistPart(part, messageId, null);
        setUri(newUri);
    }
}
