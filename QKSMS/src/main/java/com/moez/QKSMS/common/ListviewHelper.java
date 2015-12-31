package com.moez.QKSMS.common;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ListView;
import com.moez.QKSMS.R;
import com.moez.QKSMS.ui.ThemeManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ListviewHelper {

    public static void applyCustomScrollbar(Context context, ListView listView) {
        if (context != null && listView != null) {
            try {
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.scrollbar);
                drawable.setColorFilter(Color.argb(64,
                        Color.red(ThemeManager.getTextOnBackgroundSecondary()),
                        Color.green(ThemeManager.getTextOnBackgroundSecondary()),
                        Color.blue(ThemeManager.getTextOnBackgroundSecondary())),
                        PorterDuff.Mode.SRC_ATOP);

                Field mScrollCacheField = View.class.getDeclaredField("mScrollCache");
                mScrollCacheField.setAccessible(true);
                Object mScrollCache = mScrollCacheField.get(listView);
                Field scrollBarField = mScrollCache.getClass().getDeclaredField("scrollBar");
                scrollBarField.setAccessible(true);
                Object scrollBar = scrollBarField.get(mScrollCache);
                Method method = scrollBar.getClass().getDeclaredMethod("setVerticalThumbDrawable", Drawable.class);
                method.setAccessible(true);
                method.invoke(scrollBar, drawable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
