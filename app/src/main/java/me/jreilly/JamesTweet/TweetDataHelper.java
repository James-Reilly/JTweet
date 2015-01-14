package me.jreilly.JamesTweet;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.text.format.DateFormat;
import android.util.Log;

import com.twitter.sdk.android.core.models.Tweet;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by jreilly on 1/12/15.
 */
public class TweetDataHelper extends SQLiteOpenHelper {

    /**db version*/
    private static final int DATABASE_VERSION = 1;
    /**database name*/
    private static final String DATABASE_NAME = "home.db";
    /**ID column*/
    private static final String HOME_COL = BaseColumns._ID;
    /**tweet text*/
    private static final String UPDATE_COL = "update_text";
    /**twitter screen name*/
    private static final String USER_COL = "user_screen";
    /**time tweeted*/
    private static final String TIME_COL = "update_time";
    /**user profile image*/
    private static final String USER_IMG = "user_img";

    //Database creation string
    private static final String DATABASE_CREATE = "CREATE TABLE home (" + HOME_COL +
            " INTEGER NOT NULL PRIMARY KEY, " + UPDATE_COL + " TEXT, " + USER_COL +
            " TEXT, " + TIME_COL + " INTEGER, " + USER_IMG + " TEXT);";

    TweetDataHelper(Context context){

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.e("TweetDataHelper", "Creating DB!");
    }





    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e("TweetDataHelper", "Creating DB!");
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS home");
        db.execSQL("VACUUM");
        onCreate(db);
    }

    public static ContentValues getValues(Tweet tweet){
        ContentValues homeValues = new ContentValues();

        //get the values for the database
        try {
            homeValues.put(HOME_COL, tweet.id);
            homeValues.put(UPDATE_COL, tweet.text);
            homeValues.put(USER_COL, tweet.user.screenName);

            /*
            String dateStr = tweet.createdAt;
            Date date = new SimpleDateFormat("MMM", Locale.ENGLISH).parse(dateStr.substring(5,9));
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int month = cal.get(Calendar.MONTH);
            int year = Integer.parseInt(dateStr.substring(dateStr.length()-5));

            int hour = 1;
            int minute = 1;
            int second = 1;


            GregorianCalendar calendar = new GregorianCalendar()
*/
            homeValues.put(TIME_COL, tweet.createdAt);
            homeValues.put(USER_IMG, tweet.user.profileImageUrl);
        } catch (Exception te){ Log.e("TweetDataHelper", te.getMessage()); }

        return homeValues;
    }
}
