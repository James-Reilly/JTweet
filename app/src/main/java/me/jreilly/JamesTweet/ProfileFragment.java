package me.jreilly.JamesTweet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.ArrayList;
import java.util.List;

import me.jreilly.JamesTweet.Adapters.NetTweetAdapter;
import me.jreilly.JamesTweet.TweetParsers.ProfileSwitch;


public class ProfileFragment extends android.support.v4.app.Fragment implements ProfileSwitch {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mUserId;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Tweet> mTweetObjects = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String[] mDataset;
    private final String LOG_TAG = "TweetFetcher";

    private int mShortAnimationDuration;
    private View fragView;
    private ProfileSwitch mFragment;





    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String screenName) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, screenName);

        fragment.setArguments(args);
        return fragment;
    }

    public ProfileFragment() {
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

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_timeline);

        //Variables for Adapter
        fragView = rootView;
        mFragment = this;
        mShortAnimationDuration = getResources().getInteger(

                android.R.integer.config_shortAnimTime);

        FloatingActionButton mFab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        mFab.hide();

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(){
                getTweets(true);
            }

        });

        mLayoutManager = new LinearLayoutManager(getActivity());


        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new NetTweetAdapter(mTweetObjects, fragView, mShortAnimationDuration, mFragment );
        mRecyclerView.setAdapter(mAdapter);


        mRecyclerView.setOnScrollListener(
                new EndlessRecyclerOnScrollListener((LinearLayoutManager)mLayoutManager) {
                    @Override
                    public void onLoadMore() {

                        mSwipeRefreshLayout.setRefreshing(true);
                        getTweets(false);
                    }
                });

        getTweets(true);

        return rootView;
        // Inflate the layout for this fragment

    }

    public void swapToProfile(String uId){
        Intent intent = new Intent(getActivity(), ProfileActivity.class)
                .putExtra(ProfileActivity.PROFILE_KEY, uId);
        startActivity(intent);
        getActivity().finish();
    }

    public void swapToTweet(long tweetId){
        Intent intent = new Intent(getActivity(), TweetActivity.class)
                .putExtra(TweetActivity.TWEET_KEY, tweetId);
        startActivity(intent);
    }




    public void getTweets(Boolean pageRefresh){
        final StatusesService service = Twitter.getApiClient().getStatusesService();

        if (pageRefresh){
            service.userTimeline(null, mUserId, 50, null, null, null, null,null,null, new Callback<List<Tweet>>() {
                @Override
                public void success(Result<List<Tweet>> listResult) {
                    mTweetObjects.clear();
                    mTweetObjects.addAll(listResult.data);
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                    mSwipeRefreshLayout.setRefreshing(false);


                }

                @Override
                public void failure(TwitterException e) {
                    Log.v(LOG_TAG, "No tweets collected on main refresh! " + e);
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        } else if (mTweetObjects.size() < 200) {
            long maxId = mTweetObjects.get(mTweetObjects.size() - 1).id;
            service.userTimeline(null, mUserId, 50, null, maxId, null, null,null,null, new Callback<List<Tweet>>() {
                @Override
                public void success(Result<List<Tweet>> listResult) {

                    mTweetObjects.addAll(listResult.data.subList(0,listResult.data.size() - 1));
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void failure(TwitterException e) {
                    Log.v(LOG_TAG, "No tweets collected! on other refresh : " + e);
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        } else {
            Context context = getActivity();
            CharSequence text = "End of Stream";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

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
