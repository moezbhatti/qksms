package com.moez.QKSMS.ui;

/**
 * This interface is for fragments that must extend something other than a vanilla Fragment. It
 * allows the fragment to get content animation callbacks without the developer having to
 * rewrite the base class to extend BaseContentFragment.
 */
public interface ContentFragment {
    /**
     * Called when the content is being opened with an animation.
     */
    void onContentOpening();

    /**
     * Called when the content has been opened, i.e. a opening animation has finished or it had
     * been opened immediately.
     */
    void onContentOpened();

    /**
     * Called when the content is being closed with an animation.
     */
    void onContentClosing();

    /**
     * Called when the content has been closed, i.e. a closing animation has finished or it had
     * been closed immediately.
     */
    void onContentClosed();
}
