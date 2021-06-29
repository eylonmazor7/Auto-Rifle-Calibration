package org.tensorflow.lite.examples.detection.helpers;

import android.util.Log;

import org.tensorflow.lite.examples.detection.activity.ProjectActivity;


public class SingleRange {
    private int id = -1;
    private float grade = 0.0f;
    private String result = "";
    private static final Float pixelToCM = 22.18f;
    private static final Pair<Float,Float> targetCoordinates = whichWeapon();
    private Pair<Float,Float> NPM;
    private final NpmTable npmTable = NpmTable.getInstance();

    public SingleRange() {
        this.NPM = ProjectActivity.singleRangeResult;
        this.grade = calcGrade();
        calculateResult();
    }

    private float calcGrade() {
            float finalGrade = 0.f;
            int bonus = ProjectActivity.bonus;
            if(bonus == 2)
                return finalGrade;

            double maxDistance = ProjectActivity.maxDistance;

            double roundToHalf = ((int)(maxDistance*2 + 0.5))/2.0; // round to 0.5, 1, 1.5, 2 ...
            finalGrade = 100;
            finalGrade -= roundToHalf>2 ? ((roundToHalf-2.0)/0.5)*2 : 0;

            if(bonus == 1) //if you have a runner -> you cant reach more than 70.
                return Math.min(finalGrade,70);

            return finalGrade > 0 ? finalGrade : 0;
    }

    public SingleRange(int id, float grade, String result) //for print the result on list
    {
        this.id = id;
        this.grade = grade;
        this.result = result;
    }

    private void calculateResult() {
        if(this.NPM.getL() == 0){
            this.result = "not enough hits - please shoot to Tumulus(רוג'ום).";
            this.grade = 0;
            return;
        }

        float clickHorizontal = this.NPM.getL() - targetCoordinates.getL();
        float clickVertical = this.NPM.getR() - targetCoordinates.getR();

        String rightOrLeft = clickHorizontal > 0 ? "left" : "right";
        String upOrDown = clickVertical > 0 ? "up" : "down";

        int cmToClicksRL = npmTable.searchHowManyClickes((double) (Math.abs(clickHorizontal / pixelToCM)));
        int cmToClicksUD = npmTable.searchHowManyClickes((double) (Math.abs(clickVertical / pixelToCM)));

        this.result = "You need to move: " + cmToClicksRL + " clicks to " + rightOrLeft + " and "
                + cmToClicksUD + " clicks to " + upOrDown;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getGrade() {
        return this.grade;
    }

    public String getResult() {
        return this.result;
    }

    private static Pair<Float,Float> whichWeapon(){
        Pair<Float,Float> res = new Pair<Float,Float>(208.0f, 208.0f); //black circle

        switch (ProjectActivity.getWeapon()){
            case("M16ShortMafro") : res.setR(208.0f + pixelToCM);
                break;
            case("M4Mafro") :
            case("M4M5") :
                res.setR(208.0f - 1.4f*pixelToCM);
                break;
            case("MicroM5") :
            case("MicroMafro") :
                res.setR(208.0f - 2.4f*pixelToCM);
                break;
            default: ;
        }

        return res;
    }

    @Override
    public String toString() {
        return "" + this.id + ". Grade = " + this.grade +"\n  Result = " + this.result;
    }
}