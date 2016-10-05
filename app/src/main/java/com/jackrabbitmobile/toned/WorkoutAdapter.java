package com.jackrabbitmobile.toned;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jackrabbitmobile.toned.model.Workout;
import com.parse.ParseImageView;

import java.util.ArrayList;


/**
 * Created by SamMyxer on 7/8/15.
 */
public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.ViewHolder> {
    private final boolean mActiveSubscription;
    private ArrayList<Workout> mDataset;
    Context mContext;

    // Define listener member variable
    private static OnItemClickListener listener;
    private int mDescriptionHeight;

    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }
    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        WorkoutAdapter.listener = listener;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView titleTV;
        public TextView descriptionTV;
        public TextView startTV;
        public ParseImageView imageView;
        public final TextView learnTV;

        public ViewHolder(View v) {
            super(v);
            titleTV = (TextView) v.findViewById(R.id.title_text_workout_row);
            descriptionTV = (TextView) v.findViewById(R.id.description_text_workout_row);
            imageView = (ParseImageView) v.findViewById(R.id.image_workout_row);
            startTV = (TextView) v.findViewById(R.id.start_workout_button_row);
            learnTV = (TextView) v.findViewById(R.id.learn_more_button_row);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (listener != null)
                        listener.onItemClick(v, getLayoutPosition());
                }
            });
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public WorkoutAdapter(ArrayList<Workout> myDataset, Context context) {
        mDataset = myDataset;
        mContext = context;

        SharedPreferences preferences =
                mContext.getSharedPreferences(BillingUtil.PREFERENCE_SUBSCRIPTION, Context.MODE_PRIVATE);

        mActiveSubscription = preferences.getBoolean(BillingUtil.PREFERENCE_SUBSCRIPTION_ACTIVE, false);
        mDescriptionHeight = (int) context.getResources().getDimension(R.dimen.workout_description_height);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public WorkoutAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_workout, parent, false);

        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        v.setTag(vh);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        final Workout data = mDataset.get(position);

        holder.titleTV.setText(data.getTitle());
        holder.descriptionTV.setText(data.getComment());
        holder.imageView.setParseFile(data.getFullImage());
        holder.imageView.loadInBackground();

        holder.itemView.findViewById(R.id.paywall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext.getApplicationContext(), SubscribeActivity.class);
                mContext.startActivity(i);
            }
        });

        holder.startTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(mContext, WorkoutDetailActivity.class);
                intent.putExtra(WorkoutDetailFragment.EXTRA_WORKOUT_ID, data.getObjectId());
                intent.putExtra(WorkoutDetailFragment.EXTRA_WORKOUT_NAME, data.getTitle());
                intent.putExtra(WorkoutDetailFragment.EXTRA_START_WORKOUT, true);
                mContext.startActivity(intent);
            }
        });

        View.OnClickListener descriptionListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isActivated()) {
                    ViewGroup.LayoutParams params = holder.descriptionTV.getLayoutParams();
                    params.height = mDescriptionHeight;
                    holder.descriptionTV.setLayoutParams(params);
                    holder.learnTV.setText(R.string.learn_expand);
                    v.setActivated(false);
                } else {
                    ViewGroup.LayoutParams params = holder.descriptionTV.getLayoutParams();
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    holder.descriptionTV.setLayoutParams(params);
                    holder.learnTV.setText(R.string.learn_collapse);
                    v.setActivated(true);
                }
            }
        };

        holder.descriptionTV.setOnClickListener(descriptionListener);
        holder.learnTV.setOnClickListener(descriptionListener);

        if (position == 0) {
            holder.itemView.findViewById(R.id.paywall).setVisibility(View.GONE);
        } else if (mActiveSubscription) {
            holder.itemView.findViewById(R.id.paywall).setVisibility(View.GONE);
        } else {
            holder.itemView.findViewById(R.id.paywall).setVisibility(View.VISIBLE);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }


}
