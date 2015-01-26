package me.jreilly.JamesTweet;

import android.content.Intent;
import android.os.Bundle;
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

import me.jreilly.JamesTweet.TweetParsers.ProfileLink;
import me.jreilly.JamesTweet.TweetParsers.ProfileSwitch;

/**
 * Created by jreilly on 1/19/15.
 */
public class TweetFragment extends android.support.v4.app.Fragment  {

    private long mTweetId;
    private TextView mTweet;
    private TextView mUser;
    private ImageButton mImage;
    private ImageButton mProfileImage;
    private ImageButton mReplyButton;
    private ImageButton mRetweetButton;
    private ImageButton mFavoriteButton;

    private ProfileSwitch mActivity;

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
        //Set Data in Detail
        mUser = (TextView) rootView.findViewById(R.id.tweet_user);
        mTweet = (TextView) rootView.findViewById(R.id.tweet_text);
        mImage = (ImageButton) rootView.findViewById(R.id.tweet_picture);
        mProfileImage = (ImageButton) rootView.findViewById(R.id.tweet_user_image);
        mReplyButton = (ImageButton) rootView.findViewById(R.id.tweet_reply_button);
        mRetweetButton = (ImageButton) rootView.findViewById(R.id.tweet_retweet_button);
        mFavoriteButton = (ImageButton) rootView.findViewById(R.id.tweet_favorite_button);

        mActivity = (ProfileSwitch) getActivity();
        setUpTweetLayout();


        return rootView;
    }

    public void setUpTweetLayout(){
        Twitter.getApiClient().getStatusesService().show(mTweetId, null, true, true, new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> tweetResult) {
                final Tweet t = tweetResult.data;
                if(t.retweeted){


                }

                mUser.setText(t.user.name + " - @" +
                        t.user.screenName);
        /*
        New MyAdapter Code
         */

                Picasso.with(mProfileImage.getContext()).load(t.user.profileImageUrl).into(
                        mProfileImage
                );

                mProfileImage.setOnClickListener( new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mActivity.swapToProfile(
                                t.user.screenName);

                    }
                });

                if (t.entities != null && (t.entities.media != null)){

                    Picasso.with(mProfileImage.getContext()).load(
                            t.entities.media.get(0).mediaUrl).fit().into(
                            mImage
                    );

                } else  {
                    mImage.setImageDrawable(null);

                }

                String tweetText = t.text;

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


                mReplyButton.setOnClickListener( new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mReplyButton.getContext(), "REPLY!",
                                Toast.LENGTH_SHORT).show();

                    }
                });
                if(t.favorited){
                    mFavoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_grey600_24dp));
                } else {
                    mFavoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_outline_grey600_24dp));
                }

                mFavoriteButton.setOnClickListener( new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {


                        if(t.favorited){


                            TwitterCore.getInstance().getApiClient().getFavoriteService().destroy(mTweetId, null,new Callback<Tweet>() {


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

                        }else {
                            TwitterCore.getInstance().getApiClient().getFavoriteService().create(mTweetId, null,new Callback<Tweet>() {


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
                });

                if(t.retweeted){
                    mRetweetButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_cached_selected));
                }else {
                    mRetweetButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_cached_grey600_24dp));
                }

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
                Log.v(LOG_TAG, "Set all attributes!");
            }

            @Override
            public void failure(TwitterException e) {
                Log.e(LOG_TAG, "Could not get Tweets : " + e);
            }
        });
    }

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


}
