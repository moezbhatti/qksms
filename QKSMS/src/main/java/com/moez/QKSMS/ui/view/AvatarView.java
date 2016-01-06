package com.moez.QKSMS.ui.view;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.common.TypefaceManager;
import com.moez.QKSMS.enums.QKPreference;
import com.moez.QKSMS.common.utils.ImageUtils;
import com.moez.QKSMS.common.utils.Units;
import com.moez.QKSMS.ui.ThemeManager;

public class AvatarView extends ImageView implements View.OnClickListener {
    private final String TAG = "AvatarView";

    static final String[] PHONE_LOOKUP_PROJECTION = new String[]{
            ContactsContract.PhoneLookup._ID,
            ContactsContract.PhoneLookup.LOOKUP_KEY,
    };

    public static final String ME = "Me";

    static final int PHONE_ID_COLUMN_INDEX = 0;
    static final int PHONE_LOOKUP_STRING_COLUMN_INDEX = 1;
    static final private int TOKEN_PHONE_LOOKUP = 1;
    static final private int TOKEN_PHONE_LOOKUP_AND_TRIGGER = 3;
    static final private String EXTRA_URI_CONTENT = "uri_content";
    protected String[] mExcludeMimes = null;
    private Uri mContactUri;
    private String mContactPhone;
    private QueryHandler mQueryHandler;
    private Bundle mExtras = null;
    private String mInitial = "#";
    private Paint mPaint;
    private Drawable mDefaultDrawable;

    /**
     * When setImageDrawable is called with a drawable, we circle crop to size of this view and use
     * that instead. Keep the original in case our size changes, or in case the size wasn't ready.
     */
    private Drawable mOriginalDrawable;

    public AvatarView(Context context) {
        this(context, null);
    }

    public AvatarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AvatarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (!isInEditMode()) {
            mQueryHandler = new QueryHandler(context.getContentResolver());
            mPaint = new Paint();
            mPaint.setTextSize(Units.dpToPx(context, 32));
            mPaint.setTextAlign(Paint.Align.CENTER);
            mPaint.setAntiAlias(true);
            mPaint.setTypeface(TypefaceManager.obtainTypeface(getContext(), TypefaceManager.Typefaces.ROBOTO_LIGHT));

            mDefaultDrawable = ContextCompat.getDrawable(context, R.drawable.ic_person);

            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AvatarView, defStyle, 0);
            for (int i = 0; i < a.getIndexCount(); i++) {
                if (a.getIndex(i) == R.styleable.AvatarView_initialSize) {
                    mPaint.setTextSize(a.getDimensionPixelSize(a.getIndex(i), (int) mPaint.getTextSize()));
                }
            }
            a.recycle();

            setOnClickListener(this);

