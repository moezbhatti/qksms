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

import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import com.android.mms.dom.AttrImpl;
import com.android.mms.dom.smil.SmilDocumentImpl;
import com.android.mms.dom.smil.SmilPlayer;
import com.android.mms.dom.smil.parser.SmilXmlSerializer;
import com.google.android.mms.MmsException;
import com.moez.QKSMS.R;
import com.moez.QKSMS.model.LayoutModel;
import com.moez.QKSMS.model.RegionModel;
import com.moez.QKSMS.model.SlideshowModel;
import com.moez.QKSMS.model.SmilHelper;
import com.moez.QKSMS.ui.base.QKActivity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.smil.SMILDocument;
import org.w3c.dom.smil.SMILElement;

import java.io.ByteArrayOutputStream;

/**
 * Plays the given slideshow in full-screen mode with a common controller.
 */
public class SlideshowActivity extends QKActivity implements EventListener {
    private static final String TAG = "SlideshowActivity";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;

    private MediaController mMediaController;
    private SmilPlayer mSmilPlayer;

    private Handler mHandler;

    private SMILDocument mSmilDoc;

    private SlideView mSlideView;
    private int mSlideCount;

    /**
     * @return whether the Smil has MMS conformance layout.
     * Refer to MMS Conformance Document OMA-MMS-CONF-v1_2-20050301-A
     */
    private static final boolean isMMSConformance(SMILDocument smilDoc) {
        SMILElement head = smilDoc.getHead();
        if (head == null) {
            // No 'head' element
            return false;
        }
        NodeList children = head.getChildNodes();
        if (children == null || children.getLength() != 1) {
            // The 'head' element should have only one child.
            return false;
        }
        Node layout = children.item(0);
        if (layout == null || !"layout".equals(layout.getNodeName())) {
            // The child is not layout element
            return false;
        }
        NodeList layoutChildren = layout.getChildNodes();
        if (layoutChildren == null) {
            // The 'layout' element has no child.
            return false;
        }
        int num = layoutChildren.getLength();
        if (num <= 0) {
            // The 'layout' element has no child.
            return false;
        }
        for (int i = 0; i < num; i++) {
            Node layoutChild = layoutChildren.item(i);
            if (layoutChild == null) {
                // The 'layout' child is null.
                return false;
            }
            String name = layoutChild.getNodeName();
            if ("root-layout".equals(name)) {
                continue;
            } else if ("region".equals(name)) {
                NamedNodeMap map = layoutChild.getAttributes();
                for (int j = 0; j < map.getLength(); j++) {
                    Node node = map.item(j);
                    if (node == null) {
                        return false;
                    }
                    String attrName = node.getNodeName();
                    // The attr should be one of left, top, height, width, fit and id
                    if ("left".equals(attrName) || "top".equals(attrName) ||
                            "height".equals(attrName) || "width".equals(attrName) ||
                            "fit".equals(attrName)) {
                        continue;
                    } else if ("id".equals(attrName)) {
                        String value;
                        if (node instanceof AttrImpl) {
                            value = ((AttrImpl)node).getValue();
                        } else {
                            return false;
                        }
                        if ("Text".equals(value) || "Image".equals(value)) {
                            continue;
                        } else {
                            // The id attr is not 'Text' or 'Image'
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            } else {
                // The 'layout' element has the child other than 'root-layout' or 'region'
                return false;
            }
        }
        return true;
    }

    @Override
    public void onCreate(Bundle icicle) {
        // Play slide-show in full-screen mode.
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(icicle);
        mHandler = new Handler();

        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.view_slideshow);

        Intent intent = getIntent();
        Uri msg = intent.getData();
        final SlideshowModel model;

        try {
            model = SlideshowModel.createFromMessageUri(this, msg);
            mSlideCount = model.size();
        } catch (MmsException e) {
            Log.e(TAG, "Cannot present the slide show.", e);
            finish();
            return;
        }

        mSlideView = (SlideView) findViewById(R.id.slide_view);
        new SlideshowPresenter(this, mSlideView, model);

        mHandler.post(new Runnable() {
            private boolean isRotating() {
                return mSmilPlayer.isPausedState()
                        || mSmilPlayer.isPlayingState()
                        || mSmilPlayer.isPlayedState();
            }

            public void run() {
                mSmilPlayer = SmilPlayer.getPlayer();
                if (mSlideCount > 1) {
                    // Only show the slideshow controller if we have more than a single slide.
                    // Otherwise, when we play a sound on a single slide, it appears like
                    // the slide controller should control the sound (seeking, ff'ing, etc).
                    initMediaController();
                    mSlideView.setMediaController(mMediaController);
                }
                // Use SmilHelper.getDocument() to ensure rebuilding the
                // entire SMIL document.
                mSmilDoc = SmilHelper.getDocument(model);
                if (isMMSConformance(mSmilDoc)) {
                    int imageLeft = 0;
                    int imageTop = 0;
                    int textLeft = 0;
                    int textTop = 0;
                    LayoutModel layout = model.getLayout();
                    if (layout != null) {
                        RegionModel imageRegion = layout.getImageRegion();
                        if (imageRegion != null) {
                            imageLeft = imageRegion.getLeft();
                            imageTop = imageRegion.getTop();
                        }
                        RegionModel textRegion = layout.getTextRegion();
                        if (textRegion != null) {
                            textLeft = textRegion.getLeft();
                            textTop = textRegion.getTop();
                        }
                    }
                    mSlideView.enableMMSConformanceMode(textLeft, textTop, imageLeft, imageTop);
                }
                if (DEBUG) {
                    ByteArrayOutputStream ostream = new ByteArrayOutputStream();
                    SmilXmlSerializer.serialize(mSmilDoc, ostream);
                    if (LOCAL_LOGV) {
                        Log.v(TAG, ostream.toString());
                    }
                }

                // Add event listener.
                ((EventTarget) mSmilDoc).addEventListener(
                        SmilDocumentImpl.SMIL_DOCUMENT_END_EVENT,
                        SlideshowActivity.this, false);

                mSmilPlayer.init(mSmilDoc);
                if (isRotating()) {
                    mSmilPlayer.reload();
                } else {
                    mSmilPlayer.play();
                }
            }
        });
    }

    private void initMediaController() {
        mMediaController = new MediaController(SlideshowActivity.this, false);
        mMediaController.setMediaPlayer(new SmilPlayerController(mSmilPlayer));
        mMediaController.setAnchorView(findViewById(R.id.slide_view));
        mMediaController.setPrevNextListeners(
            new OnClickListener() {
              public void onClick(View v) {
                  mSmilPlayer.next();
              }
            },
            new OnClickListener() {
              public void onClick(View v) {
                  mSmilPlayer.prev();
              }
            });
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if ((mSmilPlayer != null) && (mMediaController != null)) {
            mMediaController.show();
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSmilDoc != null) {
            ((EventTarget) mSmilDoc).removeEventListener(
                    SmilDocumentImpl.SMIL_DOCUMENT_END_EVENT, this, false);
        }
        if (mSmilPlayer != null) {
            mSmilPlayer.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if ((null != mSmilPlayer)) {
            if (isFinishing()) {
                mSmilPlayer.stop();
            } else {
                mSmilPlayer.stopWhenReload();
            }
            if (mMediaController != null) {
                // TODO Must set the seek bar change listener null, otherwise if we rotate it
                // while tapping progress bar continuously, window will leak.
                /*View seekBar = mMediaController.findViewById(com.android.internal.R.id.mediacontroller_progress);
                if (seekBar instanceof SeekBar) {
                    ((SeekBar)seekBar).setOnSeekBarChangeListener(null);
                }*/
                // Must do this so we don't leak a window.
                mMediaController.hide();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mSlideView != null) {
            mSlideView.setMediaController(null);
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                break;
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_MENU:
                if ((mSmilPlayer != null) &&
                        (mSmilPlayer.isPausedState()
                        || mSmilPlayer.isPlayingState()
                        || mSmilPlayer.isPlayedState())) {
                    mSmilPlayer.stop();
                }
                break;
            default:
                if ((mSmilPlayer != null) && (mMediaController != null)) {
                    mMediaController.show();
                }
        }
        return super.onKeyDown(keyCode, event);
    }

    private class SmilPlayerController implements MediaPlayerControl {
        private final SmilPlayer mPlayer;
        /**
         * We need to cache the playback state because when the MediaController issues a play or
         * pause command, it expects subsequent calls to {@link #isPlaying()} to return the right
         * value immediately. However, the SmilPlayer executes play and pause asynchronously, so
         * {@link #isPlaying()} will return the wrong value for some time. That's why we keep our
         * own version of the state of whether the player is playing.
         *
         * Initialized to true because we always programatically start the SmilPlayer upon creation
         */
        private boolean mCachedIsPlaying = true;

        public SmilPlayerController(SmilPlayer player) {
            mPlayer = player;
        }

        public int getBufferPercentage() {
            // We don't need to buffer data, always return 100%.
            return 100;
        }

        public int getCurrentPosition() {
            return mPlayer.getCurrentPosition();
        }

        public int getDuration() {
            return mPlayer.getDuration();
        }

        public boolean isPlaying() {
            return mCachedIsPlaying;
        }

        public void pause() {
            mPlayer.pause();
            mCachedIsPlaying = false;
        }

        public void seekTo(int pos) {
            // Don't need to support.
        }

        public void start() {
            mPlayer.start();
            mCachedIsPlaying = true;
        }

        public boolean canPause() {
            return true;
        }

        public boolean canSeekBackward() {
            return true;
        }

        public boolean canSeekForward() {
            return true;
        }

        @Override
        public int getAudioSessionId() {
            return 0;
        }
    }

    public void handleEvent(Event evt) {
        final Event event = evt;
        mHandler.post(new Runnable() {
            public void run() {
                String type = event.getType();
                if(type.equals(SmilDocumentImpl.SMIL_DOCUMENT_END_EVENT)) {
                    finish();
                }
            }
        });
    }
}
