package com.jackrabbitmobile.toned;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.jackrabbitmobile.toned.R;

public class CenteredAppCompat extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_centered_app_compat);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.toolbar_centered);
        }
    }

    protected void setActionBarTitle(String title) {
        ((TextView)findViewById(R.id.actionbar_title)).setText(title);
    }
}
