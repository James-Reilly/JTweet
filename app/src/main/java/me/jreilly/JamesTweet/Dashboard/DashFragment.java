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
package me.jreilly.JamesTweet.Dashboard;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.melnykov.fab.FloatingActionButton;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import me.jreilly.JamesTweet.Adapters.RealmAdapter;
import me.jreilly.JamesTweet.Etc.ComposeActivity;
import me.jreilly.JamesTweet.Models.TweetRealm;
import me.jreilly.JamesTweet.Profile.ProfileActivity;
import me.jreilly.JamesTweet.R;
import me.jreilly.JamesTweet.TweetView.TweetActivity;
import me.jreilly.JamesTweet.TweetParsers.ProfileSwitch;


/**
 * DashFragment
 * A fragment to store the main timeline of the newsfeed
 */
public class DashFragment extends android.support.v4.app.Fragment implements ProfileSwitch {

    /** The RecyclerView to store the tweets */
    private RecyclerView mRecyclerView;
    /** The Layout manager for the RecyclerView */
    private RecyclerView.LayoutManager mLayoutManager;
    /** Stores the layout to detect pull down to refresh  */
    private SwipeRefreshLayout mSwipeRefreshLayout;
    /** The LOG_TAG for all the log calls in the fragment */
    private final String LOG_TAG = "DashFragment";
    /** Temporary variable for saying max # items in the timeline. Will eventually be in settings */
    private int mMaxItems = 100;
    /** The Floating Action Button that floats above the timeline */
    private FloatingActionButton mFab;
    /** The PopUp window used for compose tweet */
    private PopupWindow popUp;
    /** The Adpater for the RecylerView */
    private RealmAdapter mTweetAdapter;
    /** Stores an instance of timeline updates that gets tweets and store them in the Realm */
    private TimelineUpdater mTimelineUpdater;
    /** Stores the callback used for getting tweets */
    private Callback<List<Tweet>> mCallBack;
    /** Stroes the data of the animation duration for the adapter*/
    private int mShortAnimationDuration;
    /** Stores the data of the rootView for many function */
    private View fragView;
    /**Stores the fragment used in the profile switch class */
    private ProfileSwitch mFragment;
    /** Stores the tweets to be displayed in the recylerview*/
    private RealmResults<TweetRealm> mDataset;


