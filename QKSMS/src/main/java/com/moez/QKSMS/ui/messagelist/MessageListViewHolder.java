package com.moez.QKSMS.ui.messagelist;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.google.ItemLoadedCallback;
import com.moez.QKSMS.common.google.ThumbnailManager;
import com.moez.QKSMS.interfaces.SlideViewInterface;
import com.moez.QKSMS.ui.base.ClickyViewHolder;
import com.moez.QKSMS.ui.mms.Presenter;
import com.moez.QKSMS.ui.view.AvatarView;
import com.moez.QKSMS.ui.view.QKTextView;

import java.util.Map;

public class MessageListViewHolder extends ClickyViewHolder<MessageItem> implements SlideViewInterface {
    private final String TAG = "MessageListViewHolder";

    // Views
    protected View mRoot;
    protected QKTextView mBodyTextView;
    protected QKTextView mDateView;
    protected ImageView mLockedIndicator;
    protected ImageView mDeliveredIndicator;
    protected ImageView mDetailsIndicator;
    protected AvatarView mAvatarView;
    protected LinearLayout mMessageBlock;
    protected View mSpace;
    // MMS only views. These are nested within ViewStubs, so we don't need to obtain a reference
    // to them right away. We'll inflate the stub only when needed
    protected View mMmsView;
    protected ImageView mImageView;
    protected ImageButton mSlideShowButton;
    protected Button mDownloadButton;
    protected QKTextView mDownloadingLabel;

    protected ImageLoadedCallback mImageLoadedCallback;
    protected Presenter mPresenter;

    public MessageListViewHolder(View view) {
        super(view);

        mRoot = view;
        mBodyTextView = (QKTextView) view.findViewById(R.id.text_view);
        mDateView = (QKTextView) view.findViewById(R.id.date_view);
        mLockedIndicator = (ImageView) view.findViewById(R.id.locked_indicator);
        mDeliveredIndicator = (ImageView) view.findViewById(R.id.delivered_indicator);
        mDetailsIndicator = (ImageView) view.findViewById(R.id.details_indicator);
        mAvatarView = (AvatarView) view.findViewById(R.id.avatar);
        mMessageBlock = (LinearLayout) view.findViewById(R.id.message_block);
        mSpace = view.findViewById(R.id.space);
    }

    protected void showMmsView(boolean visible) {
        if (mMmsView == null) {
            mMmsView = mRoot.findViewById(R.id.mms_view);
            // if mMmsView is still null here, that mean the mms section hasn't been inflated

            if (visible && mMmsView == null) {
                // inflate the mms view_stub
                View mmsStub = mRoot.findViewById(R.id.mms_layout_view_stub);
                mmsStub.setVisibility(View.VISIBLE);
                mMmsView = mRoot.findViewById(R.id.mms_view);
            }
        }
        if (mMmsView != null) {
            if (mImageView == null) {
                mImageView = (ImageView) mRoot.findViewById(R.id.image_view);
            }
            if (mSlideShowButton == null) {
                mSlideShowButton = (ImageButton) mRoot.findViewById(R.id.play_slideshow_button);
            }
            mMmsView.setVisibility(visible ? View.VISIBLE : View.GONE);
            mImageView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    protected void inflateDownloadControls() {
        if (mDownloadButton == null) {
            mRoot.findViewById(R.id.mms_downloading_view_stub).setVisibility(View.VISIBLE);
            mDownloadButton = (Button) mRoot.findViewById(R.id.btn_download_msg);
            mDownloadingLabel = (QKTextView) mRoot.findViewById(R.id.label_downloading);
        }
    }

    @Override
    public void setImage(String name, Bitmap bitmap) {
        showMmsView(true);

        try {
            mImageView.setImageBitmap(bitmap);
            mImageView.setVisibility(View.VISIBLE);
        } catch (java.lang.OutOfMemoryError e) {
            Log.e(TAG, "setImage: out of memory: ", e);
        }
    }

    @Override
    public void setImageRegionFit(String fit) {

    }

    @Override
    public void setImageVisibility(boolean visible) {

    }

    @Override
    public void setVideo(String name, Uri video) {

    }

    @Override
    public void setVideoThumbnail(String name, Bitmap bitmap) {
        showMmsView(true);

        try {
            mImageView.setImageBitmap(bitmap);
            mImageView.setVisibility(View.VISIBLE);
        } catch (java.lang.OutOfMemoryError e) {
            Log.e(TAG, "setVideo: out of memory: ", e);
        }
    }

    @Override
    public void setVideoVisibility(boolean visible) {

    }

    @Override
    public void startVideo() {

    }

    @Override
    public void stopVideo() {

    }

    @Override
    public void pauseVideo() {

    }

    @Override
    public void seekVideo(int seekTo) {

    }

    @Override
    public void setAudio(Uri audio, String name, Map<String, ?> extras) {

    }

    @Override
    public void startAudio() {

    }

    @Override
    public void stopAudio() {

    }

    @Override
    public void pauseAudio() {

    }

    @Override
    public void seekAudio(int seekTo) {

    }

    @Override
    public void setText(String name, String text) {

    }

    @Override
    public void setTextVisibility(boolean visible) {

    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setVisibility(boolean visible) {

    }

    static protected class ImageLoadedCallback implements ItemLoadedCallback<ThumbnailManager.ImageLoaded> {
        private long mMessageId;
        private final MessageListViewHolder mListItem;

        public ImageLoadedCallback(MessageListViewHolder listItem) {
            mListItem = listItem;
            mListItem.setImage(null, null);
            mMessageId = listItem.data.getMessageId();
        }

        public void reset(MessageListViewHolder listItem) {
            mMessageId = listItem.data.getMessageId();
        }

        public void onItemLoaded(ThumbnailManager.ImageLoaded imageLoaded, Throwable exception) {
            // Make sure we're still pointing to the same message. The list item could have // been recycled.
            MessageItem msgItem = mListItem.data;
            if (msgItem != null && msgItem.getMessageId() == mMessageId) {
                if (imageLoaded.mIsVideo) {
                    mListItem.setVideoThumbnail(null, imageLoaded.mBitmap);
                } else {
                    mListItem.setImage(null, imageLoaded.mBitmap);
                }
            }
        }
    }
}
