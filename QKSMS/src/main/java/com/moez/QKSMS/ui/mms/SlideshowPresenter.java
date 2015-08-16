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
import android.os.Handler;
import android.util.Log;

import com.moez.QKSMS.interfaces.AdaptableSlideViewInterface;
import com.moez.QKSMS.interfaces.SlideViewInterface;
import com.moez.QKSMS.interfaces.ViewInterface;
import com.moez.QKSMS.model.AudioModel;
import com.moez.QKSMS.model.ImageModel;
import com.moez.QKSMS.model.LayoutModel;
import com.moez.QKSMS.model.MediaModel;
import com.moez.QKSMS.model.Model;
import com.moez.QKSMS.model.RegionMediaModel;
import com.moez.QKSMS.model.RegionModel;
import com.moez.QKSMS.model.SlideModel;
import com.moez.QKSMS.model.SlideshowModel;
import com.moez.QKSMS.model.TextModel;
import com.moez.QKSMS.model.VideoModel;
import com.moez.QKSMS.common.google.ItemLoadedCallback;

/**
 * A basic presenter of slides.
 */
public class SlideshowPresenter extends Presenter {
    private static final String TAG = "SlideshowPresenter";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;

    protected int mLocation;
    protected final int mSlideNumber;

    protected float mWidthTransformRatio = 1.0f;
    protected float mHeightTransformRatio = 1.0f;

    // Since only the original thread that created a view hierarchy can touch
    // its views, we have to use Handler to manage the views in the some
    // callbacks such as onModelChanged().
    protected final Handler mHandler = new Handler();

    public SlideshowPresenter(Context context, ViewInterface view, Model model) {
        super(context, view, model);
        mLocation = 0;
        mSlideNumber = ((SlideshowModel) mModel).size();

        if (view instanceof AdaptableSlideViewInterface) {
            ((AdaptableSlideViewInterface) view).setOnSizeChangedListener(
                    mViewSizeChangedListener);
        }
    }

    private final AdaptableSlideViewInterface.OnSizeChangedListener mViewSizeChangedListener =
        new AdaptableSlideViewInterface.OnSizeChangedListener() {
        public void onSizeChanged(int width, int height) {
            LayoutModel layout = ((SlideshowModel) mModel).getLayout();
            mWidthTransformRatio = getWidthTransformRatio(
                    width, layout.getLayoutWidth());
            mHeightTransformRatio = getHeightTransformRatio(
                    height, layout.getLayoutHeight());
            // The ratio indicates how to reduce the source to match the View,
            // so the larger one should be used.
            float ratio = mWidthTransformRatio > mHeightTransformRatio ?
                    mWidthTransformRatio : mHeightTransformRatio;
            mWidthTransformRatio = ratio;
            mHeightTransformRatio = ratio;
            if (LOCAL_LOGV) {
                Log.v(TAG, "ratio_w = " + mWidthTransformRatio
                        + ", ratio_h = " + mHeightTransformRatio);
            }
        }
    };

    private float getWidthTransformRatio(int width, int layoutWidth) {
        if (width > 0) {
            return (float) layoutWidth / (float) width;
        }
        return 1.0f;
    }

    private float getHeightTransformRatio(int height, int layoutHeight) {
        if (height > 0) {
            return (float) layoutHeight / (float) height;
        }
        return 1.0f;
    }

    private int transformWidth(int width) {
        return (int) (width / mWidthTransformRatio);
    }

    private int transformHeight(int height) {
        return (int) (height / mHeightTransformRatio);
    }

    @Override
    public void present(ItemLoadedCallback callback) {
        // This is called to show a full-screen slideshow. Presently, all parts of
        // a slideshow (images, sounds, etc.) are loaded and displayed on the UI thread.
        presentSlide((SlideViewInterface) mView, ((SlideshowModel) mModel).get(mLocation));
    }

    /**
     * @param view
     * @param model
     */
    protected void presentSlide(SlideViewInterface view, SlideModel model) {
        view.reset();

        for (MediaModel media : model) {
            if (media instanceof RegionMediaModel) {
                presentRegionMedia(view, (RegionMediaModel) media, true);
            } else if (media.isAudio()) {
                presentAudio(view, (AudioModel) media, true);
            }
        }
    }

    /**
     * @param view
     */
    protected void presentRegionMedia(SlideViewInterface view,
            RegionMediaModel rMedia, boolean dataChanged) {
        RegionModel r = (rMedia).getRegion();
        if (rMedia.isText()) {
            presentText(view, (TextModel) rMedia, r, dataChanged);
        } else if (rMedia.isImage()) {
            presentImage(view, (ImageModel) rMedia, r, dataChanged);
        } else if (rMedia.isVideo()) {
            presentVideo(view, (VideoModel) rMedia, r, dataChanged);
        }
    }

