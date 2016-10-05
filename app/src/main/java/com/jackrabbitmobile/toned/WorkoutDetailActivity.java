package com.jackrabbitmobile.toned;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;

import com.jackrabbitmobile.toned.model.Workout;
import com.jackrabbitmobile.toned.util.NetworkConnectionUtil;
import com.parse.ParseFile;

import java.util.List;

public class WorkoutDetailActivity extends CenteredAppCompat
        implements WorkoutListFragment.Callbacks, WorkoutDetailFragment.Callbacks {

    public static final String DEFAULT_VIDEO = "zCwaeeuhQ4I";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        startService(new Intent(this, NetworkConnectionUtil.class));

        if (savedInstanceState == null){

            String workoutId = getIntent().getStringExtra(WorkoutDetailFragment.EXTRA_WORKOUT_ID);
            String workoutName = getIntent().getStringExtra(WorkoutDetailFragment.EXTRA_WORKOUT_NAME);
            boolean start = getIntent().getBooleanExtra(WorkoutDetailFragment.EXTRA_START_WORKOUT, false);

            WorkoutDetailFragment detailFragment =
                    WorkoutDetailFragment.newInstance(workoutId, workoutName, start);

            getFragmentManager().beginTransaction()
                    .add(R.id.workout_detail_container, detailFragment).commit();



            if(getSupportActionBar() != null) {
                setActionBarTitle(workoutName);
            }
        }

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }


    @Override
    public void onItemSelected(String objectId, String workoutName) {

        //The user selected a workout from the WorkoutListFragment
        //do something to display the detail view of that workout

        WorkoutDetailFragment detailFragment =
                (WorkoutDetailFragment) getFragmentManager().
                        findFragmentById(R.id.workout_detail_container);

        if (detailFragment != null) {
            //this is a wide format screen
            //todo:get the detail fragment
            //todo:populate the detail fragment
        } else {
            //must perform a screen transition
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onLoadWorkoutsComplete(List<Workout> workoutCount) {

    }

    @Override
    public void onRoutineClicked(final String youtubeId) {
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra(VideoPlayerActivity.VIDEO_ID, availableVideo(youtubeId));
        startActivity(intent);
    }

    //we should really display a message stating that the video could not be loaded but for now
    //we play the default video
    //todo:add alert that the video is not available
    private String availableVideo(String id) {
        return (id != null) ? id : DEFAULT_VIDEO;
    }
}
