package me.jreilly.JamesTweet;


import android.os.Bundle;
import android.app.Fragment;
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

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class DashFragment extends android.support.v4.app.Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Tweet> mTweetObjects = new ArrayList<>();
    private String[] mDataset;
    private final String LOG_TAG = "TweetFetcher";


    public DashFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);



        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MyAdapter(mTweetObjects);
        mRecyclerView.setAdapter(mAdapter);

        getMainUserTweets();

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        getMainUserTweets();
    }

    /*
    Pulls the hometimeline of the user current session's user
    It updates the data with the new tweets
     */
    public void getMainUserTweets() {
        final StatusesService service = Twitter.getApiClient().getStatusesService();

        service.homeTimeline(20, null, null, null, null, null, null, new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> listResult) {
                mTweetObjects.clear();
                mTweetObjects.addAll(listResult.data);
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void failure(TwitterException e) {
                Log.v(LOG_TAG, "No tweets collected!");
            }
        });

    }
}