    protected void presentAudio(SlideViewInterface view, AudioModel audio,
            boolean dataChanged) {
        // Set audio only when data changed.
        if (dataChanged) {
            view.setAudio(audio.getUri(), audio.getSrc(), audio.getExtras());
        }

        MediaModel.MediaAction action = audio.getCurrentAction();
        if (action == MediaModel.MediaAction.START) {
            view.startAudio();
        } else if (action == MediaModel.MediaAction.PAUSE) {
            view.pauseAudio();
        } else if (action == MediaModel.MediaAction.STOP) {
            view.stopAudio();
        } else if (action == MediaModel.MediaAction.SEEK) {
            view.seekAudio(audio.getSeekTo());
        }
    }

    protected void presentText(SlideViewInterface view, TextModel text,
            RegionModel r, boolean dataChanged) {
        if (dataChanged) {
            view.setText(text.getSrc(), text.getText());
        }

        if (view instanceof AdaptableSlideViewInterface) {
            ((AdaptableSlideViewInterface) view).setTextRegion(
                    transformWidth(r.getLeft()),
                    transformHeight(r.getTop()),
                    transformWidth(r.getWidth()),
                    transformHeight(r.getHeight()));
        }
        view.setTextVisibility(text.isVisible());
    }

    /**
     * @param view
     * @param image
     * @param r
     */
    protected void presentImage(SlideViewInterface view, ImageModel image,
            RegionModel r, boolean dataChanged) {
        int transformedWidth = transformWidth(r.getWidth());
        int transformedHeight = transformWidth(r.getHeight());

        if (LOCAL_LOGV) {
            Log.v(TAG, "presentImage r.getWidth: " + r.getWidth()
                    + ", r.getHeight: " + r.getHeight() +
                    " transformedWidth: " + transformedWidth +
                    " transformedHeight: " + transformedHeight);
        }

        if (dataChanged) {
            view.setImage(image.getSrc(), image.getBitmap(transformedWidth, transformedHeight));
        }

        if (view instanceof AdaptableSlideViewInterface) {
            ((AdaptableSlideViewInterface) view).setImageRegion(
                    transformWidth(r.getLeft()),
                    transformHeight(r.getTop()),
                    transformedWidth,
                    transformedHeight);
        }
        view.setImageRegionFit(r.getFit());
        view.setImageVisibility(image.isVisible());
    }

    /**
     * @param view
     * @param video
     * @param r
     */
    protected void presentVideo(SlideViewInterface view, VideoModel video,
            RegionModel r, boolean dataChanged) {
        if (dataChanged) {
            view.setVideo(video.getSrc(), video.getUri());
        }

            if (view instanceof AdaptableSlideViewInterface) {
            ((AdaptableSlideViewInterface) view).setVideoRegion(
                    transformWidth(r.getLeft()),
                    transformHeight(r.getTop()),
                    transformWidth(r.getWidth()),
                    transformHeight(r.getHeight()));
        }
        view.setVideoVisibility(video.isVisible());

        MediaModel.MediaAction action = video.getCurrentAction();
        if (action == MediaModel.MediaAction.START) {
            view.startVideo();
        } else if (action == MediaModel.MediaAction.PAUSE) {
            view.pauseVideo();
        } else if (action == MediaModel.MediaAction.STOP) {
            view.stopVideo();
        } else if (action == MediaModel.MediaAction.SEEK) {
            view.seekVideo(video.getSeekTo());
        }
    }

    public void setLocation(int location) {
        mLocation = location;
    }

    public int getLocation() {
        return mLocation;
    }

    public void goBackward() {
        if (mLocation > 0) {
            mLocation--;
        }
    }

    public void goForward() {
        if (mLocation < (mSlideNumber - 1)) {
            mLocation++;
        }
    }

    public void onModelChanged(final Model model, final boolean dataChanged) {
        final SlideViewInterface view = (SlideViewInterface) mView;

        // FIXME: Should be optimized.
        if (model instanceof SlideshowModel) {
            // TODO:
        } else if (model instanceof SlideModel) {
            if (((SlideModel) model).isVisible()) {
                mHandler.post(new Runnable() {
                    public void run() {
                        presentSlide(view, (SlideModel) model);
                    }
                });
            } else {
                mHandler.post(new Runnable() {
                    public void run() {
                        goForward();
                    }
                });
            }
        } else if (model instanceof MediaModel) {
            if (model instanceof RegionMediaModel) {
                mHandler.post(new Runnable() {
                    public void run() {
                        presentRegionMedia(view, (RegionMediaModel) model, dataChanged);
                    }
                });
            } else if (((MediaModel) model).isAudio()) {
                mHandler.post(new Runnable() {
                    public void run() {
                        presentAudio(view, (AudioModel) model, dataChanged);
                    }
                });
            }
        } else if (model instanceof RegionModel) {
            // TODO:
        }
    }

    @Override
    public void cancelBackgroundLoading() {
        // For now, the SlideshowPresenter does no background loading so there is nothing to cancel.
    }
}
