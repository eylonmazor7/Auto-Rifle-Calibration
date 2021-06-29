package org.tensorflow.lite.examples.detection.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.tensorflow.lite.examples.detection.helpers.SingleRange;

import java.util.ArrayList;
import java.util.List;

//singelton to be continue
public class ShooterRangesDB extends SQLiteOpenHelper {

    private final static String shooterTable = "SHOOTER_TABLE1";
    private final static String colId = "NUMBER";
    private final static String colDate = "DATE";
    private final static String colGrade = "GRADE";
    private final static String colResult = "RESULT";

    public ShooterRangesDB(Context context, String shooterName) {
        super(context, shooterName, null, 1);
    }

    //create table at first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        String create = "CREATE TABLE " + shooterTable + " (" + colId + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                colGrade + " FLOAT, " + colResult + " TEXT)";
        db.execSQL(create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public boolean addRange(SingleRange singleRange) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(colResult, singleRange.getResult());
        cv.put(colGrade, singleRange.getGrade());

        long su = db.insert(shooterTable, null,cv);

        return su != -1;
    }

    public List<SingleRange> getAllRanges() {
        List<SingleRange> retList = new ArrayList<>();

        //now, get data from db
        SQLiteDatabase db = this.getReadableDatabase();
        String sqlText = "SELECT * FROM " + shooterTable;
        Cursor cursor = db.rawQuery(sqlText, null);

        if(cursor.moveToFirst())
        {
            do {
                int id = cursor.getInt(0);
                float grade = cursor.getFloat(1);
                String result = cursor.getString(2);
                SingleRange sg = new SingleRange(id, grade, result);
                retList.add(sg);
            } while(cursor.moveToNext());
        }

        //close the both the cursor and db when done
        db.close();
        cursor.close();
        return retList;
    }
}

