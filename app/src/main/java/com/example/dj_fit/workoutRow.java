package com.example.dj_fit;

import java.util.ArrayList;

//Class used to organize data stored in each row of the workout routine table
public class workoutRow
{
    private String exercise, minWeight, MaxWeight;
    private ArrayList<String> videoList;

    public String getExercise() {
        return exercise;
    }

    public void setExercise(String exercise) {
        this.exercise = exercise;
    }

    public String getMinWeight() {
        return minWeight;
    }

    public void setMinWeight(String minWeight) {
        this.minWeight = minWeight;
    }

    public String getMaxWeight() {
        return MaxWeight;
    }

    public void setMaxWeight(String maxWeight) {
        MaxWeight = maxWeight;
    }

    public ArrayList<String> getVideoList() {
        return videoList;
    }

    public void setVideoList(ArrayList<String> videoList) {
        this.videoList = videoList;
    }

}
