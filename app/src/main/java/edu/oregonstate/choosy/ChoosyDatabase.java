package edu.oregonstate.choosy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Samuel on 3/19/2018.
 */

public class ChoosyDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "choosy.db";
    private static final int DATABASE_VERSION = 1;

    public ChoosyDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create Comparisons table
        final String SQL_CREATE_COMPARISONS_TABLE =
                "CREATE TABLE " + ChoosyContract.Comparisons.TABLE_NAME +
                        " (" + ChoosyContract.Comparisons._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        ChoosyContract.Comparisons.COLUMN_FIRST + " TEXT NOT NULL, " +
                        ChoosyContract.Comparisons.COLUMN_SECOND + " TEXT NOT NULL, " +
                        ChoosyContract.Comparisons.COLUMN_TIMESTAMP +
                        " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ");";
        //Create Factors table
        final String SQL_CREATE_FACTORS_TABLE =
                "CREATE TABLE " + ChoosyContract.Factors.TABLE_NAME +
                        " (" + ChoosyContract.Factors._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        ChoosyContract.Factors.COLUMN_NAME + " TEXT NOT NULL, " +
                        ChoosyContract.Factors.COLUMN_COMP + " TEXT NOT NULL, " +
                        ChoosyContract.Factors.COLUMN_PRO + " TEXT NOT NULL, " +
                        ChoosyContract.Factors.COLUMN_WEIGHT + " INTEGER NOT NULL, " +
                        ChoosyContract.Factors.COLUMN_TIMESTAMP +
                        " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ");";

        //Create tables
        db.execSQL(SQL_CREATE_COMPARISONS_TABLE);
        db.execSQL(SQL_CREATE_FACTORS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int old, int cur) {
        //do nothing
    }

    public boolean addDecision(DecisionUtils.decisionObject dec) {
        SQLiteDatabase db = this.getWritableDatabase();

        //Insert
        String sqlSelection = ChoosyContract.Comparisons.COLUMN_FIRST + " = ?";
        String[] sqlSelectionArgs = { dec.firstOption };
        Cursor cursor = db.query(
                ChoosyContract.Comparisons.TABLE_NAME,
                null,
                sqlSelection,
                sqlSelectionArgs,
                null,
                null,
                null
        );
        if (cursor.getCount() == 0) {
            //if entry doesn't already exist, add entry
            ContentValues values = new ContentValues();
            values.put(ChoosyContract.Comparisons.COLUMN_FIRST, dec.firstOption);
            values.put(ChoosyContract.Comparisons.COLUMN_SECOND, dec.secondOption);
            db.insert(ChoosyContract.Comparisons.TABLE_NAME, null, values);
            Log.d("ChoosyDatabase","Added decision "+dec.firstOption+" vs "+dec.secondOption+" to database.");
            return true;
        }
        else
            Log.d("ChoosyDatabase","Decision already exists in database!");

        return false;
    }

    public boolean addFactor(DecisionUtils.factorObject factor) {
        SQLiteDatabase db = this.getWritableDatabase();

        //Insert
        String sqlSelection = ChoosyContract.Factors.COLUMN_NAME + " = ? AND " + ChoosyContract.Factors.COLUMN_COMP + " = ?";
        String[] sqlSelectionArgs = { factor.name, factor.comp };
        Cursor cursor = db.query(
                ChoosyContract.Factors.TABLE_NAME,
                null,
                sqlSelection,
                sqlSelectionArgs,
                null,
                null,
                null
        );
        if (cursor.getCount() == 0) {
            //if factor entry doesn't already exist, add entry
            ContentValues values = new ContentValues();
            values.put(ChoosyContract.Factors.COLUMN_NAME, factor.name);
            values.put(ChoosyContract.Factors.COLUMN_COMP, factor.comp);
            values.put(ChoosyContract.Factors.COLUMN_PRO, factor.pro);
            values.put(ChoosyContract.Factors.COLUMN_WEIGHT, factor.weight);
            db.insert(ChoosyContract.Factors.TABLE_NAME, null, values);
            Log.d("ChoosyDatabase","Added factor "+ factor.name +" of decision "+ factor.comp +" to database.");
            return true;
        }
        else
            Log.d("ChoosyDatabase","Factor already exists for decision "+ factor.comp + " in database!");

        return false;
    }

    public ArrayList<DecisionUtils.decisionObject> getDecisions() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(ChoosyContract.Comparisons.TABLE_NAME,
                null, null, null, null,
                null, ChoosyContract.Comparisons.COLUMN_TIMESTAMP + " DESC");

        ArrayList<DecisionUtils.decisionObject> vals = new ArrayList<>();
        DecisionUtils.decisionObject val;
        int index;
        while (cursor.moveToNext()) {
            String first = "";
            String second = "";

            index = cursor.getColumnIndex(ChoosyContract.Comparisons.COLUMN_FIRST);
            first = cursor.getString(index);

            index = cursor.getColumnIndex(ChoosyContract.Comparisons.COLUMN_SECOND);
            second = cursor.getString(index);

            val = new DecisionUtils.decisionObject(first, second);
            vals.add(val);

            Log.d("ChoosyDatabase","Retrieved "+val.firstOption+" vs "+val.secondOption+" from database.");
        }

        return vals;
    }

    public ArrayList<DecisionUtils.factorObject> getFactors(String decision) {
        //Get all factors for specified comparison name / decision
        SQLiteDatabase db = this.getReadableDatabase();
        String sqlSelection = ChoosyContract.Factors.COLUMN_COMP + " = ?";
        String[] sqlSelectionArgs = { decision };

        Cursor cursor = db.query(ChoosyContract.Factors.TABLE_NAME,
                null, sqlSelection, sqlSelectionArgs, null,
                null, ChoosyContract.Factors.COLUMN_TIMESTAMP + " DESC");

        ArrayList<DecisionUtils.factorObject> vals = new ArrayList<>();
        DecisionUtils.factorObject val;
        int index;
        while (cursor.moveToNext()) {
            String name = "";
            String comp = "";
            int pro = -1;
            int weight = -1;

            index = cursor.getColumnIndex(ChoosyContract.Factors.COLUMN_NAME);
            name = cursor.getString(index);

            index = cursor.getColumnIndex(ChoosyContract.Factors.COLUMN_COMP);
            comp = cursor.getString(index);

            index = cursor.getColumnIndex(ChoosyContract.Factors.COLUMN_PRO);
            pro = cursor.getInt(index);

            index = cursor.getColumnIndex(ChoosyContract.Factors.COLUMN_WEIGHT);
            weight = cursor.getInt(index);

            val = new DecisionUtils.factorObject(name, comp, pro, weight);
            vals.add(val);

            Log.d("ChoosyDatabase","Retrieved "+val.name+" factor of decision "+val.comp+" from database.");
        }

        return vals;
    }

    public void deleteDecision(String dec) {
        SQLiteDatabase db = this.getWritableDatabase();
        //Delete decision
        String where = ChoosyContract.Comparisons.COLUMN_FIRST + " = ?";
        String[] SQLwhereArgs = { dec };
        db.delete(ChoosyContract.Comparisons.TABLE_NAME, where, SQLwhereArgs);
        //Delete factors
        where = ChoosyContract.Factors.COLUMN_COMP + " = ?";
        db.delete(ChoosyContract.Factors.TABLE_NAME, where, SQLwhereArgs);
    }

    public void deleteFactor(String name, String decName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = ChoosyContract.Factors.COLUMN_COMP + " = ? AND " + ChoosyContract.Factors.COLUMN_NAME + " = ?";
        String[] SQLwhereArgs = { name, decName };
        db.delete(ChoosyContract.Factors.TABLE_NAME, where, SQLwhereArgs);
    }
}
