package com.jackrabbitmobile.toned;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.jackrabbitmobile.toned.model.Exercise;
import com.jackrabbitmobile.toned.model.Workout;
import com.jackrabbitmobile.toned.util.ExerciseTimer;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class WorkoutDetailFragment extends Fragment {

    private static final String PREFERENCE_EXERCISE = "com.jackrabbitmobile.toned.EXERCISE";
    private static final String PREFERENCE_SHOW_TAGLINE = "show_tagline";

    final static String EXTRA_WORKOUT_ID = "workout_detail_fragment_workout_id";
    final static String EXTRA_WORKOUT_NAME = "workout_detail_fragment_workout_name";
    final static String EXTRA_START_WORKOUT = "start_workout";

    //cases for exercise routine formatting
    private static final String DURATION = "DurationInSec";
    private static final String REPS = "Reps";
    private static final String SETS = "Sets";

    private FloatingActionButton mActionButton;
    private String mWorkoutObjectId;
    private String workoutName;
    private ImageView mHeaderView;
    private ArrayList<ExerciseTimer> mTimers;
    private Callbacks mListener;
    private ArrayList<Exercise> exercises;
    private ExerciseAdapter exerciseAdapter;
    private LinearLayout exercisesList;
    private ScrollView mScrollView;
    private Menu detailsMenu;
    private boolean mStartAfterLoad = false;
    private boolean mShowTagline = false;
    private boolean mStarted = false;
    private SharedPreferences mPreferences;
    private ImageView mTagline;

    public static WorkoutDetailFragment newInstance(
            String workoutId, String workoutName, boolean start) {

        Bundle args = new Bundle();
        args.putString(EXTRA_WORKOUT_ID, workoutId);
        args.putString(EXTRA_WORKOUT_NAME, workoutName);
        args.putBoolean(EXTRA_START_WORKOUT, start);

        WorkoutDetailFragment fragment = new WorkoutDetailFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof Callbacks) {
            mListener = (Callbacks) activity;
        } else {
            throw new IllegalArgumentException(
                    activity.getClass().getSimpleName()
                            + " must implement "
                            + Callbacks.class.getSimpleName()
            );
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mWorkoutObjectId = getArguments().getString(EXTRA_WORKOUT_ID);
        workoutName = getArguments().getString(EXTRA_WORKOUT_NAME);
        mStartAfterLoad = getArguments().getBoolean(EXTRA_START_WORKOUT);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mTimers = new ArrayList<>();

        getActivity().setTitle(workoutName);

        mPreferences =
                getActivity().getSharedPreferences(PREFERENCE_EXERCISE, Context.MODE_PRIVATE);

        mShowTagline = mPreferences.getBoolean(PREFERENCE_SHOW_TAGLINE, true);
    }

    View.OnClickListener startWorkout = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View rootView = getView();
            if (rootView == null) return;

            mStarted = true;

            if (mShowTagline) {
                mTagline.setBackground(getResources().getDrawable(R.drawable.ic_finish_workout_tagline));
                mPreferences.edit().putBoolean(PREFERENCE_SHOW_TAGLINE, false).apply();
            }

            mActionButton = (FloatingActionButton) rootView.findViewById(R.id.action_button);
            mActionButton.setImageResource(R.drawable.workout_fab_badge);

            for (ExerciseTimer timer : mTimers) {
                timer.setAlpha(1.0f);
                timer.setEnabled(true);
            }

            mScrollView.smoothScrollTo(0, (int) exercisesList.getY());

            mActionButton.setOnClickListener(finishWorkout);
        }
    };

    View.OnClickListener finishWorkout = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getActivity().finish();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mTagline = (ImageView) rootView.findViewById(R.id.tagline);
        if (!mShowTagline) {
            mTagline.setVisibility(View.GONE);
        }

        mHeaderView = (ImageView) rootView.findViewById(R.id.detail_header_image);

        //setup the header
        ParseQuery<Workout> query = ParseQuery.getQuery(Workout.class);
        query.getInBackground(mWorkoutObjectId, new GetCallback<Workout>() {
            @Override
            public void done(Workout workout, ParseException e) {
                Picasso.with(getActivity())
                        .load(workout.getFullImage().getUrl())
                        .error(R.drawable.l_trash)
                        .into(mHeaderView);

            }
        });

        //set up the list view below the header
        exercises = new ArrayList<Exercise>();
        exercisesList = (LinearLayout) rootView.findViewById(R.id.exercises_layout);
        exerciseAdapter = new ExerciseAdapter(getActivity(), R.layout.exercise_row, exercises);
        mScrollView = (ScrollView) rootView.findViewById(R.id.scrollview);
        mScrollView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);

        FloatingActionButton button = (FloatingActionButton) rootView.findViewById(R.id.action_button);
        button.setImageResource(R.drawable.workout_start);
        button.setOnClickListener(startWorkout);

        fetchExercises();

        return rootView;
    }

    /**
     * This function sets up the exercise cards displayed in the list view by
     * calling Parse and the CardsLib framework
     *
     * @return ArrayList of formatted card
     */
    private void fetchExercises() {

        ParseQuery<Workout> query = ParseQuery.getQuery(Workout.class);
        query.getInBackground(mWorkoutObjectId, new GetCallback<Workout>() {
            @Override
            public void done(Workout workout, ParseException e) {

                //get the routine descriptions
                final List<HashMap<String, Integer>> routineList = workout.getExerciseConfigList();
                final ArrayList<String> routineText = formatRoutineToText(routineList);

                //get the list of exercise object ids
                final List<Exercise> exerciseList = workout.getExerciseList();
                final ArrayList<String> exerciseIds = new ArrayList<String>();

                int d = 0;
                try {
                    d = routineList.get(0).get("DurationInSec");
                } catch (NullPointerException exception) {
                    exception.printStackTrace();
                }
                final int duration = d;

                for (Exercise exercise : exerciseList) {
                    exerciseIds.add(exercise.getObjectId());
                }

                if (e == null) {
                    //query for the exercises
                    ParseQuery<Exercise> exerciseQuery = ParseQuery.getQuery(Exercise.class);
                    exerciseQuery.whereContainedIn("objectId", exerciseIds);
                    exerciseQuery.findInBackground(new FindCallback<Exercise>() {
                        @Override
                        public void done(List<Exercise> list, ParseException e) {
                            exercises = new ArrayList<>(list);
                            exerciseAdapter.setData(exercises);

                            LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pixelsToDp(100));
                            rowLayoutParams.setMargins(0, 0, 0, 0);

                            for (int i = 0; i < exercises.size(); i++) {
                                final Exercise exercise = exercises.get(i);
                                exercises.get(i).setExerciseDesc(routineText.get(i));
                                final View item = exerciseAdapter.getView(i, null, null);
                                item.setLayoutParams(rowLayoutParams);

                                final ExerciseTimer timer = (ExerciseTimer) item.findViewById(R.id.exercise_timer);
                                ImageView check = (ImageView) timer.findViewById(R.id.exercise_complete_check);
                                timer.setEnabled(false);
                                timer.setDuration(duration);

                                if (duration > 0) {
                                    timer.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            ExerciseTimer timer = (ExerciseTimer) v;
                                            timer.start();
                                        }
                                    });
                                } else {
                                    check.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            timer.toggleChecked();
                                        }
                                    });
                                }
                                mTimers.add(timer);

                                item.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (mStarted) mListener.onRoutineClicked(exercise.getYoutubeID());
                                    }
                                });
                                exercisesList.addView(item);
                            }

                            //hack:add an invisible view to the list as "padding"
                            LayoutInflater inflater = LayoutInflater.from(WorkoutDetailFragment.this.getActivity());
                            final View padding = inflater.inflate(R.layout.exercise_row, null);
                            padding.setVisibility(View.INVISIBLE);
                            padding.setLayoutParams(rowLayoutParams);
                            padding.setOnClickListener(null);
                            exercisesList.addView(padding);
                            exercisesList.refreshDrawableState();

                            if (mStartAfterLoad) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        startWorkout.onClick(mActionButton);
                                    }
                                }, 2000);//hack: wait a couple of seconds
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * This helper function gets the list of hashmaps from Parse and formats each pair
     * according to the exercise routine.
     *
     * @param routineHashMap
     * @return an array of formatted strings
     */
    public ArrayList<String> formatRoutineToText(List<HashMap<String, Integer>> routineHashMap) {

        ListIterator<HashMap<String, Integer>> iterator = null;
        ArrayList<String> routineTextArray = new ArrayList<>();

        String setText = null;
        String durationText = null;
        String repText = null;
        String routineText = null;
        String setNumber = null;

        //get a pointer for each list key/value pair ya monkey!
        iterator = routineHashMap.listIterator();

        //hasNext() starts at -1
        while (iterator.hasNext()) {

            //go to the next one
            HashMap<String, Integer> entry = iterator.next();

            //create the text string from each pair (system.out is for testing)
            for (Map.Entry<String, Integer> e : entry.entrySet()) {

                switch (e.getKey()) {

                    case DURATION:
                        durationText = e.getValue() + " Seconds";
                        routineText = setText + durationText;

                    case REPS:
                        repText = e.getValue().toString() + " Reps"; //ex., 4 Reps X 60 Seconds
                        routineText = setText + repText;

                    case SETS:
                        setNumber = (e.getValue() == 1) ? " Set X " : " Sets X ";
                        setText = e.getValue().toString() + setNumber; //ex., 5 Sets X 60 Seconds
                }
            }

            //add the string to the array
            routineTextArray.add(routineText);
        }

        return routineTextArray;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_detail, menu);

        detailsMenu = menu;

        setBookmarkedIcon();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_bookmark) {
            SharedPreferences preferences = getActivity()
                    .getSharedPreferences(Workout.PREFERENCE_BOOKMARKED, Context.MODE_PRIVATE);

            if(!preferences.getBoolean(mWorkoutObjectId, false)) {
                Drawable whiteIcon = tintToolbarIcon(item.getIcon(), getResources().getColor(R.color.bookmark_active));
                item.setIcon(whiteIcon);
                preferences.edit().putBoolean(mWorkoutObjectId, true).apply();
            } else {
                Drawable darkIcon = tintToolbarIcon(item.getIcon(), getResources().getColor(R.color.bookmark_inactive));
                item.setIcon(darkIcon);
                preferences.edit().putBoolean(mWorkoutObjectId, false).apply();
            }
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    public interface Callbacks {
        void onRoutineClicked(String youtubeId);
    }

    public Drawable tintToolbarIcon(Drawable d, int color) {

        Drawable drawable = DrawableCompat.wrap(d);
        DrawableCompat.setTint(drawable, color);

        return drawable;

    }

    //This method will be used to set the icon color based on if the user has bookmarked the workout
    //right now the bookmark will always be set to false since the bookmark function is not incorporated
    public void setBookmarkedIcon() {
        if(detailsMenu != null) {
            SharedPreferences preferences = getActivity()
                    .getSharedPreferences(Workout.PREFERENCE_BOOKMARKED, Context.MODE_PRIVATE);
            boolean bookmarked = preferences.getBoolean(mWorkoutObjectId, false);
            MenuItem bookmarkItem = detailsMenu.findItem(R.id.action_bookmark);
            int color = (bookmarked) ? R.color.bookmark_active : R.color.bookmark_inactive;
            bookmarkItem.setIcon(tintToolbarIcon(bookmarkItem.getIcon(), getResources().getColor(color)));
        }
    }


    public int pixelsToDp(int dps) {
        final float scale = getActivity().getResources().getDisplayMetrics().density;
        return  (int) (dps * scale + 0.5f);
    }
}


