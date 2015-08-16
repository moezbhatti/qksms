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

package com.moez.QKSMS.ui.mms;

import android.content.Context;

import com.moez.QKSMS.interfaces.SlideViewInterface;
import com.moez.QKSMS.interfaces.ViewInterface;
import com.moez.QKSMS.model.AudioModel;
import com.moez.QKSMS.model.ImageModel;
import com.moez.QKSMS.model.Model;
import com.moez.QKSMS.model.SlideModel;
import com.moez.QKSMS.model.SlideshowModel;
import com.moez.QKSMS.model.VideoModel;
import com.moez.QKSMS.common.google.ItemLoadedCallback;
import com.moez.QKSMS.common.google.ItemLoadedFuture;
import com.moez.QKSMS.common.google.ThumbnailManager;

public class MmsThumbnailPresenter extends Presenter {
    private static final String TAG = "MmsThumbnailPresenter";
    private ItemLoadedCallback mOnLoadedCallback;
    private ItemLoadedFuture mItemLoadedFuture;

    public MmsThumbnailPresenter(Context context, ViewInterface view, Model model) {
        super(context, view, model);
    }

    @Override
    public void present(ItemLoadedCallback callback) {
        mOnLoadedCallback = callback;
        SlideModel slide = ((SlideshowModel) mModel).get(0);
        if (slide != null) {
            presentFirstSlide((SlideViewInterface) mView, slide);
        }
    }

    private void presentFirstSlide(SlideViewInterface view, SlideModel slide) {
        view.reset();

        if (slide.hasImage()) {
            presentImageThumbnail(view, slide.getImage());
        } else if (slide.hasVideo()) {
            presentVideoThumbnail(view, slide.getVideo());
        } else if (slide.hasAudio()) {
            presentAudioThumbnail(view, slide.getAudio());
        }
    }

    private ItemLoadedCallback<ThumbnailManager.ImageLoaded> mImageLoadedCallback =
            new ItemLoadedCallback<ThumbnailManager.ImageLoaded>() {
        public void onItemLoaded(ThumbnailManager.ImageLoaded imageLoaded, Throwable exception) {
            if (exception == null) {
                if (mItemLoadedFuture != null) {
                    synchronized(mItemLoadedFuture) {
                        mItemLoadedFuture.setIsDone(true);
                    }
                }
                if (mOnLoadedCallback != null) {
                    mOnLoadedCallback.onItemLoaded(imageLoaded, exception);
                } else {
                    // Right now we're only handling image and video loaded callbacks.
                    SlideModel slide = ((SlideshowModel) mModel).get(0);
                    if (slide != null) {
                        if (slide.hasVideo() && imageLoaded.mIsVideo) {
                            ((SlideViewInterface)mView).setVideoThumbnail(null,
                                    imageLoaded.mBitmap);
                        } else if (slide.hasImage() && !imageLoaded.mIsVideo) {
                            ((SlideViewInterface)mView).setImage(null, imageLoaded.mBitmap);
                        }
                    }
                }
            }
        }
    };

    private void presentVideoThumbnail(SlideViewInterface view, VideoModel video) {
        mItemLoadedFuture = video.loadThumbnailBitmap(mImageLoadedCallback);
    }

    private void presentImageThumbnail(SlideViewInterface view, ImageModel image) {
        mItemLoadedFuture = image.loadThumbnailBitmap(mImageLoadedCallback);
    }

    protected void presentAudioThumbnail(SlideViewInterface view, AudioModel audio) {
        view.setAudio(audio.getUri(), audio.getSrc(), audio.getExtras());
    }

    public void onModelChanged(Model model, boolean dataChanged) {
        // TODO Auto-generated method stub
    }

    public void cancelBackgroundLoading() {
        // Currently we only support background loading of thumbnails. If we extend background
        // loading to other media types, we should add a cancelLoading API to Model.
        SlideModel slide = ((SlideshowModel) mModel).get(0);
        if (slide != null && slide.hasImage()) {
            slide.getImage().cancelThumbnailLoading();
        }
    }

}
