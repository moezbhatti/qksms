package com.moez.QKSMS.ui.base;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import com.moez.QKSMS.interfaces.LiveView;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.ui.MainActivity;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.settings.SettingsFragment;

/**
 * Base class for Fragments. QKSMS uses the recycle pattern as an optimization, and our fragment
 * system has been built to help accommodate this.
 * <p/>
 * Functions:
 * - Exposes a new API around fragment arguments update. updateArguments(Bundle) can be used to
 * reconfigure a recycled fragment, and onNewArguments() will be called whenever
 * updateArguments(Bundle) is called.
 * <p/>
 * - Gives callbacks for the content animations.
 * <p/>
 * - Manages the current state of the fragment in terms of the animation, i.e. opening, fully
 * opened, etc.
 */
public class QKContentFragment extends QKFragment implements LiveView {

    private static final boolean LOCAL_LOGV = false;

    /**
     * Holds the state of the content in the menu.
     */
    private int mContentAnimationState = STATE_OPENED;

    public static final int STATE_OPENING = 0;
    public static final int STATE_OPENED = 1;
    public static final int STATE_CLOSING = 2;
    public static final int STATE_CLOSED = 3;

    protected MainActivity mContext;

    /**
     * It's not strictly necessary, but subclasses should call through to super() in their
     * constructor.
     */
    public QKContentFragment() {
        setArguments(new Bundle());
    }

    /**
     * Called whenever setArguments is called, so any setup that uses the new configurations can be
     * implemented in this method.
     */
    public void onNewArguments() {
    }

    public void updateArguments(Bundle args) {
        // Ensure that args is not null.
        args = args == null ? new Bundle() : args;

        // The fragment's arguments can only be set before onAttach is called.
        if (getActivity() == null) {
            // If the fragment hasn't been attached to its activity yet, call the super method to
            // actually set this Bundle as the arguments.
            super.setArguments(args);

        } else {
            // Otherwise, replace all the values in the old args with the values in the new args.
            Bundle oldArgs = getArguments() == null ? new Bundle() : getArguments();
            oldArgs.clear();
            oldArgs.putAll(args);
        }

        // Notify that the arguments have been changed.
        onNewArguments();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (MainActivity) getActivity();
        LiveViewManager.registerPreference(this, SettingsFragment.BACKGROUND);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refresh();
    }

    /**
     * @return The current state of the animation of the content.
     */
    public int getContentAnimationState() {
        return mContentAnimationState;
    }

    /**
     * Call on a BaseContentFragment to notify it that it is opening.
     */
    public final void performOnContentOpening() {
        mContentAnimationState = STATE_OPENING;
        onContentOpening();
    }

    /**
     * Call on a BaseContentFragment to notify it that it has been opened fully, i.e. an opening
     * animation has finished or the Fragment has been opened immediately.
     */
    public final void performOnContentOpened() {
        mContentAnimationState = STATE_OPENED;
        onContentOpened();
    }

    /**
     * Call on a BaseContentFragment to notify it that it is closing.
     */
    public final void performOnContentClosing() {
        mContentAnimationState = STATE_CLOSING;
        onContentClosing();
    }

    /**
     * Call on a BaseContentFragment to notify it that it has been closed fully, i.e. a closing
     * animation has finished or the Fragment has been closed immediately.
     */
    public final void performOnContentClosed() {
        mContentAnimationState = STATE_CLOSED;
        onContentClosed();
    }

    /**
     * Called when the content is being opened with an animation.
     */
    protected void onContentOpening() {
    }

    /**
     * Called when the content has been opened, i.e. a opening animation has finished or it had
     * been opened immediately.
     */
    protected void onContentOpened() {
    }

    /**
     * Called when the content is being closed with an animation.
     */
    protected void onContentClosing() {
    }

    /**
     * Called when the content has been closed, i.e. a closing animation has finished or it had
     * been closed immediately.
     */
    protected void onContentClosed() {
    }

    @Override
    public void refresh() {
        View view = getView();
        if (view != null) {
            view.setBackgroundColor(ThemeManager.getBackgroundColor());
        }
    }

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

    // The following static methods will notify a Fragment about their content animations if
    // appropriate, or do nothing otherwise.
    public static void notifyOnContentOpening(Fragment fragment) {
        if (fragment instanceof QKContentFragment) {
            ((QKContentFragment) fragment).performOnContentOpening();
        } else if (fragment instanceof ContentFragment) {
            ((ContentFragment) fragment).onContentOpening();
        }
    }

    public static void notifyOnContentOpened(Fragment fragment) {
        if (fragment instanceof QKContentFragment) {
            ((QKContentFragment) fragment).performOnContentOpened();
        } else if (fragment instanceof ContentFragment) {
            ((ContentFragment) fragment).onContentOpened();
        }
    }

    public static void notifyOnContentClosing(Fragment fragment) {
        if (fragment instanceof QKContentFragment) {
            ((QKContentFragment) fragment).performOnContentClosing();
        } else if (fragment instanceof ContentFragment) {
            ((ContentFragment) fragment).onContentClosing();
        }
    }

    public static void notifyOnContentClosed(Fragment fragment) {
        if (fragment instanceof QKContentFragment) {
            ((QKContentFragment) fragment).performOnContentClosed();
        } else if (fragment instanceof ContentFragment) {
            ((ContentFragment) fragment).onContentClosed();
        }
    }
}
