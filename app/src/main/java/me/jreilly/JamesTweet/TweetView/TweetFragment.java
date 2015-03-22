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
package me.jreilly.JamesTweet.TweetView;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.RealmResults;
import me.jreilly.JamesTweet.Adapters.RealmAdapter;
import me.jreilly.JamesTweet.Etc.ComposeActivity;
import me.jreilly.JamesTweet.Models.RealmHelper;
import me.jreilly.JamesTweet.Models.TweetRealm;
import me.jreilly.JamesTweet.Profile.ProfileActivity;
import me.jreilly.JamesTweet.R;
import me.jreilly.JamesTweet.TweetParsers.ProfileLink;
import me.jreilly.JamesTweet.TweetParsers.ProfileSwitch;

/**
 * Created by jreilly on 1/19/15.
 * The Fragment of the detail of the Tweet given
 */
public class TweetFragment extends android.support.v4.app.Fragment implements ProfileSwitch {

    /** Variable to hold the visual objects that need to be filled */
    private long mTweetId;
    private TextView mTweet;
    private TextView mUser;
    private ImageButton mImage;
    private ImageButton mProfileImage;
    private ImageButton mReplyButton;
    private ImageButton mRetweetButton;
    private ImageButton mFavoriteButton;

    /** Variables to hold the data for the RecyclerView */
    private ProfileSwitch mActivity;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    /**Vairables for the Realm */
    private RealmAdapter mAdapter;
    private RealmResults<TweetRealm> mDataset;
    private RealmHelper mRealmHelper;

    private final String LOG_TAG = "TweetFragment";
    public TweetFragment() {

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(TweetActivity.TWEET_KEY)){

            mTweetId = intent.getLongExtra(TweetActivity.TWEET_KEY, 0);
            Log.v(LOG_TAG, "Got Tweet id: " + mTweetId);

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_tweet, container, false);
        //Get Visuals to set
        mUser = (TextView) rootView.findViewById(R.id.tweet_user);
        mTweet = (TextView) rootView.findViewById(R.id.tweet_text);
        mImage = (ImageButton) rootView.findViewById(R.id.tweet_picture);
        mProfileImage = (ImageButton) rootView.findViewById(R.id.tweet_user_image);
        mReplyButton = (ImageButton) rootView.findViewById(R.id.tweet_reply_button);
        mRetweetButton = (ImageButton) rootView.findViewById(R.id.tweet_retweet_button);
        mFavoriteButton = (ImageButton) rootView.findViewById(R.id.tweet_favorite_button);

        //private variable to use to switch to other profiles
        mActivity = (ProfileSwitch) getActivity();

        //Setup the layout
        setUpTweetLayout();

