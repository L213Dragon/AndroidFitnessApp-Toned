package com.jackrabbitmobile.toned;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.jackrabbitmobile.toned.util.NetworkConnectionUtil;

public class VideoPlayerActivity extends YouTubeBaseActivity
        implements YouTubePlayer.OnInitializedListener {

    public static final String VIDEO_ID = "VIDEO_ID";
    public static final String APP_STRING = "market://details?id=com.google.android.youtube";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_video_player);

        startService(new Intent(this, NetworkConnectionUtil.class));

        YouTubePlayerView youTubePlayerView = (YouTubePlayerView) findViewById(R.id.player);
        youTubePlayerView.initialize(MyYoutubePlayerFragment.API_KEY, this);

        //fix:YouTube video playback stopped due to unauthorized overlay on top of player.
//        ImageButton closeButton = (ImageButton) findViewById(R.id.buttonClose);
//        closeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                VideoPlayerActivity.super.onBackPressed();
//            }
//        });
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        Intent intent = getIntent();
        youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);

        if (!b && (intent != null)) {
            youTubePlayer.loadVideo(intent.getStringExtra(VIDEO_ID));
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        //send the user to the google play store if youtube isn't installed
        switch (youTubeInitializationResult){
            case INTERNAL_ERROR:
            case UNKNOWN_ERROR:
            case SERVICE_MISSING:
            case SERVICE_VERSION_UPDATE_REQUIRED:
            case SERVICE_INVALID:
            case CLIENT_LIBRARY_UPDATE_REQUIRED:
            case SERVICE_DISABLED:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(APP_STRING));
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                startActivity(intent);
                break;
            case INVALID_APPLICATION_SIGNATURE:
            case NETWORK_ERROR:
            case DEVELOPER_KEY_INVALID:
            case ERROR_CONNECTING_TO_SERVICE:
        }
    }
}