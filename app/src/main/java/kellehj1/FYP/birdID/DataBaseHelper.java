package kellehj1.FYP.birdID;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

public class DataBaseHelper extends SQLiteOpenHelper {

    String createTable;
    String tableName;
    String jsonFilename;
    JSONArray birdList;
    Context context;

    public DataBaseHelper(@Nullable Context context, String birdType) {
        super(context, birdType.toUpperCase() + "_TABLE.db", null, 1);
        this.context = context;
        this.tableName = birdType.toUpperCase() + "_TABLE";
        this.jsonFilename = birdType + ".json";

        try {
            birdList = new JSONArray(loadJSONFromAsset(jsonFilename));
            JSONObject mask = birdList.getJSONObject(0);
            createTable = mask.getString("DESCRIPTION");

        } catch (JSONException e) {
            Log.e("BirdID", "unexpected JSON exception:", e);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
        onCreate(db);
    }

    public boolean addBirds() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            for (int i = 0; i < birdList.length(); i++) {
                ContentValues cv = new ContentValues();
                JSONObject bird = birdList.getJSONObject(i);
                Iterator<String> keys = bird.keys();

                while (keys.hasNext()) {
                    String key = keys.next();
                    cv.put(key, String.valueOf(bird.get(key)));
                }

                long insert = db.insertOrThrow(tableName, null, cv);
                if (insert == -1) {
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e("", "exception : " + e.toString());
        }
        finally {
            db.close();
        }
        return true;
    }

    // Getting contacts Count
    public int getBirdsCount() {
        String countQuery = "SELECT * FROM " + tableName;
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        try {
            count = (int) DatabaseUtils.queryNumEntries(db, tableName);
        }
        catch (Exception e) {
                Log.e("", "exception : " + e.toString());
            }
        finally {
                db.close();
            }
        return count;
    }

    public ArrayList<Integer> getAllIds() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Integer> allIds = new ArrayList<Integer>();
        try {
            String matchQuery = "SELECT * FROM " + tableName + " WHERE NAME!='MASK'";;
            Cursor cursor = db.rawQuery(matchQuery, null);
            if (cursor != null) {
                if  (cursor.moveToFirst()) {
                    do {
                        int id = cursor.getInt(0);
                        allIds.add(id);
                    }
                    while (cursor.moveToNext());
                }
                cursor.close();
            }
        }
        catch (Exception e) {
            Log.e("", "exception : " + e.toString());
        }
        finally {
            db.close();
        }
        return allIds;
    }

    public ContentValues getBirdDataFromID(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues data = new ContentValues();
        try {
            String dataQuery = "SELECT NAME, LATINNAME, IRISHNAME, DESCRIPTION FROM "
                    + tableName + " WHERE ID = " + id;
            Cursor cursor = db.rawQuery(dataQuery, null);
            if (cursor != null) {
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getColumnCount() ; i++) {
                    String column = cursor.getColumnName(i);
                    data.put(cursor.getColumnName(i), cursor.getString(i));
                }
            }
            cursor.close();
        }
        catch (Exception e) {
            Log.e("", "exception : " + e.toString());
        }
        finally {
            db.close();
        }
        return data;
    }

    public String getColouredSection(int maskSectionColour) {
        SQLiteDatabase db = this.getReadableDatabase();
        String maskSection = "";
        try {
            String maskQuery = "SELECT * FROM " + tableName + " WHERE NAME='MASK'";
            Cursor cursor = db.rawQuery(maskQuery, null);
            if (cursor != null) {
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getColumnCount() && maskSection.equals(""); i++) {
                    String column = cursor.getColumnName(i);
                    if (!column.equals("ID") && !column.equals("NAME") && !column.equals("DESCRIPTION")
                            && !column.equals("LATINNAME") && !column.equals("IRISHNAME")
                            && maskSectionColour == Color.parseColor(cursor.getString(i))) {
                        maskSection = cursor.getColumnName(i);
                    }
                }
                cursor.close();
                db.close();
            }
        }
        catch (Exception e) {
            Log.e("", "exception : " + e.toString());
        }
        finally {
            db.close();
        }
        return maskSection;
    }

    public ArrayList<Integer> getMatches(String section, int replacementColour, ArrayList<Integer> priorMatches) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Integer> matches = new ArrayList<Integer>();
        String hexColor = String.format("#%06X", (0xFFFFFF & replacementColour));
        try {
            String matchQuery = "SELECT * FROM " + tableName + " WHERE " + section + "='" + hexColor + "'";
            Cursor cursor = db.rawQuery(matchQuery, null);
            if (cursor != null) {
                if  (cursor.moveToFirst()) {
                    do {
                        int id = cursor.getInt(0);
                        matches.add(id);
                    }
                    while (cursor.moveToNext());
                }
                cursor.close();
            }
        }
        catch (Exception e) {
            Log.e("", "exception : " + e.toString());
        }
        finally {
            db.close();
        }

        if (!priorMatches.isEmpty())
            matches = intersection(matches, priorMatches);
        return matches;
    }

    public <T> ArrayList<T> intersection(ArrayList<T> list1, ArrayList<T> list2) {
        ArrayList<T> list = new ArrayList<T>();
        for (T t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
    }

    public String loadJSONFromAsset(String filename) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
