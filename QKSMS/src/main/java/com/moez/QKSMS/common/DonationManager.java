package com.moez.QKSMS.common;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.moez.QKSMS.R;
import com.moez.QKSMS.enums.QKPreference;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.dialog.QKDialog;

import java.util.Random;

/**
 * Manages donations
 */
public class DonationManager implements BillingProcessor.IBillingHandler {
    public static final String TAG = "DonationManager";
    public static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAprSfnH+TSMKWakRyf9dTiK5Z71MCvRthkvDCB62Tnyv2JnbMdeI6PePgtEWDEBPaPsNfmus+E9HNnPOvqZAH01TCKW0YOW6bIv7tVj+xHrAyaZMXOMKZMQx9Wme1oPU3IG686TkHGkbIdEJrHcYLU7JpwCqm0DgKFHC+/Ehd40uQAEZvXlESEJtft0N4anSifunjCpTCyDA56sXf2zNhYIhM5MlhjHw4gPq36RCml0S+N8xQL1hyh1doIAo82vx3Bh18KSbQMpk4t189Yjh9DyvOj8Se3iTtXSwaK1vXrZiWd9B3C/CXzIicFKLYKP1Ejigd0YInaC8mxIsrKddLVwIDAQAB";

    public static String SKU_DONATE_1 = "donate_1";
    public static String SKU_DONATE_5 = "donate_5";
    public static String SKU_DONATE_10 = "donate_10";

    private static DonationManager sInstance = null;

    private BillingProcessor mBillingProcessor;
    private QKActivity mContext;

    public static DonationManager getInstance(QKActivity context) {
        if (sInstance == null) {
            sInstance = new DonationManager(context);
            sInstance.mBillingProcessor = new BillingProcessor(context, PUBLIC_KEY, sInstance);
        } else {
            sInstance.mContext = context; // Update the context, in case the previous context was destroyed
        }

        return sInstance;
    }

    public static void clearInstance() {
        if (null != sInstance) {
            sInstance.destroy();
        }
        sInstance = null;
    }

    private DonationManager(QKActivity context) {
        mContext = context;
    }

    public void destroy() {
        if (mBillingProcessor != null) {
            mBillingProcessor.release();
        }
    }


    // User clicked the "Upgrade to Premium" button.
    public void onDonateButtonClicked(String sku) {
        if (mBillingProcessor.isInitialized()) {
            mBillingProcessor.purchase(mContext, sku);
        } else {
            Toast.makeText(mContext, R.string.toast_billing_not_available, Toast.LENGTH_LONG).show();
        }
    }

    public void showDonateDialog() {

        View.OnClickListener clickListener = view -> {
            switch (view.getId()) {
                case R.id.donate_1:
                    onDonateButtonClicked(SKU_DONATE_1);
                    break;
                case R.id.donate_5:
                    onDonateButtonClicked(SKU_DONATE_5);
                    break;
                case R.id.donate_10:
                    onDonateButtonClicked(SKU_DONATE_10);
                    break;
                case R.id.donate_paypal:
                    donatePaypal();
                    break;
            }
        };

        View view = mContext.getLayoutInflater().inflate(R.layout.dialog_donate, null);
        view.findViewById(R.id.donate_1).setOnClickListener(clickListener);
        view.findViewById(R.id.donate_5).setOnClickListener(clickListener);
        view.findViewById(R.id.donate_10).setOnClickListener(clickListener);
        view.findViewById(R.id.donate_paypal).setOnClickListener(clickListener);

        LiveViewManager.registerView(QKPreference.BACKGROUND, this, key -> {
            view.findViewById(R.id.donate_1).setBackgroundDrawable(ThemeManager.getRippleBackground());
            view.findViewById(R.id.donate_5).setBackgroundDrawable(ThemeManager.getRippleBackground());
            view.findViewById(R.id.donate_10).setBackgroundDrawable(ThemeManager.getRippleBackground());
            view.findViewById(R.id.donate_paypal).setBackgroundDrawable(ThemeManager.getRippleBackground());
        });

        QKDialog dialog = new QKDialog()
                .setContext(mContext)
                .setCustomView(view);

        dialog.show();
    }

    public void donatePaypal() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/QKSMSDonation"));
        mContext.startActivity(browserIntent);
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        String[] thanks = {
                mContext.getString(R.string.thanks_1),
                mContext.getString(R.string.thanks_2),
                mContext.getString(R.string.thanks_3),
                mContext.getString(R.string.thanks_4)};

        Toast.makeText(mContext, thanks[new Random().nextInt(thanks.length)], Toast.LENGTH_LONG).show();
        mBillingProcessor.consumePurchase(productId);
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_LONG).show();
        Log.w(TAG, "Error purchasing: " + error.getMessage());
    }

    @Override
    public void onBillingInitialized() {

    }
}
