package com.moez.QKSMS.ui;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;

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

    /**
     * Called multiple times as the content is opening or closing. Allows it to receive updates
     * of the position. Currently used to animate to the new theme color
     *
     * @param percentOpen
     */
    void onMenuChanging(float percentOpen);

    /**
     * Allows the MainActivity to delegate setting of the Toolbar title and menu
     */
    void inflateToolbar(Menu menu, MenuInflater inflater, Context context);
}
