package com.moez.QKSMS.ui.base;

import android.os.Bundle;
import com.moez.QKSMS.ui.ContentFragment;

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
public abstract class QKContentFragment extends QKFragment implements ContentFragment {

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

    /**
     * Called when the content is being opened with an animation.
     */
    public void onContentOpening() {
    }

    /**
     * Called when the content has been opened, i.e. a opening animation has finished or it had
     * been opened immediately.
     */
    public void onContentOpened() {
    }

    /**
     * Called when the content is being closed with an animation.
     */
    public void onContentClosing() {
    }

    /**
     * Called when the content has been closed, i.e. a closing animation has finished or it had
     * been closed immediately.
     */
    public void onContentClosed() {
    }
}
