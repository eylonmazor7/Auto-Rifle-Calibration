package org.tensorflow.lite.examples.detection.helpers;

import org.tensorflow.lite.examples.detection.activity.ProjectActivity;

import java.util.ArrayList;

public class NpmTable {
    private static NpmTable single_instance = null; //singleton
    private static final ArrayList<Double> rangeFromNPR = new ArrayList<>(); //0.5,1,1.5......
    private static final int[][] bigArray = new int[][]{
            { 1, 2, 4, 5, 6, 8, 9, 10, 11, 12, 14, 15, 16, 17, 19, 20, 21, 22, 24, 25 } ,
            { 1, 1, 2, 2, 3, 3, 4, 4,   5,  6,  6,  7,  7,  8,  8,  9,  9, 10, 10, 11 }};

    private NpmTable(){ // Reset all params in table
        for(double i = 0.5; i < 10.5 ; i+=0.5)
            rangeFromNPR.add(i);
    }

    public static NpmTable getInstance() {
        if (single_instance == null)
            single_instance = new NpmTable();

        return single_instance;
    }

    public int searchHowManyClickes(Double distance)
    {
        double roundToHalf = ((int)(distance*2 + 0.5))/2.0; // round to 0.5, 1, 1.5, 2 ...
        int rowOfWeaponKindInTable = 1; //default
        if(ProjectActivity.getWeapon().equals("MicroM5") || ProjectActivity.getWeapon().equals("M4M5"))
            rowOfWeaponKindInTable = 0;

        int colOfRangeInTable = rangeFromNPR.indexOf(roundToHalf);
        if(colOfRangeInTable > 19) colOfRangeInTable = 19;
        if(colOfRangeInTable <  0) colOfRangeInTable =  0;

        return bigArray[rowOfWeaponKindInTable][colOfRangeInTable];
    }
}