    public DashFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //Inflate the Recyclerview
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_timeline);


        //Set Variables needed for the RealmAdapter and (ProfileSwitch)
        mFragment = this;
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        fragView = rootView;

        setFab(rootView);

        //Enable pull down to refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(){

                mTimelineUpdater.run();
            }

        });

        //Does the rest of the timeline initialization
        setupTimeline();

        return rootView;
    }

    /**
     *  Initializes many of the data structures needed for this fragment to work.
     */
    public void setupTimeline() {
        try{

            //Initialize the layout to a LinearLayout
            mLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLayoutManager);

            //Initialize Compose Tweet Window
            popUp = new PopupWindow(getActivity());

            //Instantiate the RealmAdpater for the Recyclerview
            mTweetAdapter = new RealmAdapter(mDataset, fragView, mShortAnimationDuration, mFragment, null);

            //apply the adapter to the timeline view
            //this will make it populate the new update data in the view
            mRecyclerView.setAdapter(mTweetAdapter);

            //Instantiate the Timeline Updater
            mTimelineUpdater = new TimelineUpdater();

            //Instantiate General Use CallBack the inserts Tweets into the database
            mCallBack = generateCallback();
            mDataset = getTweets();

            //Run to get the current tweeets
            mTimelineUpdater.run();

            //Instantiate the RealmAdpater for the Recyclerview
            mTweetAdapter = new RealmAdapter(mDataset, fragView, mShortAnimationDuration, mFragment, null);

            //apply the adapter to the timeline view
            //this will make it populate the new update data in the view
            mRecyclerView.setAdapter(mTweetAdapter);

            //Instantiate the Timeline Updater
            mTimelineUpdater = new TimelineUpdater();

            Log.e(LOG_TAG, "Finished Setup!");

        }
        catch(Exception te) { Log.e(LOG_TAG, "Failed to fetch timeline: "+te.getMessage()); }
    }

    public Realm getRealm(){
        return Realm.getInstance(this.getActivity());
    }


    /**
     * @param t Tweet to be inserted into the realm
     * Adds the specified tweet into the realm
     */
    public void insertToRealm(Tweet t){
        Realm realm = Realm.getInstance(this.getActivity());

        realm.beginTransaction();



        TweetRealm tweet = realm.createObject(TweetRealm.class);
        String dateString = t.createdAt;
        DateFormat format = new SimpleDateFormat("EEE MMM dd kk:mm:ss ZZZZZ yyyy");
        Date date;
        try {
            date = format.parse(dateString);
            tweet.setDate(date);
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Error: " + e);
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
    }

    /**
     * @param uId
     * Takes in the profile id (screen_name) of the desired profile to switch to
     * It then switches the to profile activity of the requested user
     */

    public void swapToProfile(String uId){
        Intent intent = new Intent(getActivity(), ProfileActivity.class)
                .putExtra(ProfileActivity.PROFILE_KEY, uId);
        mFab.hide();
        startActivity(intent);
    }

    /**
     * @param tweetId
     * Starts the TweetActivity with the tweet of the
     * specified ID.
     */
    public void swapToTweet(long tweetId){
        Intent intent = new Intent(getActivity(), TweetActivity.class)
                .putExtra(TweetActivity.TWEET_KEY, tweetId);
        mFab.hide();
        startActivity(intent);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        getRealm().close();

    }

    /**
     * @return RealmResults<TweetRealm> The sorted list of tweets
     * Querys the realm of this activity for tweets
     * and sorts the data by the data
    */
    public RealmResults<TweetRealm> getTweets(){
        Realm realm = getRealm();
        RealmResults<TweetRealm> result = realm.where(TweetRealm.class).findAll();
        result.sort("date", RealmResults.SORT_ORDER_DESCENDING);
        return result;
    }

    /**
     * @return A Callback that adds a list of tweets to a realm database on success
     * Generates a callback to add tweets to the local real database
     */
    public Callback<List<Tweet>> generateCallback(){
        return new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> listResult) {

                List<Tweet> list = listResult.data;
                Realm realm = Realm.getInstance(getActivity());

                for (Tweet t : list) {
                    try {
                        insertToRealm(t);
                    }catch (Exception te) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        Log.e(LOG_TAG, "Exception: " + te);}
                }
                int rowLimit = 50;
                RealmResults<TweetRealm> result = realm.where(TweetRealm.class).findAll();
                result.sort("date", RealmResults.SORT_ORDER_DESCENDING);
                int curSize = result.size();
                if(curSize > rowLimit){
                    while(curSize > rowLimit){
                        realm.beginTransaction();
                        result.get(result.size()-1).removeFromRealm();
                        realm.commitTransaction();
                        curSize--;
                    }
                    result = realm.where(TweetRealm.class).findAll();
                    result.sort("date", RealmResults.SORT_ORDER_DESCENDING);
                }
                mDataset = result;
                mRecyclerView.getAdapter().notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);

                Log.v("NEW CONTEXT", "All done!");


            }
            @Override
            public void failure(TwitterException e) {
                Log.e("NEW CONTEXT", "Exception " + e);

            }
        };
    }

    /**
     * @param rootView
     * Initializes the fab the rootView
     */
    public void setFab(View rootView){
        mFab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        mFab.setVisibility(View.VISIBLE);
        mFab.attachToRecyclerView(mRecyclerView);
        mFab.hide();
        mFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ComposeActivity.class);
                mFab.hide();
                startActivity(intent);
            }
        });
    }





    class TimelineUpdater implements Runnable {

        @Override
        public void run() {
            long id;
            Log.e(LOG_TAG, "Getting Tweets!");
            if(mDataset != null && mDataset.size() != 0){
                id = mDataset.get(0).getOriginalId();
                final StatusesService service = Twitter.getApiClient().getStatusesService();
                service.homeTimeline(50, id, null, null, null, null, true, mCallBack);
            }else{
                final StatusesService service = Twitter.getApiClient().getStatusesService();
                service.homeTimeline(50, null, null, null, null, null, true, mCallBack);

            }
        }
    }

    /**
     * A class used for detecting the end of a recyclerview to load more data to repopluate the
     * view to simulate a endless list
     */
    public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
        private int previousToral = 0;
        private boolean loading = true;
        private int visibleThreshold = 5;
        int firstVisibleItem, visibleItemCount, totalItemCount;

        private LinearLayoutManager mLinearLayoutManager;

        public EndlessRecyclerOnScrollListener(LinearLayoutManager linearLayoutManager){
            this.mLinearLayoutManager = linearLayoutManager;
        }



        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy){
            if( dy > 0) {
                mFab.hide();
            } else {
                mFab.show();
            }
            super.onScrolled(recyclerView,dx,dy);

            visibleItemCount = recyclerView.getChildCount();
            totalItemCount = mLinearLayoutManager.getItemCount();
            firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

            if (loading){
                if (totalItemCount > previousToral) {
                    loading = false;
                    previousToral = totalItemCount;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount)
                    <= (firstVisibleItem + visibleThreshold)) {
                onLoadMore();
                loading = true;

            }
        }

        public abstract void onLoadMore();


    }



}
