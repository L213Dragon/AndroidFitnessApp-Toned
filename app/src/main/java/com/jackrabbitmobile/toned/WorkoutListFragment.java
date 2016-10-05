package com.jackrabbitmobile.toned;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jackrabbitmobile.toned.model.ParseApplication;
import com.jackrabbitmobile.toned.model.Workout;
import com.parse.ConfigCallback;
import com.parse.FindCallback;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.recyclerview.internal.CardArrayRecyclerViewAdapter;


/**
 * A placeholder fragment containing a simple view.
 */
public class WorkoutListFragment extends Fragment {

    private static int MAX_WORKOUTS = 20;
    private Callbacks mListener;
    private ArrayList<Workout> workouts;
    private WorkoutAdapter mAdapter;

    public WorkoutListFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof Callbacks) {
            mListener = (Callbacks) activity;
        } else {
            throw new IllegalArgumentException(
                    activity.getClass().getSimpleName()
                    + " must implement " + Callbacks.class.getSimpleName());
        }

        ParseConfig.getInBackground(new ConfigCallback() {
            @Override
            public void done(ParseConfig parseConfig, ParseException e) {
                MAX_WORKOUTS = parseConfig.getNumber("howManyAreAvailableOnAndroid").intValue();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        workouts = new ArrayList<>();
        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.workouts_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new WorkoutAdapter(workouts, getActivity());
        mAdapter.setOnItemClickListener(new WorkoutAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                Workout w = workouts.get(position);
                mListener.onItemSelected(w.getObjectId(), w.getTitle());
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        getWorkouts();

        return rootView;
    }

    //the interface for the exercise clicked
    public interface Callbacks {
        void onItemSelected(String objectId, String workoutName);
        void onLoadWorkoutsComplete(List<Workout> workoutCount);
    }

    public void getWorkouts(){
        ParseQuery<Workout> query = ParseQuery
                .getQuery(Workout.class)
                .orderByDescending("priority");

        query.findInBackground(new FindCallback<Workout>() {
            public void done(List<Workout> parseObjects, com.parse.ParseException e) {
                int current = 1;
                if (parseObjects != null) {
                    for (Workout workout : parseObjects) {
                        if (!workout.getBoolean("public")) continue;
                        workouts.add(workout);

                        if (++current > MAX_WORKOUTS) {
                            break;
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                    mListener.onLoadWorkoutsComplete(parseObjects);
                }
            }
        });
    }
}

