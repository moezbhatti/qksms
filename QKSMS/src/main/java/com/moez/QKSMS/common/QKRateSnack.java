package com.moez.QKSMS.common;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.moez.QKSMS.R;

import org.ligi.snackengage.SnackContext;
import org.ligi.snackengage.conditions.AfterNumberOfOpportunities;
import org.ligi.snackengage.conditions.NeverAgainWhenClickedOnce;
import org.ligi.snackengage.conditions.SnackCondition;
import org.ligi.snackengage.snacks.RateSnack;
import org.ligi.snackengage.snacks.Snack;

/* https://github.com/ligi/SnackEngage
*  "This shows a snackbar after MainActivity was created *10* times and never again when once clicked on Rate" */

public class QKRateSnack extends RateSnack {
    private final String SNACK_COUNT = "snack_count";

    public QKRateSnack() {
        withConditions(new NeverAgainWhenClickedOnce(), new AfterNumberOfOpportunities(10), new OnlyThreeTimes());
    }

    @Override
    public String getText() {
        return getString(R.string.rate_title);
    }

    @Override
    public String getActionText() {
        return getString(R.string.rate_action);
    }

    @Override
    public boolean opportunity(SnackContext snackContext) {
        boolean result = super.opportunity(snackContext);

        if (result) {
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(snackContext.getAndroidContext());
            mPrefs.edit().putInt(SNACK_COUNT, mPrefs.getInt(SNACK_COUNT, 0) + 1).apply();
        }

        return result;
    }

    private class OnlyThreeTimes implements SnackCondition {

        @Override
        public boolean isAppropriate(SnackContext context, Snack snack) {
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context.getAndroidContext());
            return mPrefs.getInt(SNACK_COUNT, 0) < 3;
        }
    }
}