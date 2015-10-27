package com.moez.QKSMS.common;

import com.moez.QKSMS.R;

import org.ligi.snackengage.conditions.AfterNumberOfOpportunities;
import org.ligi.snackengage.conditions.NeverAgainWhenClickedOnce;
import org.ligi.snackengage.snacks.RateSnack;

/* https://github.com/ligi/SnackEngage
*  "This shows a snackbar after MainActivity was created *10* times and never again when once clicked on Rate" */

public class QKRateSnack extends RateSnack {
    public QKRateSnack() {
        withConditions(new NeverAgainWhenClickedOnce(), new AfterNumberOfOpportunities(10));
    }

    @Override
    public String getText() {
        return getString(R.string.rate_title);
    }

    @Override
    public String getActionText() {
        return getString(R.string.rate_action);
    }
}