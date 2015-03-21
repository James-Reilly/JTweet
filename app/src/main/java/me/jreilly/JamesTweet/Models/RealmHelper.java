/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.jreilly.JamesTweet.Models;

import android.app.Activity;
import android.util.Log;

import com.twitter.sdk.android.core.models.Tweet;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * A Helper Class to do all the realm functions necessary for this application
 * This was created to reduce repeat functions in all the classes
 *
 * Created by James Reilly on 3/21/15.
 */
public class RealmHelper {

    /**  String of the custom realm to look for */
    private String mRealmName;
    /** Activity the helper was called from*/
    private Activity mAct;

    public RealmHelper(Activity a, String rName){
        mAct = a;
        mRealmName = rName;
    }

    /**
     * @return the realm of the current helper
     */
    public Realm getRealm(){
        if (mRealmName != null){
            return Realm.getInstance(mAct, mRealmName);
        }else{
            return Realm.getInstance(mAct);
        }
    }

    /**
     * @param t Tweet to be inserted into the realm
     * Adds the specified tweet into the realm
     */
    public void insertToRealm(Tweet t){
        Realm realm = this.getRealm();
        realm.beginTransaction();
        try{
            TweetRealm tweet = realm.createObject(TweetRealm.class);
            String dateString = t.createdAt;
            DateFormat format = new SimpleDateFormat("EEE MMM dd kk:mm:ss ZZZZZ yyyy");
            Date date;
            try {
                date = format.parse(dateString);
                tweet.setDate(date);
            } catch (ParseException e) {
                Log.e(mRealmName, "Error: " + e);
            }
            tweet.setRetweetedBy(t.user.screenName);
            tweet.setOriginalId(t.id);
            if(t.retweetedStatus != null){
                t = t.retweetedStatus;
                tweet.setRetweetedStatus(true);
            }else{
                tweet.setRetweetedStatus(false);
            }
            tweet.setProfileImageUrl(t.user.profileImageUrl);
            tweet.setId(t.id);
            tweet.setName(t.user.name);
            tweet.setScreename(t.user.screenName);
            tweet.setText(t.text);
            if(t.entities != null && t.entities.media != null){
                tweet.setMediaUrl(t.entities.media.get(0).mediaUrl);
            }else{
                tweet.setMediaUrl("null");
            }
            realm.commitTransaction();

        } catch (Exception e){
            realm.cancelTransaction();
            Log.e(mRealmName, "Error: " + e);
        }

    }

    /**
     *
     * @param limit The max number of tweets this can return
     * @return A list of sorted by date Tweetrealm objects
     */
    public RealmResults<TweetRealm> getTweets(int limit){
        //Get the Realm
        Realm realm = this.getRealm();
        //Get all the TweeRealm bojects from the realm and sort by date
        RealmResults<TweetRealm> result = realm.where(TweetRealm.class).findAll();
        result.sort("date", RealmResults.SORT_ORDER_DESCENDING);

        //find the current size of the list
        int curSize = result.size();

        //if it is larger than the list we have to remove data
        if(curSize > limit){
            while(curSize > limit){
                //I don't think there is a way to remove them except for one at a time
                realm.beginTransaction();
                result.get(result.size()-1).removeFromRealm();
                realm.commitTransaction();
                curSize--;
            }
            //Re-sort trimmed database
            result = realm.where(TweetRealm.class).findAll();
            result.sort("date", RealmResults.SORT_ORDER_DESCENDING);
        }
        return result;
    }
}
