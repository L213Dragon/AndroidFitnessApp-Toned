package com.jackrabbitmobile.toned;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Base64;

import com.android.vending.billing.IInAppBillingService;
import com.google.gson.Gson;
import com.jackrabbitmobile.toned.model.Product;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Wrapper for Google Play Services Billing API.
 * This utility should always be used within an activity.
 * The activity using this utility provides the context and must have a
 * IInAppBillingService connection active in order to work correctly.
 * NOTE: call getInstance in IInAppBillingService.onServiceConnected.
 */
public class BillingUtil {

    public static final int REQUEST_BUY = 1001;
    public static final int REQUEST_CHOOSE_MONTHLY_ACCOUNT = 0;
    public static final int REQUEST_CHOOSE_YEARLY_ACCOUNT = 1;
    public static final String INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA";

    private static final String ITEM_ID_LIST = "ITEM_ID_LIST";
    private static final String RESPONSE_CODE = "RESPONSE_CODE";
    private static final String PRODUCT_TYPE_INAPP = "inapp"; //not subscription...?
    private static final String PRODUCT_TYPE_SUBSCRIPTION = "subs"; //subscriptions
    private static final String DETAILS_LIST = "DETAILS_LIST";
    private static final String BUY_INTENT = "BUY_INTENT";

    private static final int BILLING_API_VERSION = 3;
    private static final int RESPONSE_OK = 0;
    public static final String PREFERENCE_SUBSCRIPTION = "com.jackrabbitmobile.toned.PREFERENCE_SUBSCRIPTION";
    public static final String PREFERENCE_SUBSCRIPTION_ACTIVE = "com.jackrabbitmobile.toned.PREFERENCE_SUBSCRIPTION_ACTIVE";
    public static final String INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
    private static Context mContext;

    private static IInAppBillingService mService;
    private static BillingUtil mInstance;
    private static Handler mHandler = new Handler();

    //can't touch this...
    private BillingUtil() {

    }

    /**
     * get an instance of the billing utility class
     *
     * @param context the current application context
     * @param service the IInAppBillingService service connection
     * @return reference to the BillingUtil static instance
     */
    public static BillingUtil getInstance(@NonNull Context context, @NonNull IInAppBillingService service) {
        if (mInstance == null) {
            mInstance = new BillingUtil();
            mContext = context;
            mService = service;
        }

        return mInstance;
    }

    /**
     * query the billing api for purchasable products (subscriptions)
     */
    public static void getAvailableSubscriptions(@NonNull final Callbacks listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //retrieve purchasable products
                ArrayList<String> skuList = new ArrayList<>();
                Bundle querySkus = new Bundle();
                skuList.add(mContext.getString(R.string.sku_monthly_subscription));
                skuList.add(mContext.getString(R.string.sku_yearly_subscription));
                querySkus.putStringArrayList(ITEM_ID_LIST, skuList);

                Bundle skuDetails = null;
                try {
                    skuDetails = mService.getSkuDetails(BILLING_API_VERSION,
                            mContext.getPackageName(), PRODUCT_TYPE_SUBSCRIPTION, querySkus);

                    listener.onAvailableSubscriptionsLoaded(getProductList(skuDetails));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * retrieve active subscriptions
     */
    public static void getActiveSubscriptions(@NonNull final Callbacks listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //retrieve what the user has purchased
                Bundle purchases = null;
                try {
                    purchases = mService.getPurchases(BILLING_API_VERSION,
                            mContext.getPackageName(), PRODUCT_TYPE_SUBSCRIPTION, null);

                    listener.onActiveSubscriptionsLoaded(getProductList(purchases));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * convert result of product queries to a list of Product objects
     *
     * @param args the query result from the google billing api
     * @return a list of Product objects
     */
    private static ArrayList<Product> getProductList(Bundle args) {
        ArrayList<Product> products = new ArrayList<>();

        int response = args.getInt(RESPONSE_CODE);
        if (response == RESPONSE_OK) {
            ArrayList<String> responseList
                    = args.getStringArrayList(DETAILS_LIST);
            ArrayList<String> purchaseList
                    = args.getStringArrayList(INAPP_PURCHASE_DATA_LIST);

            if (responseList != null) {
                for (String thisResponse : responseList) {
                    Gson gson = new Gson();
                    Product purchase = gson.fromJson(thisResponse, Product.class);
                    products.add(purchase);
                }
            }

            if (purchaseList != null) {
                for (String thisResponse : purchaseList) {
                    Gson gson = new Gson();
                    Product purchase = gson.fromJson(thisResponse, Product.class);
                    products.add(purchase);
                }
            }
        }

        return products;
    }

    /**
     * NOTE: calling activity must implement onActivity result and handle BillingUtil.REQUEST_BUY
     *
     * @param activity the calling activity
     * @param account  the active account
     */
    public static void purchaseMonthlySubscription(Activity activity, String account) {
        purchaseSubscription(activity, account, mContext.getString(R.string.sku_monthly_subscription));
    }

    public static void purchaseYearlySubscription(Activity activity, String account) {
        purchaseSubscription(activity, account, mContext.getString(R.string.sku_yearly_subscription));
    }

    private static void purchaseSubscription(Activity activity, String account, String product) {
        String base64 = new String(Base64.encode(account.getBytes(), Base64.DEFAULT));
        try {
            Bundle buyIntentBundle = mService.getBuyIntent(BILLING_API_VERSION,
                    mContext.getPackageName(), product, PRODUCT_TYPE_SUBSCRIPTION, base64);

            if (buyIntentBundle.getInt(RESPONSE_CODE) == RESPONSE_OK) {
                PendingIntent pendingIntent = buyIntentBundle.getParcelable(BUY_INTENT);
                if (pendingIntent != null) {
                    activity.startIntentSenderForResult(pendingIntent.getIntentSender(),
                            REQUEST_BUY, new Intent(), 0, 0, 0);
                }
            }
        } catch (RemoteException | IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    public static void createIntentAndStartActivity(Activity activity, int subscriptionRequestCode) {
        AccountManager accountManager = AccountManager.get(activity);
        ArrayList<Account> allowableAccounts = new ArrayList<>();
        Collections.addAll(allowableAccounts, accountManager.getAccounts());
        Intent intent = AccountManager.newChooseAccountIntent(
                null, allowableAccounts, new String[]{"com.google"}, true, null, null, null, null);
        activity.startActivityForResult(intent, subscriptionRequestCode);
    }

    /**
     * public interface for receiving product data when loading is complete
     */
    public interface Callbacks {
        void onAvailableSubscriptionsLoaded(ArrayList<Product> subscriptions);
        void onActiveSubscriptionsLoaded(ArrayList<Product> subscriptions);
    }
}
