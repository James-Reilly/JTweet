package me.jreilly.JamesTweet.Adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;

import java.util.List;

/**
 * Created by jreilly on 1/12/15.
 */
public class TweetDataHelper extends SQLiteOpenHelper {

    /**db version*/
    private static final int DATABASE_VERSION = 1;
    /**database name*/
    public static final String DATABASE_NAME = "home.db";

    public static final String TIMELINE_TABLE_NAME = "home";

    public static final String TABLE_QUEUE_NAME =  "queue";
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
    private String DATABASE_CREATE;


    TweetDataHelper(Context context, String dbName){

        super(context, dbName, null, DATABASE_VERSION);



    }





    @Override
    public void onCreate(SQLiteDatabase db) {
        DATABASE_CREATE = "CREATE TABLE " + TIMELINE_TABLE_NAME  + " (" + HOME_COL +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + UPDATE_COL + " TEXT, " + NAME_COL + " Text, "
                + USER_COL + " TEXT, " + TIME_COL + " INTEGER, " + USER_IMG + " TEXT, " + MEDIA_COL +
                " TEXT, " + FAVORITE_COL + " INTEGER, " + RETWEET_COL  + " INTEGER, " + RETWEETED_COL +
                " INTEGER, " + ORIGINAL_COL + " TEXT);";
        db.execSQL(DATABASE_CREATE);
        DATABASE_CREATE = "CREATE TABLE " + TABLE_QUEUE_NAME  + " (" + HOME_COL +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + UPDATE_COL + " TEXT, " + NAME_COL + " Text, "
                + USER_COL + " TEXT, " + TIME_COL + " INTEGER, " + USER_IMG + " TEXT, " + MEDIA_COL +
                " TEXT, " + FAVORITE_COL + " INTEGER, " + RETWEET_COL  + " INTEGER, " + RETWEETED_COL +
                " INTEGER, " + ORIGINAL_COL + " TEXT);";
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
            String createdTime = tweet.createdAt;

            if(tweet.retweetedStatus != null) {
                tweet = tweet.retweetedStatus;
                retweeted = 1;
            }
            homeValues.put(HOME_COL, tweet.id);
            homeValues.put(UPDATE_COL, tweet.text);
            homeValues.put(NAME_COL, tweet.user.name);
            homeValues.put(USER_COL, tweet.user.screenName);
            homeValues.put(TIME_COL, createdTime);
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

    public Callback<List<Tweet>> callBackMaker(final int mMaxItems,
                                               final RecyclerView mRecyclerView,
                                               final Cursor mCursor,
                                               final SQLiteDatabase mTimelineDB,
                                               final TweetDataHelper mHelper,
                                               final SwipeRefreshLayout mSwipeRefreshLayout
                                               ){
        final int position = mCursor.getPosition();
        return new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> listResult) {

                long numEntries = DatabaseUtils.queryNumEntries(mTimelineDB, "home");
                List<Tweet> list = listResult.data.subList(0,listResult.data.size());

                for (Tweet t : list) {
                    try {
                        ContentValues tweetValues = mHelper.getValues(t);
                        mTimelineDB.insertOrThrow("home", null, tweetValues);
                        Log.v("NEW CONTEXT", "Added Tweet Tweets!");

                    }catch (Exception te) { Log.e("NEW CONTEXT", "Exception: " + te);}

                }



                int rowLimit = 50;

                if(DatabaseUtils.queryNumEntries(mTimelineDB, "home") > rowLimit) {
                    String deleteQuery = "DELETE FROM home WHERE "+BaseColumns._ID+" NOT IN " +
                            "(SELECT "+BaseColumns._ID+" FROM home ORDER BY "+"update_time DESC " +
                            "limit "+rowLimit+")";
                    mTimelineDB.execSQL(deleteQuery);
                    Log.v("NEW CONTEXT", "Deleteing Tweets!");
                }
                mRecyclerView.getAdapter().notifyDataSetChanged();
                if (mSwipeRefreshLayout != null){
                    mSwipeRefreshLayout.setRefreshing(false);
                }else{
                    mRecyclerView.smoothScrollToPosition(position);
                }

                Log.v("NEW CONTEXT", "All done!");



            }

            @Override
            public void failure(TwitterException e) {
                Log.e("NEW CONTEXT", "Exception " + e);

            }
        };
    }
}
