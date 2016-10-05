package com.jackrabbitmobile.toned.model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

/**
 * This is the class which Parse uses for ParseQuery
 */
@ParseClassName("Exercise")
public class Exercise extends ParseObject{

    String exerciseDesc = "";

    public Exercise() { }

    public String getTitle(){ return getString("title");}

    public ParseFile getFullImage(){  return getParseFile("fullImage"); }

    public ParseFile getIconImage() { return getParseFile("iconImage"); }

    public ParseFile getVideoFile() { return getParseFile("video");}

    public void setExerciseDesc(String exerciseDesc) { this.exerciseDesc = exerciseDesc;}

    public String getExerciseDesc() {
        return exerciseDesc;
    }

    public String getYoutubeID() { return getString("youtubeID");}

    public String getVideoURL() {

        ParseFile videoFile = this.getVideoFile();
        return videoFile.getUrl();
    }
}