package com.moez.QKSMS.ui.view;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.enums.QKPreference;
import com.moez.QKSMS.data.Contact;
import com.moez.QKSMS.data.ContactHelper;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.settings.SettingsFragment;

public class StarredContactsView extends LinearLayout implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {
    private final String TAG = "StarredContactsView";

    private QKActivity mContext;
    private SharedPreferences mPrefs;
    private Cursor mCursor;
    private LinearLayout mFavoritesBackground;
    private QKTextView mTitle;
    private LinearLayout mFavorites;
    private AutoCompleteContactView mRecipients;
    private ComposeView mComposeView;
    private View mToggle;
    private ImageView mIndicator;


    public StarredContactsView(Context context) {
        super(context);
        init(context);
    }

    public StarredContactsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = (QKActivity) context;
        mPrefs = mContext.getPrefs();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mFavoritesBackground = (LinearLayout) findViewById(R.id.starred_contacts);
        mFavoritesBackground.setBackgroundColor(ThemeManager.getBackgroundColor());

        mTitle = (QKTextView) findViewById(R.id.title);

        mFavorites = (LinearLayout) findViewById(R.id.favorites);

        mToggle = findViewById(R.id.toggle);
        mToggle.setOnClickListener(this);

        mIndicator = (ImageView) findViewById(R.id.indicator);

        if (mPrefs.getBoolean(SettingsFragment.COMPOSE_FAVORITES, true)) {
            expand();
        } else {
            collapse();
        }

        LiveViewManager.registerView(QKPreference.BACKGROUND, this, key -> {
            mIndicator.setColorFilter(ThemeManager.getTextOnBackgroundSecondary(), PorterDuff.Mode.SRC_ATOP);
            mFavoritesBackground.setBackgroundColor(ThemeManager.getBackgroundColor());
        });
    }

    public void setComposeScreenViews(AutoCompleteContactView recipients, ComposeView composeView) {
        mRecipients = recipients;
        mComposeView = composeView;
        mContext.getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mContext, ContactHelper.CONTACTS_URI, ContactHelper.Favorites.PROJECTION,
                ContactHelper.Favorites.SELECTION, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;
        mFavorites.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(mContext);

        if (data.moveToFirst()) {
            do {
                // only add them to the favorites list if they have a phone number
                if (mCursor.getInt(ContactHelper.Favorites.HAS_PHONE_NUMBER) > 0) {

                    final String photoUri = mCursor.getString(ContactHelper.Favorites.PHOTO_THUMBNAIL_URI);
                    final Contact contact = Contact.get(ContactHelper.getPhoneNumber(
                            mContext, mCursor.getString(ContactHelper.Favorites.ID)), true);

                    final View.OnClickListener onClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            collapse();
                            mRecipients.submitItem(contact.getName(), contact.getNumber(),
                                    photoUri == null ? null : Uri.parse(photoUri));
                            mComposeView.requestReplyTextFocus();
                        }
                    };

                    View view = inflater.inflate(R.layout.view_favorite_contact, null);
                    view.setOnClickListener(onClickListener);

                    AvatarView avatar = (AvatarView) view.findViewById(R.id.avatar);
                    avatar.setOnClickListener(onClickListener);
                    avatar.setImageDrawable(contact.getAvatar(mContext, null));
                    avatar.setContactName(contact.getName());

                    QKTextView name = (QKTextView) view.findViewById(R.id.name);
                    name.setOnClickListener(onClickListener);
                    name.setText(contact.getName());

                    mFavorites.addView(view);
                }
            } while (data.moveToNext());
        }

        if (mFavorites.getChildCount() > 0) {
            mFavoritesBackground.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
    }

    public void toggle() {
        if (mFavorites.getVisibility() == View.VISIBLE) {
            collapse();
        } else {
            expand();
        }
    }

    public void expand() {
        mTitle.setTextColor(ThemeManager.getTextOnBackgroundPrimary());
        mFavorites.setVisibility(View.VISIBLE);
        mIndicator.setRotation(0f);
    }

    public void collapse() {
        mTitle.setTextColor(ThemeManager.getTextOnBackgroundSecondary());
        mFavorites.setVisibility(View.GONE);
        mIndicator.setRotation(90f);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toggle:
                toggle();
                break;
        }
    }
}
