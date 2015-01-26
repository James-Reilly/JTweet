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
    /**twitter name*/
    private static final String NAME_COL = "user_name";
    /**twitter screen name*/
    private static final String USER_COL = "user_screen";
    /**time tweeted*/
    private static final String TIME_COL = "update_time";
    /**user profile image*/
    private static final String USER_IMG = "user_img";
    /**tweet media url*/
    private static final String MEDIA_COL = "update_media";
    /**favorite boolean*/
    private static final String FAVORITE_COL = "update_favorite";
    /**retweet boolean*/
    private static final String RETWEET_COL = "update_retweet";
    /**if current tweet is retweed boolean*/
    private static final String RETWEETED_COL = "update_retweeted";
    /**if current tweet is retweed boolean*/
    private static final String ORIGINAL_COL = "update_original";




    //Database creation string
    private static final String DATABASE_CREATE = "CREATE TABLE home (" + HOME_COL +
            " INTEGER NOT NULL PRIMARY KEY, " + UPDATE_COL + " TEXT, " + NAME_COL + " Text, "
            + USER_COL + " TEXT, " + TIME_COL + " INTEGER, " + USER_IMG + " TEXT, " + MEDIA_COL +
            " TEXT, " + FAVORITE_COL + " INTEGER, " + RETWEET_COL  + " INTEGER, " + RETWEETED_COL +
            " INTEGER, " + ORIGINAL_COL + " TEXT);";


    TweetDataHelper(Context context){

        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }





    @Override
    public void onCreate(SQLiteDatabase db) {

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

        String original = tweet.user.screenName;

        int retweeted = 0;
        try {
            homeValues.put(TIME_COL, tweet.createdAt);
            if(tweet.retweetedStatus != null) {
                tweet = tweet.retweetedStatus;
                retweeted = 1;
            }
            homeValues.put(HOME_COL, tweet.id);
            homeValues.put(UPDATE_COL, tweet.text);
            homeValues.put(NAME_COL, tweet.user.name);
            homeValues.put(USER_COL, tweet.user.screenName);

            homeValues.put(USER_IMG, tweet.user.profileImageUrl);
            if (tweet.entities != null && (tweet.entities.media != null)){
                homeValues.put(MEDIA_COL, tweet.entities.media.get(0).mediaUrl);
            } else {
                homeValues.put(MEDIA_COL, "null");
            }
            homeValues.put(FAVORITE_COL, tweet.favorited);
            homeValues.put(RETWEET_COL, tweet.retweeted);
            homeValues.put(RETWEETED_COL, retweeted);
            homeValues.put(ORIGINAL_COL, original);




        } catch (Exception te){ Log.e("TweetDataHelper", te.getMessage()); }




        return homeValues;
    }
}
