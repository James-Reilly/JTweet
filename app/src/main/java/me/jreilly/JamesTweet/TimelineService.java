package me.jreilly.JamesTweet;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.List;

import io.fabric.sdk.android.Fabric;
import me.jreilly.JamesTweet.TweetDataHelper;

/**
 * Created by jreilly on 1/12/15.
 */
public class TimelineService extends Service {

    //DataBase objects
    private TweetDataHelper tweetHelper;
    private SQLiteDatabase tweetDB;

    //Generics
    private TimelineUpdater tweetUpdater;
    private SharedPreferences tweetPrefs;
    private Handler tweetHandler;
    private static int mins = 5;
    private static long FETCH_DELAY =  (60*1000);

    private String LOG_TAG = "TimelineService";

    private static final String TWITTER_KEY = "u3rtb2wblcZAS4SxsSwx4fcb5";
    private static final String TWITTER_SECRET = "NoT5fueZXHwRRnka9l0glPyojXtw64z5bnOd0RJlObSEKfkH4H";

    @Override
    public void onCreate(){
        super.onCreate();
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);

        Fabric.with(this, new Twitter(authConfig));
        tweetHelper = new TweetDataHelper(this, TweetDataHelper.DATABASE_NAME);
        tweetDB = tweetHelper.getWritableDatabase();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("Timeline Service", "Created Service!");
        super.onStart(intent, flags);
        tweetHandler = new Handler();
        tweetUpdater = new TimelineUpdater();
        tweetHandler.post(tweetUpdater);
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        tweetHandler.removeCallbacks(tweetUpdater);
        tweetDB.close();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class TimelineUpdater implements Runnable {

        @Override
        public void run() {

            Log.e(LOG_TAG, "Getting Tweets!");
            final StatusesService service = Twitter.getApiClient().getStatusesService();
            service.homeTimeline(50, null, null, null, null, null, true, new Callback<List<Tweet>>() {
                @Override
                public void success(Result<List<Tweet>> listResult) {
                    boolean statusChanges = false;
                    try {
                        for (Tweet t : listResult.data) {
                            ContentValues tweetValues = tweetHelper.getValues(t);

                            tweetDB.insertOrThrow("queue", null, tweetValues);
                            statusChanges = true;

                        }
                    }catch (Exception te) { Log.e(LOG_TAG, "Exception: " + te);
                    }
                    if(statusChanges){
                        sendBroadcast(new Intent("TWITTER_UPDATES"));
                    }

                    tweetHandler.postDelayed(tweetUpdater,FETCH_DELAY);
                }

                @Override
                public void failure(TwitterException e) {
                    Log.e(LOG_TAG, "Exception " + e);
                    tweetHandler.postDelayed(tweetUpdater,FETCH_DELAY);
                }
            });


        }
    }
}
