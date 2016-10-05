package com.jackrabbitmobile.toned;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;
import com.jackrabbitmobile.toned.model.Product;
import com.jackrabbitmobile.toned.model.Workout;
import com.jackrabbitmobile.toned.util.NetworkConnectionUtil;

import java.util.ArrayList;
import java.util.List;


public class WorkoutListActivity extends CenteredAppCompat
        implements WorkoutListFragment.Callbacks, BillingUtil.Callbacks {

    private static boolean mTwoPane;
    private String TAG = WorkoutListActivity.class.getSimpleName();

    //todo:move billing related checks to a startup activity/splashscreen
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
            BillingUtil.getAvailableSubscriptions(WorkoutListActivity.this);
            BillingUtil.getActiveSubscriptions(WorkoutListActivity.this);
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
    protected void onPause() {
        super.onPause();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        bindBillingService();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workouts);

        startService(new Intent(this, NetworkConnectionUtil.class));

        bindBillingService();

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.about_jill_icon);
        }
    }

    private void bindBillingService() {
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_workouts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == android.R.id.home) {
            intentToAboutNatalieJill();
            return true;
        }

        if(id == R.id.action_profile) {
            intentToProfile();
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            intentToSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This is the implementation of the callback in WorkoutDetailFragment
     * that communicates with the detail fragment
     *
     * @param objectId the specific id of the Parse object passed in from the WorkoutList frag
     */
    @Override
    public void onItemSelected(String objectId, String workoutName) {

        //The user selected a workout from the WorkoutListFragment
        //do something to display the detail view of that workout
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // updating the detail fragment using a
        } else {
            //Otherwise, we are in a one-pane layout, start detail activity
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), WorkoutDetailActivity.class);
            intent.putExtra(WorkoutDetailFragment.EXTRA_WORKOUT_ID, objectId);
            intent.putExtra(WorkoutDetailFragment.EXTRA_WORKOUT_NAME, workoutName);
            startActivity(intent);
        }
    }

    @Override
    public void onLoadWorkoutsComplete(List<Workout> workoutCount) {

    }

    private void intentToAboutNatalieJill() {
        Intent intent = new Intent(this, AboutNatalieJillActivity.class);
        startActivity(intent);
    }

    private void intentToSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void intentToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    @Override
    public void onAvailableSubscriptionsLoaded(ArrayList<Product> subscriptions) {

    }

    @Override
    public void onActiveSubscriptionsLoaded(ArrayList<Product> subscriptions) {
        SharedPreferences preferences =
                getSharedPreferences(BillingUtil.PREFERENCE_SUBSCRIPTION, Context.MODE_PRIVATE);

        boolean refresh = preferences.getBoolean(BillingUtil.PREFERENCE_SUBSCRIPTION_ACTIVE, false);
        if ((subscriptions.size() > 0) && !refresh) {
            preferences.edit().putBoolean(BillingUtil.PREFERENCE_SUBSCRIPTION_ACTIVE, true).apply();
            Intent intent = new Intent(this, WorkoutListActivity.class);
            startActivity(intent);
        }
    }
}
