package com.moez.QKSMS.common;

import org.ligi.snackengage.conditions.AfterNumberOfOpportunities;
import org.ligi.snackengage.conditions.NeverAgainWhenClickedOnce;
import org.ligi.snackengage.snacks.RateSnack;

/* https://github.com/ligi/SnackEngage
*  "This shows a snackbar after app was started *20* times and never again when once clicked on Rate" */

public class QksmsRateSnack extends RateSnack {
    public QksmsRateSnack() {
        this.withConditions(new NeverAgainWhenClickedOnce(), new AfterNumberOfOpportunities(20));
    }
}