        //Initialize the RecyclerView to hold the replies
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_replies);

                mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        //Clear the database, etc.
        setUpRealm();

        //get at max 15 replies from tweet and populate data
        getReplies(mTweetId, 15, true);

        //Initialize variables for the adapter
        ProfileSwitch pFragment = this;
        int shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        //initialize the RealmAdapter for the tweet
        mAdapter = new RealmAdapter(mDataset, rootView, shortAnimationDuration, pFragment, null);

        //Set the adapter to the recyclerview
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }


    /**
     * Sets the realm up for the use of the fragment
     * It clears the database and assigns the database the data for the adapter
     */
    public void setUpRealm(){
        //Initialize the helper
        mRealmHelper = new RealmHelper(this.getActivity(), "tweet.realm");
        //Get the tweet reply realm
        Realm realm = mRealmHelper.getRealm();
        //Clear the Database from previous tweets
        realm.beginTransaction();
        realm.clear(TweetRealm.class);
        realm.commitTransaction();
        //Get sorted tweets
        mDataset = mRealmHelper.getTweets(15);
    }

    /**
     * Sets all the visual elements of the detail view of the tweet using the private variable
     * mTweetId
     */
    public void setUpTweetLayout(){
        Twitter.getApiClient().getStatusesService().show(mTweetId, null, true, true, new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> tweetResult) {
                final Tweet t = tweetResult.data;

                //Set Username/ScreenName  text
                mUser.setText(t.user.name + " - @" +
                        t.user.screenName);

                //Set profile image
                Picasso.with(mProfileImage.getContext()).load(t.user.profileImageUrl).into(
                        mProfileImage
                );

                //Set swaptoprofile listener
                mProfileImage.setOnClickListener( new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mActivity.swapToProfile(
                                t.user.screenName);

                    }
                });

                //Set Media Image
                if (t.entities != null && (t.entities.media != null)){

                    Picasso.with(mProfileImage.getContext()).load(
                            t.entities.media.get(0).mediaUrl).fit().into(
                            mImage
                    );

                } else  {
                    mImage.setImageDrawable(null);

                }

                //Set the TweetText
                String tweetText = t.text;

                //Set Clickable hastags and clicakable profile names
                ArrayList<int[]> hashtagSpans = getSpans(tweetText, '#');
                ArrayList<int[]> profileSpans = getSpans(tweetText, '@');

                final SpannableString tweetContent = new SpannableString(tweetText);

                for( int j = 0; j < profileSpans.size(); j ++){
                    int[] span = profileSpans.get(j);
                    int profileStart = span[0];
                    int profileEnd = span[1];

                    tweetContent.setSpan(new ProfileLink(mTweet.getContext(), mActivity),
                            profileStart, profileEnd, 0);
                }
                mTweet.setMovementMethod(LinkMovementMethod.getInstance());
                mTweet.setText(tweetContent);

                //Setting the replaybutton
                mReplyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ComposeActivity.class)
                                .putExtra(ComposeActivity.REPLY_ID, mTweetId).putExtra(ComposeActivity.REPLY_USER, "@" + t.user.screenName);
                        startActivity(intent);

                    }
                });

                //Set initial favorite highlight
                if(t.favorited){
                    mFavoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_grey600_24dp));
                } else {
                    mFavoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_outline_grey600_24dp));
                }

                //setactionlistener
                setFavoriteButton();


                //Set initial retweet highlight
                if(t.retweeted){
                    mRetweetButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_cached_selected));
                }else {
                    mRetweetButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_cached_grey600_24dp));
                }
                //setactionlistener
                setRetweetButton();


                Log.v(LOG_TAG, "Set all attributes!");
            }

            @Override
            public void failure(TwitterException e) {
                Log.e(LOG_TAG, "Could not get Tweets : " + e);
            }
        });


    }

    /**
     * @param body The text to span
     * @param prefix The symbol at the beggining of the span
     * @return The clickable spans
     * Sets the clickable spans for any prefix
     * (This program uses it for @ strings and # strings
     */
    public ArrayList<int[]> getSpans(String body, char prefix) {
        ArrayList<int[]> spans = new ArrayList<int[]>();

        Pattern pattern = Pattern.compile(prefix + "\\w+");
        Matcher matcher = pattern.matcher(body);

        // Check all occurrences
        while (matcher.find()) {
            int[] currentSpan = new int[2];
            currentSpan[0] = matcher.start();
            currentSpan[1] = matcher.end();
            spans.add(currentSpan);
        }

        return  spans;
    }


    /**
     * @param id The id of the tweet to get replies to
     * @param num_left number of depths left to search
     * @param first if the first tweet in the reply search
     * Gets the replies to a tweet recursively
     */
    public void getReplies(long id, final int num_left, final boolean first){
        Twitter.getApiClient().getStatusesService().show(id, null, true, true, new Callback<Tweet>() {

            @Override
            public void success(Result<Tweet> tweetResult) {
                if(!first){
                    mRealmHelper.insertToRealm(tweetResult.data);
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                }
                if(tweetResult.data.inReplyToStatusIdStr != null && num_left > 0){
                    getReplies(tweetResult.data.inReplyToStatusId, num_left - 1, false);
                }
            }
            @Override
            public void failure(TwitterException e) {
                   Log.e(LOG_TAG, "Excpetion: " + e);
            }
        });
    }



    /**
     * sets the clickListener for the favorite button
     */
    public void setFavoriteButton(){
        mFavoriteButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Twitter.getApiClient().getStatusesService().show(mTweetId, null, true, true, new Callback<Tweet>() {
                    @Override
                    public void success(Result<Tweet> tweetResult) {
                        Tweet t = tweetResult.data;
                        if (t.favorited) {
                            TwitterCore.getInstance().getApiClient().getFavoriteService().destroy(mTweetId, null, new Callback<Tweet>() {
                                @Override
                                public void success(Result result) {
                                    Toast.makeText(mFavoriteButton.getContext(), "FAVORITE!",
                                            Toast.LENGTH_SHORT).show();
                                    mFavoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_outline_grey600_24dp));
                                }
                                @Override
                                public void failure(TwitterException e) {
                                }
                            });

                        } else {
                            TwitterCore.getInstance().getApiClient().getFavoriteService().create(mTweetId, null, new Callback<Tweet>() {
                                @Override
                                public void success(Result result) {
                                    Toast.makeText(mFavoriteButton.getContext(), "FAVORITE!",
                                            Toast.LENGTH_SHORT).show();
                                    mFavoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_toggle_star_selected));
                                }
                                @Override
                                public void failure(TwitterException e) {
                                    Toast.makeText(mFavoriteButton.getContext(), "Exception " + e,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                    }

                    @Override
                    public void failure(TwitterException e) {
                        Log.e("TweetFragment", "Exception: " + e);
                    }


                });
            }
        });

    }

    /**
     * sets the clickListener for the retweet button
     */
    public void setRetweetButton(){
        mRetweetButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Twitter.getApiClient().getStatusesService().show(mTweetId, null, true, true, new Callback<Tweet>() {
                    @Override
                    public void success(Result<Tweet> tweetResult) {
                        Tweet t = tweetResult.data;
                        if(t.retweeted){
                            Log.v(LOG_TAG, t.currentUserRetweet.toString());
                            String retweetId  = t.currentUserRetweet.toString();
                            int id = retweetId.indexOf("id_str=") + 7;
                            String id2 = retweetId.substring(id, retweetId.length() - 1);
                            Log.v(LOG_TAG, id2);
                            long rtId = Long.valueOf(id2);
                            TwitterCore.getInstance().getApiClient().getStatusesService().destroy(rtId, null, new Callback<Tweet>() {
                                @Override
                                public void success(Result<Tweet> tweetResult) {
                                    Toast.makeText(mRetweetButton.getContext(), "UN-RETWEETED!",
                                            Toast.LENGTH_SHORT).show();
                                    mRetweetButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_cached_grey600_24dp));

                                }
                                @Override
                                public void failure(TwitterException e) {
                                    Log.e("TweetFragment", "Exception: " + e);
                                }
                            });
                        } else {
                            TwitterCore.getInstance().getApiClient().getStatusesService().retweet(
                                    mTweetId, null, new Callback<Tweet>() {
                                        @Override
                                        public void success(Result result) {
                                            Toast.makeText(mRetweetButton.getContext(), "RETWEET!",
                                                    Toast.LENGTH_SHORT).show();
                                            mRetweetButton.setImageDrawable(getResources()
                                                    .getDrawable(
                                                            R.drawable.ic_action_cached_selected));
                                        }
                                        @Override
                                        public void failure(TwitterException e) {
                                        }
                                    });
                        }
                    }
                    @Override
                    public void failure(TwitterException e) {
                    }
                });
            }
        });
    }

    /**
     * @param uId The ID of the profile to switch to
     * Starts the ProfileActivity with the uID passed as the intent
     */
    public void swapToProfile(String uId){
        Intent intent = new Intent(getActivity(), ProfileActivity.class)
                .putExtra(ProfileActivity.PROFILE_KEY, uId);
        startActivity(intent);
        getActivity().finish();
    }

    /**
     * @param tweetId The ID of the tweet to show
     * Starts the TweetActivity with the tweetID passed as the intent
     */
    public void swapToTweet(long tweetId){
        Intent intent = new Intent(getActivity(), TweetActivity.class)
                .putExtra(TweetActivity.TWEET_KEY, tweetId);
        startActivity(intent);
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
}
