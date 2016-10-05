package com.jackrabbitmobile.toned;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.jackrabbitmobile.toned.util.NetworkConnectionUtil;

import java.io.File;

public class SettingsActivity extends CenteredAppCompat {

    private static final String STORE_APP_STRING = "market://details?id=";
    private static final String STORE_WEB_STRING = "https://play.google.com/store/apps/details?id=";
    private static final String SUPPORT_EMAIL = "tonedcustomer@gmail.com";
    public static final String SMS_BODY = "sms_body";
    public static final String PRIVACY_POLICY = "raw/privacy_policy.html";
    private static final String TERMS_ASSET = "raw/terms_and_conditions.html";
    private PackageInfo mInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_settings);

        startService(new Intent(this, NetworkConnectionUtil.class));

        if (getSupportActionBar() !=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            setActionBarTitle(getResources().getString(R.string.title_activity_settings));
        }

        try {
            mInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            TextView textView = (TextView) findViewById(R.id.version);
            textView.setText(getPackageName() + " version " + Build.VERSION.RELEASE);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        View email = findViewById(R.id.natalie_message_layout);
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                intent.setType("text/html");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{SUPPORT_EMAIL});
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject_message));
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_intro));
                startActivity(Intent.createChooser(intent, getString(R.string.email_intent_message)));
            }
        });

        View request = findViewById(R.id.request);
        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                intent.setType("text/html");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{SUPPORT_EMAIL});
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject_request));
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_intro));
                startActivity(Intent.createChooser(intent, getString(R.string.email_intent_request)));
            }
        });

        View text = findViewById(R.id.title_settings_text);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse("sms:" + "") );
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                intent.putExtra(SMS_BODY,
                        getString(R.string.sms_message) + getPlayStoreUri());
                startActivity(intent);
            }
        });

        View rate = findViewById(R.id.rate);
        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, getMarketAppUri());
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                startActivity(intent);
            }
        });

        findViewById(R.id.policy_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = getResources().getString(R.string.setting_privacy);
                view(title, PRIVACY_POLICY);
            }
        });

        findViewById(R.id.terms_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = getResources().getString(R.string.settings_terms);
                view(title, TERMS_ASSET);
            }
        });
    }

    private void view(String title, String path) {
        Intent intent = new Intent(SettingsActivity.this, ShowDocumentActivity.class);
        intent.setData(Uri.parse("file:///android_asset" + File.separator + path));
        intent.putExtra(ShowDocumentActivity.ACTIONBAR_TITLE, title);
        startActivity(intent);
    }

    private Uri getMarketAppUri() {
        return Uri.parse(STORE_APP_STRING + mInfo.packageName);
    }

    private Uri getPlayStoreUri() {
        return Uri.parse(STORE_WEB_STRING + mInfo.packageName);
    }
}
