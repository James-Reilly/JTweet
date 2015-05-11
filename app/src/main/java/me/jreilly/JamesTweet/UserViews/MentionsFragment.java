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

package me.jreilly.JamesTweet.UserViews;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import me.jreilly.JamesTweet.Adapters.RealmAdapter;
import me.jreilly.JamesTweet.Models.RealmHelper;
import me.jreilly.JamesTweet.Models.TweetRealm;
import me.jreilly.JamesTweet.Profile.ProfileActivity;
import me.jreilly.JamesTweet.R;
import me.jreilly.JamesTweet.TweetParsers.ProfileSwitch;
import me.jreilly.JamesTweet.TweetView.TweetActivity;

/**
 *
 */
public class MentionsFragment extends android.support.v4.app.Fragment implements ProfileSwitch {

    private static final String LOG_TAG = "MentionFragment";

    private RealmHelper mRealmHelper;
    private RealmResults<TweetRealm> mDataset;
    private TimelineUpdater mUpdater;
    private RecyclerView mRecyclerView;
    private RealmAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView.LayoutManager mLayoutManager;


    public MentionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_mentions, container, false);
        this.getActivity().setTitle("Mentions");
        //Setup The View
        setupMentions(rootView);

        return rootView;
    }

    public void setupMentions(View rootView){
        //Hide the Fab From the DashFragment

        //Setup Realm Helper methods
        mRealmHelper = new RealmHelper(this.getActivity(), "mentions.realm");
        mDataset = mRealmHelper.getTweets(50);

        //Setup timline updater runnable
        mUpdater = new TimelineUpdater();

        //Inflate the Recyclerview
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_timeline);

        //Initialize the layout to a LinearLayout
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);


        //Initialize variables for the adapter
        ProfileSwitch pFragment = this;
        int shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        //initialize the RealmAdapter for the tweet
        mAdapter = new RealmAdapter(mDataset, rootView, shortAnimationDuration, pFragment, null, false);

        //Set the adapter to the recyclerview
        mRecyclerView.setAdapter(mAdapter);



        //Enable pull down to refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(){
                mUpdater.run();
            }
        });
        mUpdater.run();


    }


    /**
     * @param uId The Screen_name of the profile to switch to
     * @param view The View to use for the transition
     * Takes in the profile id (screen_name) of the desired profile to switch to
     * It then switches the to profile activity of the requested user
     */
    public void swapToProfile(String uId, View view){
        Intent intent = new Intent(getActivity(), ProfileActivity.class)
                .putExtra(ProfileActivity.PROFILE_KEY, uId);
        startActivity(intent);
    }

    /**
     * @param tweetId
     * Starts the TweetActivity with the tweet of the
     * specified ID.
     */
    public void swapToTweet(long tweetId, View view){
        Intent intent = new Intent(getActivity(), TweetActivity.class)
                .putExtra(TweetActivity.TWEET_KEY, tweetId).putExtra(TweetActivity.REALM_KEY, "mentions.realm");
        String transitionName = getString(R.transition.transition);
        Log.v(LOG_TAG, transitionName);

        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this.getActivity(),
                        view,   // The view which starts the transition
                        transitionName    // The transitionName of the view we’re transitioning to
                );

        ActivityCompat.startActivity(this.getActivity(), intent, options.toBundle());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Realm realm = mRealmHelper.getRealm();
        realm.beginTransaction();
        realm.clear(TweetRealm.class);
        realm.commitTransaction();
        realm.close();

    }


    class TimelineUpdater implements Runnable {

        @Override
        public void run() {
            long id;

            if(mDataset != null && mDataset.size() != 0){
                id = mDataset.get(0).getOriginalId();
                final StatusesService service = Twitter.getApiClient().getStatusesService();
                service.mentionsTimeline(50, id, null, null, null, null, new Callback<List<Tweet>>() {
                    @Override
                    public void success(Result<List<Tweet>> listResult) {
                        List<Tweet> list = listResult.data;
                        for (Tweet t : list) {
                            try {
                                mRealmHelper.insertToRealm(t);
                            } catch (Exception te) {
                                Log.e(LOG_TAG, "Exception: " + te);
                            }
                        }
                        mDataset = mRealmHelper.getTweets(50);
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void failure(TwitterException e) {
                        Log.e(LOG_TAG, "Exception: " + e);
                    }
                });
            }else{
                final StatusesService service = Twitter.getApiClient().getStatusesService();
                service.mentionsTimeline(50, null, null, null, null, null, new Callback<List<Tweet>>() {
                    @Override
                    public void success(Result<List<Tweet>> listResult) {
                        List<Tweet> list = listResult.data;
                        for (Tweet t : list) {
                            try {
                                mRealmHelper.insertToRealm(t);
                            }catch (Exception te) { Log.e(LOG_TAG, "Exception: " + te);}
                        }
                        mDataset = mRealmHelper.getTweets(50);
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void failure(TwitterException e) {
                        Log.e(LOG_TAG, "Exception: " + e);
                    }
                });

            }
        }
    }

}
