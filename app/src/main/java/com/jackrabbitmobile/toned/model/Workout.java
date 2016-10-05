package com.jackrabbitmobile.toned.model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.util.HashMap;
import java.util.List;

@ParseClassName("Workout")
public class Workout extends ParseObject{

    public static final String PREFERENCE_BOOKMARKED =
            "com.jackrabbitmobile.toned.PREFERENCE_BOOKMARKED_WORKOUTS";

    public Workout() { }

    public String getTitle(){ return getString("title");}

    public String getComment(){ return getString("comment");}

    public ParseFile getFullImage(){ return getParseFile("fullImage");}

    public List<Exercise> getExerciseList() { return getList("exerciseList");}

    public List<HashMap<String, Integer>> getExerciseConfigList() { return getList("exerciseConfigList");}

    public ParseFile getVideo() {
        return getParseFile("video");
    }
}