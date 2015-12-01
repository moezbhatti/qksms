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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;

import com.moez.QKSMS.LogTag;
import com.moez.QKSMS.R;
import com.moez.QKSMS.TempFileProvider;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Set;

/**
 * Primary {@link ThumbnailManager} implementation used by @link MessagingApplication.
 * <p>
 * Public methods should only be used from a single thread (typically the UI
 * thread). Callbacks will be invoked on the thread where the ThumbnailManager
 * was instantiated.
 * <p>
 * Uses a thread-pool ExecutorService instead of AsyncTasks since clients may
 * request lots of pdus around the same time, and AsyncTask may reject tasks
 * in that case and has no way of bounding the number of threads used by those
 * tasks.
 * <p>
 * ThumbnailManager is used to asynchronously load pictures and create thumbnails. The thumbnails
 * are stored in a local cache with SoftReferences. Once a thumbnail is loaded, it will call the
 * passed in callback with the result. If a thumbnail is immediately available in the cache,
 * the callback will be called immediately as well.
 *
 * Based on BooksImageManager by Virgil King.
 */
public class ThumbnailManager extends BackgroundLoaderManager {
    private static final String TAG = "ThumbnailManager";

    private static final boolean DEBUG_DISABLE_CACHE = false;
    private static final boolean DEBUG_DISABLE_CALLBACK = false;
    private static final boolean DEBUG_DISABLE_LOAD = false;
    private static final boolean DEBUG_LONG_WAIT = false;

    private static final int COMPRESS_JPEG_QUALITY = 90;

    private final SimpleCache<Uri, Bitmap> mThumbnailCache;
    private final Context mContext;
    private ImageCacheService mImageCacheService;
    private static Bitmap mEmptyImageBitmap;
    private static Bitmap mEmptyVideoBitmap;

    // NOTE: These type numbers are stored in the image cache, so it should not
    // not be changed without resetting the cache.
    public static final int TYPE_THUMBNAIL = 1;
    public static final int TYPE_MICROTHUMBNAIL = 2;

    public static final int THUMBNAIL_TARGET_SIZE = 640;

    public ThumbnailManager(final Context context) {
        super(context);

        mThumbnailCache = new SimpleCache<>(8, 16, 0.75f, true);
        mContext = context;

        mEmptyImageBitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_error);

        mEmptyVideoBitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_error);
    }

    /**
     * getThumbnail must be called on the same thread that created ThumbnailManager. This is
     * normally the UI thread.
     * @param uri the uri of the image
     * @param callback the callback to call when the thumbnail is fully loaded
     * @return
     */
    public ItemLoadedFuture getThumbnail(Uri uri, final ItemLoadedCallback<ImageLoaded> callback) {
        return getThumbnail(uri, false, callback);
    }

    /**
     * getVideoThumbnail must be called on the same thread that created ThumbnailManager. This is
     * normally the UI thread.
     * @param uri the uri of the image
     * @param callback the callback to call when the thumbnail is fully loaded
     * @return
     */
    public ItemLoadedFuture getVideoThumbnail(Uri uri,
            final ItemLoadedCallback<ImageLoaded> callback) {
        return getThumbnail(uri, true, callback);
    }

    private ItemLoadedFuture getThumbnail(Uri uri, boolean isVideo,
            final ItemLoadedCallback<ImageLoaded> callback) {
        if (uri == null) {
            throw new NullPointerException();
        }

        final Bitmap thumbnail = DEBUG_DISABLE_CACHE ? null : mThumbnailCache.get(uri);

        final boolean thumbnailExists = (thumbnail != null);
        final boolean taskExists = mPendingTaskUris.contains(uri);
        final boolean newTaskRequired = !thumbnailExists && !taskExists;
        final boolean callbackRequired = (callback != null);

        if (Log.isLoggable(LogTag.THUMBNAIL_CACHE, Log.DEBUG)) {
            Log.v(TAG, "getThumbnail mThumbnailCache.getConversation for uri: " + uri + " thumbnail: " +
                    thumbnail + " callback: " + callback + " thumbnailExists: " +
                    thumbnailExists + " taskExists: " + taskExists +
                    " newTaskRequired: " + newTaskRequired +
                    " callbackRequired: " + callbackRequired);
        }

        if (thumbnailExists) {
            if (callbackRequired && !DEBUG_DISABLE_CALLBACK) {
                ImageLoaded imageLoaded = new ImageLoaded(thumbnail, isVideo);
                callback.onItemLoaded(imageLoaded, null);
            }
            return new NullItemLoadedFuture();
        }

        if (callbackRequired) {
            addCallback(uri, callback);
        }

        if (newTaskRequired) {
            mPendingTaskUris.add(uri);
            Runnable task = new ThumbnailTask(uri, isVideo);
            mExecutor.execute(task);
        }
        return new ItemLoadedFuture() {
            private boolean mIsDone;

            @Override
            public void cancel(Uri uri) {
                cancelCallback(callback);
                removeThumbnail(uri);   // if the thumbnail is half loaded, force a reload next time
            }

            @Override
            public void setIsDone(boolean done) {
                mIsDone = done;
            }

            @Override
            public boolean isDone() {
                return mIsDone;
            }
        };
    }

    @Override
    public synchronized void clear() {
        super.clear();

        mThumbnailCache.clear();    // clear in-memory cache
        clearBackingStore();        // clear on-disk cache
    }

    // Delete the on-disk cache, but leave the in-memory cache intact
    public synchronized void clearBackingStore() {
        if (mImageCacheService == null) {
            // No need to call getImageCacheService() to renew the instance if it's null.
            // It's enough to only delete the image cache files for the sake of safety.
            CacheManager.clear(mContext);
        } else {
            getImageCacheService().clear();

            // force a re-init the next time getImageCacheService requested
            mImageCacheService = null;
        }
    }

    public void removeThumbnail(Uri uri) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "removeThumbnail: " + uri);
        }
        if (uri != null) {
            mThumbnailCache.remove(uri);
        }
    }

    @Override
    public String getTag() {
        return TAG;
    }

    private synchronized ImageCacheService getImageCacheService() {
        if (mImageCacheService == null) {
            mImageCacheService = new ImageCacheService(mContext);
        }
        return mImageCacheService;
    }

    public class ThumbnailTask implements Runnable {
        private final Uri mUri;
        private final boolean mIsVideo;

        public ThumbnailTask(Uri uri, boolean isVideo) {
            if (uri == null) {
                throw new NullPointerException();
            }
            mUri = uri;
            mIsVideo = isVideo;
        }

        /** {@inheritDoc} */
        @Override
        public void run() {
            if (DEBUG_DISABLE_LOAD) {
                return;
            }
            if (DEBUG_LONG_WAIT) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                }
            }

            Bitmap bitmap = null;
            try {
                bitmap = getBitmap(mIsVideo);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Couldn't load bitmap for " + mUri, e);
            } catch (OutOfMemoryError e) {
                Log.e(TAG, "Couldn't load bitmap for " + mUri, e);
            }
            final Bitmap resultBitmap = bitmap;

            mCallbackHandler.post(new Runnable() {
                @Override
                public void run() {
                    final Set<ItemLoadedCallback> callbacks = mCallbacks.get(mUri);
                    if (callbacks != null) {
                        Bitmap bitmap = resultBitmap == null ?
                                (mIsVideo ? mEmptyVideoBitmap : mEmptyImageBitmap)
                                : resultBitmap;

                        // Make a copy so that the callback can unregister itself
                        for (final ItemLoadedCallback<ImageLoaded> callback : asList(callbacks)) {
                            if (Log.isLoggable(LogTag.THUMBNAIL_CACHE, Log.DEBUG)) {
                                Log.d(TAG, "Invoking item loaded callback " + callback);
                            }
                            if (!DEBUG_DISABLE_CALLBACK) {
                                ImageLoaded imageLoaded = new ImageLoaded(bitmap, mIsVideo);
                                callback.onItemLoaded(imageLoaded, null);
                            }
                        }
                    } else {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "No image callback!");
                        }
                    }

                    // Add the bitmap to the soft cache if the load succeeded. Don't cache the
                    // stand-ins for empty bitmaps.
                    if (resultBitmap != null) {
                        mThumbnailCache.put(mUri, resultBitmap);
                        if (Log.isLoggable(LogTag.THUMBNAIL_CACHE, Log.DEBUG)) {
                            Log.v(TAG, "in callback runnable: bitmap uri: " + mUri +
                                    " width: " + resultBitmap.getWidth() + " height: " +
                                    resultBitmap.getHeight() + " size: " +
                                    resultBitmap.getByteCount());
                        }
                    }

                    mCallbacks.remove(mUri);
                    mPendingTaskUris.remove(mUri);

                    if (Log.isLoggable(LogTag.THUMBNAIL_CACHE, Log.DEBUG)) {
                        Log.d(TAG, "Image task for " + mUri + "exiting " + mPendingTaskUris.size()
                                + " remain");
                    }
                }
            });
        }

        private Bitmap getBitmap(boolean isVideo) {
            ImageCacheService cacheService = getImageCacheService();

            UriImage uriImage = new UriImage(mContext, mUri);
            String path = uriImage.getPath();

            if (path == null) {
                return null;
            }

            // We never want to store thumbnails of temp files in the thumbnail cache on disk
            // because those temp filenames are recycled (and reused when capturing images
            // or videos).
            boolean isTempFile = TempFileProvider.isTempFile(path);

            ImageCacheService.ImageData data = null;
            if (!isTempFile) {
                data = cacheService.getImageData(path, TYPE_THUMBNAIL);
            }

            if (data != null) {
                Options options = new Options();
                options.inPreferredConfig = Config.ARGB_8888;
                Bitmap bitmap = requestDecode(data.mData,
                        data.mOffset, data.mData.length - data.mOffset, options);
                if (bitmap == null) {
                    Log.w(TAG, "decode cached failed " + path);
                }
                return bitmap;
            } else {
                Bitmap bitmap;
                if (isVideo) {
                    bitmap = getVideoBitmap();
                } else {
                    bitmap = onDecodeOriginal(mUri, TYPE_THUMBNAIL);
                }
                if (bitmap == null) {
                    Log.w(TAG, "decode orig failed " + path);
                    return null;
                }

                bitmap = resizeDownBySideLength(bitmap, THUMBNAIL_TARGET_SIZE, true);

                if (!isTempFile) {
                    byte[] array = compressBitmap(bitmap);
                    cacheService.putImageData(path, TYPE_THUMBNAIL, array);
                }
                return bitmap;
            }
        }

        private Bitmap getVideoBitmap() {
            // TODO Stagefright
            /* REMOVED FOR STAGEFRIGHT BUG
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(mContext, mUri);
                return retriever.getFrameAtTime(-1);
            } catch (RuntimeException ex) {
                // Assume this is a corrupt video file.
            } finally {
                try {
                    retriever.release();
                } catch (RuntimeException ex) {
                    // Ignore failures while cleaning up.
                }
            } */
            return null;
        }

        private byte[] compressBitmap(Bitmap bitmap) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,
                    COMPRESS_JPEG_QUALITY, os);
            return os.toByteArray();
        }

        private Bitmap requestDecode(byte[] bytes, int offset,
                int length, Options options) {
            if (options == null) {
                options = new Options();
            }
            return ensureGLCompatibleBitmap(
                    BitmapFactory.decodeByteArray(bytes, offset, length, options));
        }

        private Bitmap resizeDownBySideLength(
                Bitmap bitmap, int maxLength, boolean recycle) {
            int srcWidth = bitmap.getWidth();
            int srcHeight = bitmap.getHeight();
            float scale = Math.min(
                    (float) maxLength / srcWidth, (float) maxLength / srcHeight);
            if (scale >= 1.0f) return bitmap;
            return resizeBitmapByScale(bitmap, scale, recycle);
        }

        private Bitmap resizeBitmapByScale(
                Bitmap bitmap, float scale, boolean recycle) {
            int width = Math.round(bitmap.getWidth() * scale);
            int height = Math.round(bitmap.getHeight() * scale);
            if (width == bitmap.getWidth()
                    && height == bitmap.getHeight()) return bitmap;
            Bitmap target = Bitmap.createBitmap(width, height, getConfig(bitmap));
            Canvas canvas = new Canvas(target);
            canvas.scale(scale, scale);
            Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
            canvas.drawBitmap(bitmap, 0, 0, paint);
            if (recycle) bitmap.recycle();
            return target;
        }

        private Config getConfig(Bitmap bitmap) {
            Config config = bitmap.getConfig();
            if (config == null) {
                config = Config.ARGB_8888;
            }
            return config;
        }

        // TODO: This function should not be called directly from
        // DecodeUtils.requestDecode(...), since we don't have the knowledge
        // if the bitmap will be uploaded to GL.
        private Bitmap ensureGLCompatibleBitmap(Bitmap bitmap) {
            if (bitmap == null || bitmap.getConfig() != null) return bitmap;
            Bitmap newBitmap = bitmap.copy(Config.ARGB_8888, false);
            bitmap.recycle();
            return newBitmap;
        }

        private Bitmap onDecodeOriginal(Uri uri, int type) {
            Options options = new Options();
            options.inPreferredConfig = Config.ARGB_8888;

            return requestDecode(uri, options, THUMBNAIL_TARGET_SIZE);
        }

        private void closeSilently(Closeable c) {
            if (c == null) return;
            try {
                c.close();
            } catch (Throwable t) {
                Log.w(TAG, "close fail", t);
            }
        }

        private Bitmap requestDecode(final Uri uri, Options options, int targetSize) {
            if (options == null) options = new Options();

            InputStream inputStream;
            try {
                inputStream = mContext.getContentResolver().openInputStream(uri);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Can't open uri: " + uri, e);
                return null;
            }

            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            closeSilently(inputStream);

            // No way to reset the stream. Have to open it again :-(
            try {
                inputStream = mContext.getContentResolver().openInputStream(uri);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Can't open uri: " + uri, e);
                return null;
            }

            options.inSampleSize = computeSampleSizeLarger(
                    options.outWidth, options.outHeight, targetSize);
            options.inJustDecodeBounds = false;

            Bitmap result = BitmapFactory.decodeStream(inputStream, null, options);
            closeSilently(inputStream);

            if (result == null) {
                return null;
            }

            // We need to resize down if the decoder does not support inSampleSize.
            // (For example, GIF images.)
            result = resizeDownIfTooBig(result, targetSize, true);
            result = ensureGLCompatibleBitmap(result);

            int orientation = UriImage.getOrientation(mContext, uri);
            // Rotate the bitmap if we need to.
            if (result != null && orientation != 0) {
                result = UriImage.rotateBitmap(result, orientation);
            }
            return result;
        }

        // This computes a sample size which makes the longer side at least
        // minSideLength long. If that's not possible, return 1.
        private int computeSampleSizeLarger(int w, int h,
                int minSideLength) {
            int initialSize = Math.max(w / minSideLength, h / minSideLength);
            if (initialSize <= 1) return 1;

            return initialSize <= 8
                    ? prevPowerOf2(initialSize)
                    : initialSize / 8 * 8;
        }

        // Returns the previous power of two.
        // Returns the input if it is already power of 2.
        // Throws IllegalArgumentException if the input is <= 0
        private int prevPowerOf2(int n) {
            if (n <= 0) throw new IllegalArgumentException();
            return Integer.highestOneBit(n);
        }

        // Resize the bitmap if each side is >= targetSize * 2
        private Bitmap resizeDownIfTooBig(
                Bitmap bitmap, int targetSize, boolean recycle) {
            int srcWidth = bitmap.getWidth();
            int srcHeight = bitmap.getHeight();
            float scale = Math.max(
                    (float) targetSize / srcWidth, (float) targetSize / srcHeight);
            if (scale > 0.5f) return bitmap;
            return resizeBitmapByScale(bitmap, scale, recycle);
        }
    }

    public static class ImageLoaded {
        public final Bitmap mBitmap;
        public final boolean mIsVideo;

        public ImageLoaded(Bitmap bitmap, boolean isVideo) {
            mBitmap = bitmap;
            mIsVideo = isVideo;
        }
    }
}
