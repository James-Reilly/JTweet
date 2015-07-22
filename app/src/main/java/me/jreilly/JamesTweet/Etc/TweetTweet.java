/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.jreilly.JamesTweet.Etc;

import android.app.Application;
import android.content.Context;
import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

/**
 * Created by jreilly on 1/13/15.
 */
public class TweetTweet extends Application {
    private static TweetTweet mInstance;
    private static Context mAppContext;

    private static final String TWITTER_KEY = "";
    private static final String TWITTER_SECRET = "";

    @Override
    public void onCreate() {
        super.onCreate();

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);


        Fabric.with(this, new Twitter(authConfig));
        Fabric.with(this, new Crashlytics());
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
