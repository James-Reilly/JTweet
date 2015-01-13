package me.jreilly.JamesTweet;

import android.app.Application;
import android.content.Context;

/**
 * Created by jreilly on 1/13/15.
 */
public class TweetTweet extends Application {
    private static TweetTweet mInstance;
    private static Context mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        this.setAppContext(getApplicationContext());
    }

    public static TweetTweet getInstance(){
        return mInstance;
    }
    public static Context getAppContext() {
        return mAppContext;
    }
    public void setAppContext(Context mAppContext) {
        this.mAppContext = mAppContext;
    }
}
