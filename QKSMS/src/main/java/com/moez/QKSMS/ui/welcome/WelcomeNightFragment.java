package com.moez.QKSMS.ui.welcome;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import com.moez.QKSMS.R;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.settings.SettingsFragment;
import com.moez.QKSMS.ui.view.QKTextView;
import com.moez.QKSMS.ui.view.RobotoTextView;

public class WelcomeNightFragment extends BaseWelcomeFragment implements BaseWelcomeFragment.WelcomeScrollListener, View.OnClickListener {
    private final String TAG = "WelcomeNightFragment";

    private ArgbEvaluator mArgbEvaluator = new ArgbEvaluator();
    private RobotoTextView mNightTitle;
    private RobotoTextView mNightDescription;
    private QKTextView mNightHint;
    private String mNightShyamalan;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome_night, container, false);

        mNightTitle = (RobotoTextView) view.findViewById(R.id.welcome_night_title);
        mNightDescription = (RobotoTextView) view.findViewById(R.id.welcome_night_description);
        mNightHint = (QKTextView) view.findViewById(R.id.welcome_night_hint);
        mNightHint.setOnClickListener(this);

        mContext.setFinished();

        return view;
    }

    @Override
    public void onScrollOffsetChanged(WelcomeActivity activity, float offset) {
        int colorBackground = (Integer) mArgbEvaluator.evaluate(Math.abs(offset), ThemeManager.getBackgroundColor(), ThemeManager.getColor());
        int colorAccent = (Integer) mArgbEvaluator.evaluate(1 - Math.abs(offset), 0xFFFFFFFF, ThemeManager.getColor());

        activity.setColorBackground(colorBackground);
        activity.tintIndicators(colorAccent);

        if (mNightTitle != null) {
            mNightTitle.setTextColor(colorAccent);
        }

        if (mNightDescription != null) {
            mNightDescription.setTextColor(colorAccent);
        }

        if (mNightHint != null) {
            mNightHint.setTextColor(colorBackground);
            mNightHint.getBackground().setColorFilter(colorAccent, PorterDuff.Mode.SRC_ATOP);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.welcome_night_hint) {
            boolean night = ThemeManager.getTheme() == ThemeManager.Theme.LIGHT;

            int backgroundColor = mContext.getResources().getColor(night ? R.color.grey_light_mega_ultra : R.color.grey_material);
            int newBackgroundColor = mContext.getResources().getColor(night ? R.color.grey_material : R.color.grey_light_mega_ultra);
            ThemeManager.setTheme(night ? ThemeManager.Theme.DARK : ThemeManager.Theme.LIGHT);

            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), backgroundColor, newBackgroundColor);
            colorAnimation.setDuration(ThemeManager.TRANSITION_LENGTH);
            colorAnimation.setInterpolator(new DecelerateInterpolator());
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int color = (Integer) animation.getAnimatedValue();
                    mContext.setColorBackground(color);
                    mNightHint.setTextColor(color);

                }
            });
            colorAnimation.start();

            PreferenceManager.getDefaultSharedPreferences(mContext)
                    .edit()
                    .putString(SettingsFragment.BACKGROUND, night ? ThemeManager.Theme.PREF_GREY : ThemeManager.Theme.PREF_OFFWHITE)
                    .commit();
        }
    }
}
