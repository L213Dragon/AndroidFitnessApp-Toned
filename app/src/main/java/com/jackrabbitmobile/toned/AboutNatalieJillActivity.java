package com.jackrabbitmobile.toned;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;
import com.jackrabbitmobile.toned.model.Product;
import com.jackrabbitmobile.toned.util.NetworkConnectionUtil;

import java.util.ArrayList;

/**
 * Created by SamMyxer on 7/2/15.
 */
public class AboutNatalieJillActivity extends CenteredAppCompat
        implements BillingUtil.Callbacks {

    TextView aboutTV;
    Button goPremiumBT;

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
            BillingUtil.getAvailableSubscriptions(AboutNatalieJillActivity.this);
            BillingUtil.getActiveSubscriptions(AboutNatalieJillActivity.this);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        setContentView(R.layout.activity_about_natalie_jill);

        startService(new Intent(this, NetworkConnectionUtil.class));

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            setActionBarTitle(getResources().getString(R.string.about_natalie_jill));
        }

        aboutTV = (TextView) findViewById(R.id.scrollable_text_view_about_natalie_jill);
        goPremiumBT = (Button) findViewById(R.id.go_premium_bt_about_natalie_jill);
        goPremiumBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), SubscribeActivity.class);
                startActivity(i);
            }
        });

        MyYoutubePlayerFragment youTubePlayerFragment = (MyYoutubePlayerFragment) getFragmentManager()
                .findFragmentById(R.id.you_tube_player_fragment);
        youTubePlayerFragment.initialize(MyYoutubePlayerFragment.API_KEY, youTubePlayerFragment);
    }

    @Override
    public void onAvailableSubscriptionsLoaded(ArrayList<Product> subscriptions) {

    }

    @Override
    public void onActiveSubscriptionsLoaded(ArrayList<Product> subscriptions) {
        for (Product product : subscriptions) {
            if (product.productId.equals(getResources().getString(R.string.sku_monthly_subscription))) {
                //let the user purchase the yearly subscription even though they already have a monthly one
                goPremiumBT.setText("Upgrade subscription!");
            }
            if (product.productId.equals(getResources().getString(R.string.sku_yearly_subscription))) {
                // hide the button because they have the maximum available product
                goPremiumBT.setVisibility(View.GONE);
            }
        }
    }
}