            LiveViewManager.registerView(QKPreference.THEME, this, key -> {
                mPaint.setColor(ThemeManager.getTextOnColorPrimary());
                mDefaultDrawable.setColorFilter(ThemeManager.getTextOnColorPrimary(), PorterDuff.Mode.SRC_ATOP);

                if (getBackground() == null) {
                    setBackgroundResource(R.drawable.circle);
                }
                getBackground().setColorFilter(ThemeManager.getColor(), PorterDuff.Mode.SRC_ATOP);
            });
        }
    }

    /**
     * True if a contact, an email address or a phone number has been assigned
     */
    private boolean isAssigned() {
        return mContactPhone != null;
    }

    /**
     * Assign the contact uri that this QuickContactBadge should be associated
     * with. Note that this is only used for displaying the QuickContact window and
     * won't bind the contact's photo for you. Call {@link #setImageDrawable(Drawable)} to set the
     * photo.
     *
     * @param contactUri Either a {@link android.provider.ContactsContract.Contacts#CONTENT_URI} or
     *                   {@link android.provider.ContactsContract.Contacts#CONTENT_LOOKUP_URI} style URI.
     */
    public void assignContactUri(Uri contactUri) {
        mContactUri = contactUri;
        mContactPhone = null;
    }

    public Uri getContactUri() {
        return mContactUri;
    }

    /**
     * Assign a contact based on a phone number. This should only be used when
     * the contact's URI is not available, as an extra query will have to be
     * performed to lookup the URI based on the phone number.
     *
     * @param phoneNumber The phone number of the contact.
     * @param lazyLookup  If this is true, the lookup query will not be performed
     *                    until this view is clicked.
     */
    public void assignContactFromPhone(String phoneNumber, boolean lazyLookup) {
        assignContactFromPhone(phoneNumber, lazyLookup, new Bundle());
    }

    /**
     * Assign a contact based on a phone number. This should only be used when
     * the contact's URI is not available, as an extra query will have to be
     * performed to lookup the URI based on the phone number.
     *
     * @param phoneNumber The phone number of the contact.
     * @param lazyLookup  If this is true, the lookup query will not be performed
     *                    until this view is clicked.
     * @param extras      A bundle of extras to populate the contact edit page with if the contact
     *                    is not found and the user chooses to add the phone number to an existing contact or
     *                    create a new contact. Uses the same string constants as those found in
     *                    {@link android.provider.ContactsContract.Intents.Insert}
     */
    public void assignContactFromPhone(String phoneNumber, boolean lazyLookup, Bundle extras) {
        mContactPhone = phoneNumber;
        mExtras = extras;
        if (!lazyLookup && mQueryHandler != null) {
            mQueryHandler.startQuery(TOKEN_PHONE_LOOKUP, null,
                    Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, mContactPhone),
                    PHONE_LOOKUP_PROJECTION, null, null, null);
        } else {
            mContactUri = null;
        }
    }

    public void setContactName(String name) {
        if (TextUtils.isEmpty(name) || name.equals(ME)) {
            mInitial = "";
            super.setImageDrawable(mDefaultDrawable);
        } else if (name.length() == 1) {
            mInitial = "" + name.toUpperCase();
            if (mOriginalDrawable == null) super.setImageDrawable(null);
        } else if (isPhoneNumberFormat(name)) {
            mInitial = "";
            super.setImageDrawable(mDefaultDrawable);
        } else {
            mInitial = "" + name.toUpperCase().charAt(0);
            if (mOriginalDrawable == null) super.setImageDrawable(null);
        }

        invalidate();
    }

    private boolean isPhoneNumberFormat(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }

        char c = name.charAt(0);
        return !name.contains("@") && (c == '+' || c == '(' || Character.isDigit(c));
    }

    @Override
    public void onClick(View v) {
        // If contact has been assigned, mExtras should no longer be null, but do a null check
        // anyway just in case assignContactFromPhone or Email was called with a null bundle or
        // wasn't assigned previously.
        final Bundle extras = (mExtras == null) ? new Bundle() : mExtras;

        if (mQueryHandler != null) {
            if (mContactPhone != null) {
                extras.putString(EXTRA_URI_CONTENT, mContactPhone);
                mQueryHandler.startQuery(TOKEN_PHONE_LOOKUP_AND_TRIGGER, extras,
                        Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, mContactPhone),
                        PHONE_LOOKUP_PROJECTION, null, null, null);
            } else if (mContactUri != null) {
                mQueryHandler.startQuery(TOKEN_PHONE_LOOKUP_AND_TRIGGER, extras, mContactUri,
                        PHONE_LOOKUP_PROJECTION, null, null, null);
            }
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(AvatarView.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(AvatarView.class.getName());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Set the image, in case we didn't previously have the actual measurements of the view
        setImageWhenReady();
    }

    /**
     * Circle crops the given drawable and sets it as the background. If the given drawable is null,
     * just generate a colored drawable and set that.
     *
     * @param drawable
     */
    @Override
    public void setImageDrawable(Drawable drawable) {
        mOriginalDrawable = drawable;
        setImageWhenReady();
    }

    private void setImageWhenReady() {
        if (isInEditMode()) {
            super.setImageDrawable(mOriginalDrawable);
            return;
        }

        if (getWidth() > 0 && getHeight() > 0) {
            // If our size is initialized correctly, then set up the drawable here.
            if (mOriginalDrawable != null) {
                // Circle crop the given bitmap.
                Bitmap orig = ((BitmapDrawable) mOriginalDrawable).getBitmap();
                Bitmap bitmap = orig.copy(Bitmap.Config.ARGB_8888, true);

                int w = getWidth();

                Bitmap roundBitmap = ImageUtils.getCircleBitmap(bitmap, w);
                super.setImageDrawable(new BitmapDrawable(getResources(), roundBitmap));
                getBackground().setColorFilter(0x00000000, PorterDuff.Mode.SRC_ATOP);
            } else {
                super.setImageDrawable(TextUtils.isEmpty(mInitial) ? mDefaultDrawable : null);
                getBackground().setColorFilter(ThemeManager.getColor(), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (getDrawable() == null && !isInEditMode()) {
            int xPos = (getWidth() / 2);
            int yPos = (int) ((getHeight() / 2) - ((mPaint.descent() + mPaint.ascent()) / 2));
            canvas.drawText("" + mInitial, xPos, yPos, mPaint);
        }
    }

    private class QueryHandler extends AsyncQueryHandler {

        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            Uri lookupUri = null;
            Uri createUri = null;
            boolean trigger = false;
            Bundle extras = (cookie != null) ? (Bundle) cookie : new Bundle();
            try {
                switch (token) {
                    case TOKEN_PHONE_LOOKUP_AND_TRIGGER:
                        trigger = true;
                        if (extras.getString(EXTRA_URI_CONTENT) != null) {
                            createUri = Uri.fromParts("tel", extras.getString(EXTRA_URI_CONTENT), null);
                        }

                        //$FALL-THROUGH$
                    case TOKEN_PHONE_LOOKUP: {
                        if (cursor != null && cursor.moveToFirst()) {
                            long contactId = cursor.getLong(PHONE_ID_COLUMN_INDEX);
                            String lookupKey = cursor.getString(PHONE_LOOKUP_STRING_COLUMN_INDEX);
                            lookupUri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey);
                        }
                        break;
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            mContactUri = lookupUri;

            if (trigger && lookupUri != null) {
                // Found contact, so trigger QuickContact
                ContactsContract.QuickContact.showQuickContact(getContext(), AvatarView.this, lookupUri,
                        ContactsContract.QuickContact.MODE_LARGE, mExcludeMimes);
            } else if (createUri != null) {
                // Prompt user to add this person to contacts
                final Intent intent = new Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT, createUri);
                if (extras != null) {
                    extras.remove(EXTRA_URI_CONTENT);
                    intent.putExtras(extras);
                }
                getContext().startActivity(intent);
            }
        }
    }
}
