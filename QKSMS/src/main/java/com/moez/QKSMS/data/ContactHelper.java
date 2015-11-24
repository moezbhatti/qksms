package com.moez.QKSMS.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import com.moez.QKSMS.common.TypefaceManager;
import com.moez.QKSMS.common.utils.Units;
import com.moez.QKSMS.ui.ThemeManager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.provider.ContactsContract.CommonDataKinds.Phone;

public class ContactHelper {
    private static final String TAG = "ContactHelper";

    public static Uri CONTACTS_URI = ContactsContract.Contacts.CONTENT_URI;

    public static class Favorites {

        public static String SELECTION = ContactsContract.Contacts.STARRED + "='1'";

        public static String[] PROJECTION = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.STARRED,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI};

        public static final int ID = 0;
        public static final int DISPLAY_NAME = 1;
        public static final int STARRED = 2;
        public static final int HAS_PHONE_NUMBER = 3;
        public static final int PHOTO_THUMBNAIL_URI = 4;
    }

    public static String getTag()
    {
        return  TAG;
    }

    public static String getName(Context context, String address) {
        return Contact.getName(context, address);
    }

    /**
     * Get the phone number of a contact given their id
     * TODO: The logic for picking the best phone number could be better
     */
    public static String getPhoneNumber(Context context, String contactId) {

        String number = "";

        Cursor cursor;
        try {
            cursor = context.getContentResolver().query(Phone.CONTENT_URI, null,
                    Phone.CONTACT_ID + " = " + contactId, null, null);

            while (cursor.moveToNext()) {
                number = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
                int type = cursor.getInt(cursor.getColumnIndex(Phone.TYPE));

                switch (type) {
                    case Phone.TYPE_MOBILE:
                        // Return right away if it's a mobile number
                        cursor.close();
                        return number;
                }
            }

            cursor.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        // Return whatever number we found last, since we don't know which is best
        return number;
    }

    public static boolean validateEmail(String email) {
        Pattern pattern;
        Matcher matcher;
        String EMAIL_PATTERN = "\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static Uri getUri(String address) {
        return Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
    }

    public static long getId(Context context, String address) {
        return Contact.getId(context, address);
    }

    public static Uri getPhotoUri(Context context, Uri contactUri) {
        Cursor cursor = context.getContentResolver().query(contactUri, Favorites.PROJECTION, null, null, null);

        String photoUriString = null;
        if (cursor.moveToFirst()) {
            photoUriString = cursor.getString(Favorites.PHOTO_THUMBNAIL_URI);
        }

        return photoUriString == null ? null : Uri.parse(photoUriString);
    }

    public static Drawable getDrawable(Context context, long id) {
        return new BitmapDrawable(context.getResources(), getBitmap(context, id));
    }

    public static Bitmap getOwnerPhoto(Context context) {

        final String[] SELF_PROJECTION = new String[]{Phone._ID};
        Cursor cursor = context.getContentResolver().query(ContactsContract.Profile.CONTENT_URI, SELF_PROJECTION, null, null, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) return getBitmap(context, cursor.getLong(0));

        return null;
    }

    public static Bitmap getBitmap(Context context, long id) {
        return Contact.getBitmap(context, id);
    }
    
    public static Bitmap blankContact(Context context, String name) {
        String text = name == null || PhoneNumberUtils.isWellFormedSmsAddress(PhoneNumberUtils.stripSeparators(name)) || name.length() == 0 ? "#" : "" + name.toUpperCase().charAt(0);

        int length = Units.dpToPx(context, 64);

        Bitmap bitmap = Bitmap.createBitmap(length, length, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(ThemeManager.getColor());

        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTypeface(TypefaceManager.obtainTypeface(context, TypefaceManager.Typefaces.ROBOTO_LIGHT));
        paint.setTextSize(length / 2);

        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width()) / 2;
        int y = (bitmap.getHeight() + bounds.height()) / 2;

        canvas.drawText(text, x, y, paint);

        return bitmap;
    }
}
