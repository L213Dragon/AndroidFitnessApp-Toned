package com.jackrabbitmobile.toned.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by victor on 157/26/.
 */
public class RobotoLightTextView extends TextView {

    Typeface mTypeface;

    public RobotoLightTextView(Context context) {
        super(context);
        setTypeface(context);
        setTypeface(mTypeface);
    }

    public RobotoLightTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(context);
        setTypeface(mTypeface);
    }

    public RobotoLightTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeface(context);
        setTypeface(mTypeface);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RobotoLightTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setTypeface(context);
        setTypeface(mTypeface);
    }

    private void setTypeface(Context context) {
        mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/RobotoCondensed-Light.ttf");
    }
}
