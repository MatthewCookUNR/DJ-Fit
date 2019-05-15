package com.example.dj_fit;

//Class used to organize data stored in each table of the workout routine
public class workoutTable
{
    private String day;
    private workoutRow [] rows;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public workoutRow[] getRows() {
        return rows;
    }

    public void setRows(workoutRow[] rows) {
        this.rows = rows;
    }
}
