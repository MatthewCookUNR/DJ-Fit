package com.example.dj_fit;

import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class workoutDay
{
    //Class variables
    private String day;
    private TextView exerTitle, viewTitle, minTitle, maxTitle, dayView;
    private TableLayout myTable;
    private int dayOrderIndex;
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

    public TextView getExerTitle() {
        return exerTitle;
    }

    public void setExerTitle(TextView exerTitle) {
        this.exerTitle = exerTitle;
    }

    public TextView getViewTitle() {
        return viewTitle;
    }

    public void setViewTitle(TextView viewTitle) {
        this.viewTitle = viewTitle;
    }

    public TextView getMinTitle() {
        return minTitle;
    }

    public void setMinTitle(TextView minTitle) {
        this.minTitle = minTitle;
    }

    public TextView getMaxTitle() {
        return maxTitle;
    }

    public void setMaxTitle(TextView maxTitle) {
        this.maxTitle = maxTitle;
    }

    public TableLayout getMyTable() {
        return myTable;
    }

    public void setMyTable(TableLayout myTable) {
        this.myTable = myTable;
    }

    public TextView getDayView() {
        return dayView;
    }

    public void setDayView(TextView dayView) {
        this.dayView = dayView;
    }

    public int getDayOrderIndex() {
        return dayOrderIndex;
    }

    public void setDayOrderIndex(int dayOrderIndex) {
        this.dayOrderIndex = dayOrderIndex;
    }

    public void clearViewVideosList()
    {
        this.viewVideosList.clear();
    }

    public void addViewVideos (ArrayList<String> viewVideos)
    {
        this.viewVideosList.add(viewVideos);
    }

    //Function removes the current views from activity
    public void destroyViews()
    {

        for(int i = 0; i < exercise.size(); i++)
        {
            exercise.get(i).setVisibility(View.GONE);
            minWeight.get(i).setVisibility(View.GONE);
            maxWeight.get(i).setVisibility(View.GONE);
        }
        exerTitle.setVisibility(View.GONE);
        minTitle.setVisibility(View.GONE);
        maxTitle.setVisibility(View.GONE);
        viewTitle.setVisibility(View.GONE);
        dayView.setVisibility(View.GONE);
        myTable.setVisibility(View.GONE);
    }
}
