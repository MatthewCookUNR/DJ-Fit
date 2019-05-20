package com.example.dj_fit;

import java.util.ArrayList;

//Class used to organize data stored in each row of the workout routine table
public class workoutRow
{
    private String exercise, minWeight, maxWeight;
    private ArrayList<String> videoList;

    public workoutRow()
    {

    }

    public workoutRow(workoutRow row) {
        this.exercise = row.exercise;
        this.minWeight = row.minWeight;
        this.maxWeight = row.maxWeight;
        this.videoList = row.videoList;
    }

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
        return maxWeight;
    }

    public void setMaxWeight(String maxWeight) {
        maxWeight = maxWeight;
    }

    public ArrayList<String> getVideoList() {
        return videoList;
    }

    public void setVideoList(ArrayList<String> videoList) {
        this.videoList = videoList;
    }

}
