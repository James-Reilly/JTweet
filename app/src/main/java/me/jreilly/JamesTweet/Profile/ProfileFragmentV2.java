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

package me.jreilly.JamesTweet.Profile;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
import me.jreilly.JamesTweet.R;
import me.jreilly.JamesTweet.TweetParsers.ProfileSwitch;
import me.jreilly.JamesTweet.TweetView.TweetActivity;


public class ProfileFragmentV2 extends android.support.v4.app.Fragment implements ProfileSwitch {

    /** The screen_name of the profile being viewed */
    private String mUserId;
    /** A vairable to store a generalized callback */
    private Callback<List<Tweet>> mCallBack;
    /** The RealmAdapter that holds main realm methods for getting/storing tweets */
    private RealmAdapter mTweetAdapter;
    /** The recyclerview to hold the profile's tweets */
    private RecyclerView mRecyclerView;
    /** The layout manager for the recylerview */
    private RecyclerView.LayoutManager mLayoutManager;
    /** The Dataset of the profiles tweets for the recyclerview */
    private RealmResults<TweetRealm> mDataset;
    /** The string for log output for this fragment */
    private final String LOG_TAG = "Profile_FragmentV2";

    /** Variable for holding the animation duration for recyclerview */
    private int mShortAnimationDuration;
    /** Variable for holding the View for recyclerview */
    private View fragView;
    /** Variable for holding the fragment for recyclerview */
    private ProfileSwitch mFragment;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Boolean mIsSticked;
    private RealmHelper mRealmHelper;

    private int selected = 0;

    private Drawable mActionBarBackgroundDrawable;


