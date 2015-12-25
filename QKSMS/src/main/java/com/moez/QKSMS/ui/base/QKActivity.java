package com.moez.QKSMS.ui.base;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.moez.QKSMS.QKSMSApp;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.preferences.QKPreference;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.view.QKTextView;

import java.util.ArrayList;

public abstract class QKActivity extends ActionBarActivity {
    private final String TAG = "QKActivity";

    private Toolbar mToolbar;
    private QKTextView mTitle;
    private ImageView mOverflowButton;
    private Menu mMenu;
    private ProgressDialog mProgressDialog;

    protected Resources mRes;
    protected SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRes = getResources();
        getPrefs(); // set the preferences if they haven't been set. this method takes care of that logic for us

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
    }

    /**
     * Reloads the toolbar and it's view references.
     * <p/>
     * This is called every time the content view of the activity is set, since the
     * toolbar is now a part of the activity layout.
     * <p/>
     * TODO: If someone ever wants to manage the Toolbar dynamically instead of keeping it in their
     * TODO  layout file, we can add an alternate way of setting the toolbar programmatically.
     */
    private void reloadToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        if (mToolbar == null) {
            throw new RuntimeException("Toolbar not found in BaseActivity layout.");
        } else {
            mToolbar.setPopupTheme(R.style.PopupTheme);
            mTitle = (QKTextView) mToolbar.findViewById(R.id.toolbar_title);
            setSupportActionBar(mToolbar);
        }

        ThemeManager.getInstance().loadThemeProperties(this);
    }

    protected void showBackButton(boolean show) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(show);
    }

    public void showProgressDialog() {
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        mProgressDialog.hide();
    }

    public SharedPreferences getPrefs() {
        if (mPrefs == null) {
            mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        }
        return mPrefs;
    }

    public void colorMenuIcons(Menu menu, int color) {

        // Toolbar navigation icon
        Drawable navigationIcon = getToolbar().getNavigationIcon();
        if (navigationIcon != null) {
            navigationIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            getToolbar().setNavigationIcon(navigationIcon);
        }

        // Overflow icon
        colorOverflowButtonWhenReady(color);

        // Settings expansion
        ArrayList<View> views = new ArrayList<>();
        View decor = getWindow().getDecorView();
        decor.findViewsWithText(views, getString(R.string.menu_show_all_prefs), View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
        decor.findViewsWithText(views, getString(R.string.menu_show_fewer_prefs), View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
        android.widget.TextView connected = !views.isEmpty() ? (android.widget.TextView) views.get(0) : null;
        if (connected != null) {
            connected.setTextColor(color);
        }

        // Other icons
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            Drawable newIcon = menuItem.getIcon();
            if (newIcon != null) {
                newIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                menuItem.setIcon(newIcon);
            }
        }
    }

    private void colorOverflowButtonWhenReady(final int color) {
        if (mOverflowButton != null) {
            // We already have the overflow button, so just color it.
            Drawable icon = mOverflowButton.getDrawable();
            icon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            // Have to clear the image drawable first or else it won't take effect
            mOverflowButton.setImageDrawable(null);
            mOverflowButton.setImageDrawable(icon);

        } else {
            // Otherwise, find the overflow button by searching for the content description.
            final String overflowDesc = getString(R.string.abc_action_menu_overflow_description);
            final ViewGroup decor = (ViewGroup) getWindow().getDecorView();
            decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    decor.getViewTreeObserver().removeOnPreDrawListener(this);

                    final ArrayList<View> views = new ArrayList<>();
                    decor.findViewsWithText(views, overflowDesc,
                            View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);

                    if (views.isEmpty()) {
                        Log.w(TAG, "no views");
                    } else {
                        if (views.get(0) instanceof ImageView) {
                            mOverflowButton = (ImageView) views.get(0);
                            colorOverflowButtonWhenReady(color);
                        } else {
                            Log.w(TAG, "overflow button isn't an imageview");
                        }
                    }
                    return false;
                }
            });
        }
    }

    public Menu getMenu() {
        return mMenu;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Save a reference to the menu so that we can quickly access menu icons later.
        mMenu = menu;
        colorMenuIcons(mMenu, ThemeManager.getInstance().getTextOnColorPrimary());
        return true;
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        reloadToolbar();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        reloadToolbar();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        reloadToolbar();
    }

    /**
     * Sets the title of the activity, displayed on the toolbar
     * <p/>
     * Make sure this is only called AFTER setContentView, or else the Toolbar
     * is likely not initialized yet and this method will do nothing
     *
     * @param title title of activity
     */
    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);

        if (mTitle != null) {
            mTitle.setText(title);
        }
    }

    /**
     * Returns the Toolbar for this activity.
     *
     * @return
     */
    public Toolbar getToolbar() {
        return mToolbar;
    }

    public boolean isScreenOn() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            return powerManager.isInteractive();
        } else {
            return powerManager.isScreenOn();
        }
    }

    public void makeToast(@StringRes int message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public RequestQueue getRequestQueue() {
        return ((QKSMSApp) getApplication()).getRequestQueue();
    }

    public boolean getBoolean(QKPreference preference) {
        return getPrefs().getBoolean(preference.getKey(), (boolean) preference.getDefaultValue());
    }

    public void setBoolean(QKPreference preference, boolean newValue) {
        getPrefs().edit().putBoolean(preference.getKey(), newValue).apply();
    }

    public int getInt(QKPreference preference) {
        return getPrefs().getInt(preference.getKey(), (int) preference.getDefaultValue());
    }

    public void setInt(QKPreference preference, int newValue) {
        getPrefs().edit().putInt(preference.getKey(), newValue).apply();
    }

    public String getString(QKPreference preference) {
        return getPrefs().getString(preference.getKey(), (String) preference.getDefaultValue());
    }

    public void setString(QKPreference preference, String newValue) {
        getPrefs().edit().putString(preference.getKey(), newValue).apply();
    }
}
