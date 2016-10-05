package com.jackrabbitmobile.toned;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jackrabbitmobile.toned.model.Product;
import com.jackrabbitmobile.toned.model.Workout;
import com.jackrabbitmobile.toned.util.NetworkConnectionUtil;
import com.jackrabbitmobile.toned.view.CircleImageView;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bolts.Continuation;
import bolts.Task;


public class ProfileActivity extends CenteredAppCompat
        implements BillingUtil.Callbacks  {

    private static final int _ID = 0;
    private static final int DISPLAY_NAME_PRIMARY = 1;
    private static final int LOOKUP_KEY = 2;
    private static final int PHOTO_THUMBNAIL_URI = 3;
    private Uri mContactUri;

    private static final int REQUEST_CREATE_PROFILE = 0;
    private CircleImageView mCircle;
    private TextView mName;

    TextView tvNoBookmarks;
    private int mDescriptionHeight;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CREATE_PROFILE: {
                    mContactUri = data.getData();
                } break;
            }
        }
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mName = (TextView) findViewById(R.id.profile_name);
        mCircle = (CircleImageView) findViewById(R.id.profile_image);

        // Sets the columns to retrieve for the user profile
        String[] mProjection = new String[]
                {
                        ContactsContract.Profile._ID,
                        ContactsContract.Profile.DISPLAY_NAME_PRIMARY,
                        ContactsContract.Profile.LOOKUP_KEY,
                        ContactsContract.Profile.PHOTO_THUMBNAIL_URI,
                };

        // Retrieves the profile from the Contacts Provider
        Cursor mProfileCursor = getContentResolver().query(
                ContactsContract.Profile.CONTENT_URI,
                mProjection, null, null, null);

        if (mContactUri != null) {
            // Retrieves the profile from the Contacts Provider
            mProfileCursor = getContentResolver().query(mContactUri,
                    mProjection, null, null, null);

        }

        if (mProfileCursor.moveToFirst()) {
            CursorWrapper wrapper = new CursorWrapper(mProfileCursor);

            Uri mContactUri = ContactsContract.Contacts.getLookupUri(
                    wrapper.getLong(_ID),
                    wrapper.getString(LOOKUP_KEY)
            );

            String thumbnailLocation = wrapper.getString(PHOTO_THUMBNAIL_URI);
            Bitmap thumbnail = (thumbnailLocation == null) ?
                    BitmapFactory.decodeResource(getResources(), R.drawable.user_female_filled)
                    : loadContactPhotoThumbnail(thumbnailLocation);

            mName.setText(wrapper.getString(DISPLAY_NAME_PRIMARY));
            wrapper.close();
            mProfileCursor.close();

            mCircle.assignContactUri(mContactUri);
            mCircle.setImageBitmap(thumbnail);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Profile not found")
                    .setMessage("Would you like to create the device profile now?")
                    .setPositiveButton("Create profile", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            createUserProfile();
                        }
                    })
                    .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mCircle.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    createUserProfile();
                                }
                            });
                        }
                    })
                    .show();
        }
    }

    private void createUserProfile() {
        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT,
                ContactsContract.Profile.CONTENT_URI);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivityForResult(intent, REQUEST_CREATE_PROFILE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        startService(new Intent(this, NetworkConnectionUtil.class));

        tvNoBookmarks = (TextView) findViewById(R.id.no_bookmarks_message);

        mDescriptionHeight = (int) getResources().getDimension(R.dimen.workout_description_height);
        getWorkouts();

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            setActionBarTitle(getResources().getString(R.string.title_activity_profile));
        }
    }

    @Override
    public void onAvailableSubscriptionsLoaded(ArrayList<Product> subscriptions) {

    }

    @Override
    public void onActiveSubscriptionsLoaded(ArrayList<Product> subscriptions) {

    }

    /**
     *
     */
    private void getWorkouts(){
        ParseQuery<Workout> query = ParseQuery.getQuery(Workout.class);
        query.findInBackground(new FindCallback<Workout>() {
            public void done(List<Workout> parseObjects, com.parse.ParseException e) {

                SharedPreferences preferences =
                        getSharedPreferences(Workout.PREFERENCE_BOOKMARKED, Context.MODE_PRIVATE);

                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.bookmarked_workouts);

                if (parseObjects.size() > 0) {
                    findViewById(R.id.workout_list).setVisibility(View.VISIBLE);
                    for (Workout workout : parseObjects) {
                        final String workoutId = workout.getObjectId();
                        if (!preferences.getBoolean(workoutId, false)) continue;
                        final View card = View.inflate(ProfileActivity.this, R.layout.row_workout, null);
                        card.findViewById(R.id.paywall).setVisibility(View.GONE);
                        TextView title = (TextView) card.findViewById(R.id.title_text_workout_row);
                        TextView description = (TextView) card.findViewById(R.id.description_text_workout_row);
                        ParseImageView image = (ParseImageView) card.findViewById(R.id.image_workout_row);
                        title.setText(workout.getTitle());
                        description.setText(workout.getComment());
                        image.setParseFile(workout.getFullImage());
                        Picasso.with(ProfileActivity.this)
                                .load(workout.getFullImage().getUrl())
                                .error(R.drawable.l_trash)
                                .into(image);

                        card.findViewById(R.id.learn_more_button_row).setVisibility(View.GONE);
                        card.findViewById(R.id.remove_bookmark).setVisibility(View.VISIBLE);
                        card.findViewById(R.id.remove_bookmark).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SharedPreferences preferences =
                                        getSharedPreferences(Workout.PREFERENCE_BOOKMARKED, Context.MODE_PRIVATE);
                                boolean bookmarked = preferences.getBoolean(workoutId, false);
                                TextView remove = (TextView) v;
                                if (bookmarked) {
                                    preferences.edit().putBoolean(workoutId, false).apply();
                                    card.setAlpha(0.5f);
                                    remove.setText("UNDO");
                                } else {
                                    preferences.edit().putBoolean(workoutId, true).apply();
                                    card.setAlpha(1.0f);
                                    remove.setText("REMOVE");
                                }
                            }
                        });

                        final Workout w = workout;
                        card.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent();
                                intent.setClass(ProfileActivity.this, WorkoutDetailActivity.class);
                                intent.putExtra(WorkoutDetailFragment.EXTRA_WORKOUT_ID, w.getObjectId());
                                intent.putExtra(WorkoutDetailFragment.EXTRA_WORKOUT_NAME, w.getTitle());
                                intent.putExtra(WorkoutDetailFragment.EXTRA_START_WORKOUT, true);
                                startActivity(intent);
                            }
                        });

                        linearLayout.addView(card);
                    }
                }

                if (linearLayout.getChildCount() == 0) {
                    findViewById(R.id.loading_bookmarks).setVisibility(View.GONE);
                    findViewById(R.id.no_bookmarks_message).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.no_workouts).setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Load a contact photo thumbnail and return it as a Bitmap,
     * resizing the image to the provided image dimensions as needed.
     * @param photoData photo ID Prior to Honeycomb, the contact's _ID value.
     * For Honeycomb and later, the value of PHOTO_THUMBNAIL_URI.
     * @return A thumbnail Bitmap, sized to the provided width and height.
     * Returns null if the thumbnail is not found.
     */
    private Bitmap loadContactPhotoThumbnail(String photoData) {
        // Creates an asset file descriptor for the thumbnail file.
        AssetFileDescriptor afd = null;
        // try-catch block for file not found
        try {
            // Creates a holder for the URI.
            Uri thumbUri;
            // If Android 3.0 or later
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                // Sets the URI from the incoming PHOTO_THUMBNAIL_URI
                thumbUri = Uri.parse(photoData);
            } else {
                // Prior to Android 3.0, constructs a photo Uri using _ID
                /*
                 * Creates a contact URI from the Contacts content URI
                 * incoming photoData (_ID)
                 */
                final Uri contactUri = Uri.withAppendedPath(
                        ContactsContract.Contacts.CONTENT_URI, photoData);
                /*
                 * Creates a photo URI by appending the content URI of
                 * Contacts.Photo.
                 */
                thumbUri =
                        Uri.withAppendedPath(
                                contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
            }

        /*
         * Retrieves an AssetFileDescriptor object for the thumbnail
         * URI
         * using ContentResolver.openAssetFileDescriptor
         */
            afd = getContentResolver().
                    openAssetFileDescriptor(thumbUri, "r");
        /*
         * Gets a file descriptor from the asset file descriptor.
         * This object can be used across processes.
         */
            FileDescriptor fileDescriptor = afd.getFileDescriptor();
            // Decode the photo file and return the result as a Bitmap
            // If the file descriptor is valid
            if (fileDescriptor != null) {
                // Decodes the bitmap
                return BitmapFactory.decodeFileDescriptor(
                        fileDescriptor, null, null);
            }
            // If the file isn't found
        } catch (FileNotFoundException e) {
            /*
             * Handle file not found errors
             */
            // In all cases, close the asset file descriptor
        } finally {
            if (afd != null) {
                try {
                    afd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
