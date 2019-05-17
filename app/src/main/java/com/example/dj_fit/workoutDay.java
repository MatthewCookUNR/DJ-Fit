package com.example.dj_fit;

import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class workoutDay
{
    private String day;
    private ArrayList<String> muscleUsed = new ArrayList<>();
    private ArrayList<EditText> exercise = new ArrayList<>();
    private ArrayList<EditText> minWeight = new ArrayList<>();
    private ArrayList<EditText> maxWeight = new ArrayList<>();
    private ArrayList<ArrayList<String>> viewVideosList = new ArrayList<>();

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public ArrayList<String> getMusclesUsed() {
        return muscleUsed;
    }

    public void setMusclesUsed(ArrayList<String> musclesUsed) {
        this.muscleUsed = musclesUsed;
    }

    public void addMuscleUsed(String muscleUsed)
    {
        this.muscleUsed.add(muscleUsed);
    }

    public ArrayList<EditText> getExercise() {
        return exercise;
    }

    public void setExercise(ArrayList<EditText> exercise) {
        this.exercise = exercise;
    }

    public void addExercise(EditText exercise) {
        this.exercise.add(exercise);
    }

    public ArrayList<EditText> getMinWeight() {
        return minWeight;
    }

    public void setMinWeight(ArrayList<EditText> minWeights) {
        this.minWeight = minWeights;
    }

    public void addMinWeight(EditText minWeight)
    {
        this.minWeight.add(minWeight);
    }

    public ArrayList<EditText> getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(ArrayList<EditText> maxWeights) {
        this.maxWeight = maxWeights;
    }

    public void addMaxWeight(EditText maxWeight)
    {
        this.maxWeight.add(maxWeight);
    }

    public ArrayList<ArrayList<String>> getViewVideosList() {
        return viewVideosList;
    }

    public void setViewVideosList(ArrayList<ArrayList<String>> viewVideosList) {
        this.viewVideosList = viewVideosList;
    }

    public void clearViewVideosList()
    {
        this.viewVideosList.clear();
    }

    public void addViewVideos (ArrayList<String> viewVideos)
    {
        this.viewVideosList.add(viewVideos);
    }
}
