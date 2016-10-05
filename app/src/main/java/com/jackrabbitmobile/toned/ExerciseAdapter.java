package com.jackrabbitmobile.toned;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jackrabbitmobile.toned.model.Exercise;
import com.parse.GetDataCallback;
import com.parse.ParseImageView;

import java.util.ArrayList;

/**
 * Created by SamMyxer on 7/7/15.
 */
public class ExerciseAdapter extends ArrayAdapter<Exercise> {

        Context mContext;
        int mLayoutResourceId;
        LayoutInflater mLayoutInflater;
        ArrayList<Exercise> mData;

        public ExerciseAdapter(Context context,int layoutResourceId, ArrayList<Exercise> tweets) {
            super(context, layoutResourceId , tweets);
            this.mContext = context;
            this.mLayoutResourceId = layoutResourceId;
            this.mData = tweets;
            mLayoutInflater = LayoutInflater.from(mContext);
        }

        public void setData(ArrayList<Exercise> updatedData) {
            mData = updatedData;
            this.notifyDataSetChanged();
        }

        @Override
        public Exercise getItem(int position) {
            return super.getItem(position);
        }

        public int getCount() {
            return mData.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = convertView;

            PlaceHolder holder = null;

            if (row == null) {
                row = mLayoutInflater.inflate(mLayoutResourceId, parent, false);

                holder = new PlaceHolder();

                holder.titleView = (TextView) row.findViewById(R.id.exercise_title_row);
                holder.repView = (TextView) row.findViewById(R.id.exercise_reps_row);
                holder.imageView = (ParseImageView) row.findViewById(R.id.exercise_image_row);
                holder.numberView = (TextView) row.findViewById(R.id.number_text_row);

                row.setTag(holder);
            }else {
                holder = (PlaceHolder) row.getTag();
            }

            //get the data from the array
            Exercise data = mData.get(position);

            //set the views

            holder.numberView.setText(String.valueOf(position + 1));
            holder.titleView.setText(data.getTitle());
            holder.repView.setText(data.getExerciseDesc());


            holder.imageView.setParseFile(data.getIconImage());
            holder.imageView.loadInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] bytes, com.parse.ParseException e) {

                }
            });

            return row;
        }

        static class PlaceHolder {
            TextView numberView;
            TextView titleView;
            TextView repView;
            ParseImageView imageView;
        }

}
