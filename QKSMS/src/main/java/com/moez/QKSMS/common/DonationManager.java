package com.moez.QKSMS.common;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.moez.QKSMS.R;
import com.moez.QKSMS.external.iab.IabHelper;
import com.moez.QKSMS.external.iab.IabResult;
import com.moez.QKSMS.external.iab.Inventory;
import com.moez.QKSMS.external.iab.Purchase;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.dialog.QKDialog;

import java.util.Random;

/**
 * Manages donations
 */
public class DonationManager {
    public static final String TAG = "DonationManager";
    public static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAprSfnH+TSMKWakRyf9dTiK5Z71MCvRthkvDCB62Tnyv2JnbMdeI6PePgtEWDEBPaPsNfmus+E9HNnPOvqZAH01TCKW0YOW6bIv7tVj+xHrAyaZMXOMKZMQx9Wme1oPU3IG686TkHGkbIdEJrHcYLU7JpwCqm0DgKFHC+/Ehd40uQAEZvXlESEJtft0N4anSifunjCpTCyDA56sXf2zNhYIhM5MlhjHw4gPq36RCml0S+N8xQL1hyh1doIAo82vx3Bh18KSbQMpk4t189Yjh9DyvOj8Se3iTtXSwaK1vXrZiWd9B3C/CXzIicFKLYKP1Ejigd0YInaC8mxIsrKddLVwIDAQAB";

    public static String SKU_DONATE_1 = "donate_1";
    public static String SKU_DONATE_5 = "donate_5";
    public static String SKU_DONATE_10 = "donate_10";

    private static DonationManager sInstance = null;

    private IabHelper mHelper;
    private boolean mBillingServiceReady = false;
    private QKActivity mContext;
    private Resources mRes;

    public static DonationManager getInstance(QKActivity context) {
        if (sInstance == null) {
            sInstance = new DonationManager(context);
        }

        return sInstance;
    }

    private DonationManager(QKActivity context) {
        mContext = context;
        mRes = mContext.getResources();

        // Create the helper, passing it our context and the public key to verify signatures with
        mHelper = new IabHelper(mContext, PUBLIC_KEY);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {

            @Override
            public void onIabSetupFinished(IabResult result) {
                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) {
                    return;
                }

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Log.w(TAG, "Problem setting up in-app billing: " + result.getMessage());
                    return;
                }

                // IAB is fully set up.
                mBillingServiceReady = true;

                // IAB is fully set up. Now, let's getConversation an inventory of stuff we own.
                mHelper.queryInventoryAsync(iabInventoryListener());
            }
        });
    }

    public void destroy() {
        if (mHelper != null) {
            mHelper.dispose();
        }
    }

    // Callback for when a purchase is finished
    private final IabHelper.OnIabPurchaseFinishedListener sPurchaseFinishedListener =
            new IabHelper.OnIabPurchaseFinishedListener() {
                @Override
                public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                    if (mHelper == null || mContext == null) {
                        return;
                    }

                    // Don't complain if cancelling
                    if (result.getResponse() == IabHelper.IABHELPER_USER_CANCELLED) {
                        return;
                    }

                    if (!result.isSuccess()) {
                        Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show();
                        Log.w(TAG, "Error purchasing: " + result.getMessage());
                        return;
                    }

                    Log.d(TAG, "Purchase complete: " + purchase.getSku());
                }
            };


    /**
     * Listener that's called when we finish querying the items and subscriptions we own
     */
    private IabHelper.QueryInventoryFinishedListener iabInventoryListener() {
        return new IabHelper.QueryInventoryFinishedListener() {
            @Override
            public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) {
                    return;
                }

                // Something went wrong
                if (!result.isSuccess()) {
                    return;
                }

                IabHelper.OnConsumeFinishedListener onConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
                    @Override
                    public void onConsumeFinished(Purchase purchase, IabResult result) {
                        // if we were disposed of in the meantime, quit.
                        if (mHelper == null) {
                            return;
                        }

                        if (result.isSuccess()) {
                            String[] thanks = {mRes.getString(R.string.thanks_1), mRes.getString(R.string.thanks_2), mRes.getString(R.string.thanks_3), mRes.getString(R.string.thanks_4)};
                            Toast.makeText(mContext, thanks[new Random().nextInt(thanks.length)], Toast.LENGTH_LONG).show();
                        } else {
                            Log.w(TAG, "Error while consuming: " + result);
                        }
                    }
                };

                if (inventory.hasPurchase(SKU_DONATE_1)) {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_DONATE_1), onConsumeFinishedListener);
                }

                if (inventory.hasPurchase(SKU_DONATE_5)) {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_DONATE_5), onConsumeFinishedListener);
                }

                if (inventory.hasPurchase(SKU_DONATE_10)) {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_DONATE_10), onConsumeFinishedListener);
                }
            }
        };
    }


    // User clicked the "Upgrade to Premium" button.
    public void onDonateButtonClicked(String sku) {
        if (!mBillingServiceReady) {
            Toast.makeText(mContext, R.string.toast_billing_not_available, Toast.LENGTH_LONG).show();
            return;
        }

        if (mHelper != null) {
            mHelper.flagEndAsync();
            mHelper.launchPurchaseFlow(mContext, sku, 6639, sPurchaseFinishedListener, sku);
        }
    }

    public void showDonateDialog() {

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        };

        View view = mContext.getLayoutInflater().inflate(R.layout.dialog_donate, null);
        view.findViewById(R.id.donate_1).setOnClickListener(clickListener);
        view.findViewById(R.id.donate_5).setOnClickListener(clickListener);
        view.findViewById(R.id.donate_10).setOnClickListener(clickListener);
        view.findViewById(R.id.donate_paypal).setOnClickListener(clickListener);

        QKDialog dialog = new QKDialog()
                .setContext(mContext)
                .setCustomView(view);

        dialog.show(mContext.getFragmentManager(), "donate dialog");
    }

    public void donatePaypal() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/QKSMSDonation"));
        mContext.startActivity(browserIntent);
    }

}