    public ProfileFragmentV2() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(ProfileActivity.PROFILE_KEY)){
            mUserId = intent.getStringExtra(ProfileActivity.PROFILE_KEY);

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_profile_v2, container, false);
        this.getActivity().setTitle("Profile");
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_timeline);
        mRealmHelper = new RealmHelper(this.getActivity(),"profile.realm");





        //Variables for Adapter
        fragView = rootView;
        mFragment = this;
        mShortAnimationDuration = getResources().getInteger(

                android.R.integer.config_shortAnimTime);

        //Hide the floating action buttton
        //FloatingActionButton mFab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        //mFab.hide();

        //Setup pull down the refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(){
                getTweets(true);
            }

        });

        try {
            mActionBarBackgroundDrawable = getResources().getDrawable(R.drawable.ab_background);
            mActionBarBackgroundDrawable.setAlpha(0);
            ((ProfileActivity)getActivity()).getSupportActionBar().setBackgroundDrawable(mActionBarBackgroundDrawable);
        }catch (Exception e){
            Log.e(LOG_TAG, "Exception: " + e);
        }
        //Set the layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        //Create a general callback for getting tweets
        mCallBack = new Callback<List<Tweet>>() {
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
                redrawTabs();
                mSwipeRefreshLayout.setRefreshing(false);

            }

            @Override
            public void failure(TwitterException e) {
                mSwipeRefreshLayout.setRefreshing(false);
                Log.e(LOG_TAG, "Exception " + e);

            }
        };

        //Intialize the realm that will store the profiles tweets
        Realm realm = mRealmHelper.getRealm();
        realm.beginTransaction();
        realm.clear(TweetRealm.class);
        realm.commitTransaction();
        RealmResults<TweetRealm> result = realm.where(TweetRealm.class).findAll();
        result.sort("date", RealmResults.SORT_ORDER_DESCENDING);
        mDataset = mRealmHelper.getTweets(50);
        getTweets(true);

        //Intiailize the realm adapter for the recyclerview to display the profiles tweets
        mTweetAdapter = new RealmAdapter(mDataset, fragView, mShortAnimationDuration, mFragment, mUserId, false);

        //apply the adapter to the timeline view
        //this will make it populate the new update data in the view
        mRecyclerView.setAdapter(mTweetAdapter);

        mRecyclerView.setOnScrollListener(
                new EndlessRecyclerOnScrollListener((LinearLayoutManager)mLayoutManager) {
                    @Override
                    public void onLoadMore() {

                    }
                });



        return rootView;
        // Inflate the layout for this fragment

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
        getActivity().finish();
    }

    public void swapToTweet(long tweetId, View view){
        Intent intent = new Intent(getActivity(), TweetActivity.class)
                .putExtra(TweetActivity.TWEET_KEY, tweetId).putExtra(TweetActivity.REALM_KEY, "profile.realm");
        String transitionName = getString(R.transition.transition);
        Log.v(LOG_TAG, transitionName);

        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this.getActivity(),
                        view,   // The view which starts the transition
                        transitionName    // The transitionName of the view we’re transitioning to
                );

        ActivityCompat.startActivity(this.getActivity(), intent, options.toBundle());
    }
    public Realm getRealm(){
        return Realm.getInstance(this.getActivity(), "profile.realm");
    }


    /**
     * Get the tweets from current user
     * @param pageRefresh Old parameter may bring it back so it goes unused
     */

    public void getTweets(Boolean pageRefresh){
        final StatusesService service = Twitter.getApiClient().getStatusesService();

        long id;
        //Check if this is a refresh or the intial filling of the database
        if(mDataset != null && mDataset.size() != 0){
            id = mDataset.get(0).getOriginalId();
            service.userTimeline(null, mUserId, 50, id, null, null, null,null,null, mCallBack);
        }else{

            service.userTimeline(null, mUserId, 50, null, null, null, null,null,null, mCallBack);

        }
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


    /**
     * Draws the tabs and translates them to the correct position
     */
    public void redrawTabs(){
        if(mRecyclerView != null){
            fragView.findViewById(R.id.stickyheader).setVisibility(View.VISIBLE);
            View mView =  mRecyclerView.getChildAt(0);
            int top = mView.findViewById(R.id.card_view).getTop();
            int tabsHeight = fragView.findViewById(R.id.stickyheader).getHeight();


            int headerViewHeight = mView.findViewById(R.id.card_view).getMeasuredHeight();
            int delta = headerViewHeight - tabsHeight;

            fragView.findViewById(R.id.stickyheader).setTranslationY(Math.max(((ProfileActivity)
                    getActivity()).getSupportActionBar().getHeight(), delta + top));
        }


    }

    /**
     * Class used to implement endless scrolling of the recyclerveiw
     * Currently goes unused but should return soon
     */

    public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
        private int previousToral = 0;
        private boolean loading = true;
        private int visibleThreshold = 5;
        int firstVisibleItem, visibleItemCount, totalItemCount;
        int y_dir = 0;

        private LinearLayoutManager mLinearLayoutManager;

        public EndlessRecyclerOnScrollListener(LinearLayoutManager linearLayoutManager){
            this.mLinearLayoutManager = linearLayoutManager;
        }



        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy){
            super.onScrolled(recyclerView,dx,dy);
            y_dir += dy;

            visibleItemCount = recyclerView.getChildCount();
            totalItemCount = mLinearLayoutManager.getItemCount();
            firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

            if (visibleItemCount == 0) return;

            if(visibleItemCount == 0) return;
            if(firstVisibleItem != 0) return;

            //Caluclated how translucent the actionbar should be
            ImageView mImageView = (ImageView) recyclerView.getChildAt(0).findViewById(R.id.header_imageview);
            View mView =  recyclerView.getChildAt(0);
            mImageView.setTranslationY(-recyclerView.getChildAt(0).getTop() / 2);


            final int headerHeight = mImageView.getHeight() - ((ProfileActivity)getActivity()).getSupportActionBar().getHeight();
            final float ratio = (float) Math.min(Math.max(y_dir, 0), headerHeight) / headerHeight;
            final int newAlpha = (int) (ratio * 255);

            mActionBarBackgroundDrawable.setAlpha(newAlpha);

            //Tabs stuff

            redrawTabs();

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
