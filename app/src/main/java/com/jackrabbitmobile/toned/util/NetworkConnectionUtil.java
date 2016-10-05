package com.jackrabbitmobile.toned.util;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;

import com.jackrabbitmobile.toned.NetworkDisconnectedActivity;

/**
 * Created by victor on 157/28/.
 */
public class NetworkConnectionUtil extends Service {

    private static final long WAIT_INTERVAL = 5000;
    private static ConnectivityManager mService;
    private static Context mContext;
    private static boolean mRunning = false;
    private static Binder binder = new Binder();

    public NetworkConnectionUtil() {
        super();
    }

    public static class Status {
        public static void notifyDisconnected() {
            mRunning = false;
            Intent intent = new Intent(mContext, NetworkDisconnectedActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!mRunning) {
            mRunning = true;
            checkStatus();
            mContext = getBaseContext();
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public static void checkStatus() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                do {
                    ConnectivityManager connMgr = (ConnectivityManager)
                            mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        // fetch data
                    } else {
                        // display error
                        Status.notifyDisconnected();
                    }

                    try {
                        synchronized (this) {
                            wait(WAIT_INTERVAL);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (mRunning);
            }
        }).start();
    }
}
