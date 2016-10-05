package com.jackrabbitmobile.toned;

import android.accounts.AccountManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.android.vending.billing.IInAppBillingService;
import com.jackrabbitmobile.toned.model.Product;
import com.jackrabbitmobile.toned.util.NetworkConnectionUtil;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;

/**
 * Created by SamMyxer on 7/6/15.
 * NOTE: if issues w/ ViewPagerIndicator library then use the commented out code for the PagerTabStrip
 * ViewPagerIndicator is a Jake Wharton library but was difficult to include
 * For use if there are any issues with ViewPagerIndicator library
 */
public class SubscribeActivity extends CenteredAppCompat implements BillingUtil.Callbacks {
    private static final String TAG = SubscribeActivity.class.getSimpleName();
    public static String name;
    ViewPager viewPager;
    //PagerTabStrip pagerTabStrip;
    CirclePageIndicator circlePageIndicator;
    int numberOfViewPagerChildren = 2;
    RelativeLayout monthlyLayout;
    RelativeLayout yearlyLayout;
    ImageButton exitButton;
    private IInAppBillingService mService;
    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {

            final IInAppBillingService mService = IInAppBillingService.Stub.asInterface(service);

            //intialize the billing utility and perform a couple of queries
            BillingUtil.getInstance(getBaseContext(), mService);
            BillingUtil.getAvailableSubscriptions(SubscribeActivity.this);
            BillingUtil.getActiveSubscriptions(SubscribeActivity.this);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        setContentView(R.layout.activity_subscribe);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        startService(new Intent(this, NetworkConnectionUtil.class));

        viewPager = (ViewPager) findViewById(R.id.pager);
        //pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_title_strip);
        circlePageIndicator = (CirclePageIndicator)findViewById(R.id.circular_page_indicator);
        monthlyLayout = (RelativeLayout) findViewById(R.id.monthly_layout);
        yearlyLayout = (RelativeLayout) findViewById(R.id.yearly_layout);
        exitButton = (ImageButton) findViewById(R.id.exit_activity_subscribe);

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        circlePageIndicator.setFillColor(getResources().getColor(R.color.toned_pink));
        circlePageIndicator.setPageColor(getResources().getColor(R.color.grey));
        circlePageIndicator.setRadius(12);

        monthlyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BillingUtil.createIntentAndStartActivity(SubscribeActivity.this, BillingUtil.REQUEST_CHOOSE_MONTHLY_ACCOUNT);
            }
        });

        yearlyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BillingUtil.createIntentAndStartActivity(SubscribeActivity.this, BillingUtil.REQUEST_CHOOSE_YEARLY_ACCOUNT);
            }
        });


        //pagerTabStrip.setTabIndicatorColor(getResources().getColor(R.color.toned_pink));
        viewPager.setAdapter(new SubscribeAdapter(getSupportFragmentManager()));
        circlePageIndicator.setViewPager(viewPager);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case BillingUtil.REQUEST_CHOOSE_MONTHLY_ACCOUNT: {
                if (resultCode == RESULT_OK) {
                    BillingUtil.purchaseMonthlySubscription(this, data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
                } else {
                    //todo:prompt user to notify them that no @google.com account is available
                }
            }
            break;
            case BillingUtil.REQUEST_CHOOSE_YEARLY_ACCOUNT: {
                if (resultCode == RESULT_OK) {
                    BillingUtil.purchaseYearlySubscription(this, data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
                } else {
                    //todo:prompt user to notify them that no @google.com account is available
                }
            }
            break;
            case BillingUtil.REQUEST_BUY: {
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(this, AboutNatalieJillActivity.class);
                    startActivity(intent);
                } else {
                }
            }
            break;
        }
    }

    @Override
    public void onAvailableSubscriptionsLoaded(ArrayList<Product> subscriptions) {

    }

    @Override
    public void onActiveSubscriptionsLoaded(ArrayList<Product> subscriptions) {
        for (Product subscription : subscriptions) {
            if (subscription.productId.equals(getString(R.string.sku_monthly_subscription))) {
                //we already have a monthly subscription
                monthlyLayout.setEnabled(false);
            }

            if (subscription.productId.equals(getString(R.string.sku_yearly_subscription))) {
                //we already have a yearly subscription
                yearlyLayout.setEnabled(false);
            }
        }
    }

    class SubscribeAdapter extends FragmentStatePagerAdapter {
        public SubscribeAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int i) {
            Fragment fragment=null;
            if(i==0)
            {
                fragment = new UnlimitedAccessFragment();
            }
            if(i==1)
            {
                fragment = new AllAboutYouFragment();
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return numberOfViewPagerChildren;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position == 0) {
                return "Unlimited";
            }
            if(position == 1) {
                return "All About YOU";
            }

            return "";
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            if(object instanceof UnlimitedAccessFragment){
                view.setTag(2);
            }
            if(object instanceof AllAboutYouFragment){
                view.setTag(1);
            }
            return super.isViewFromObject(view, object);
        }
    }

}


