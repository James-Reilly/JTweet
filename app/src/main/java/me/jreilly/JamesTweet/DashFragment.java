package me.jreilly.JamesTweet;


import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.BaseColumns;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.ArrayList;
import java.util.List;

import me.jreilly.JamesTweet.TweetParsers.ProfileLink;
import me.jreilly.JamesTweet.TweetParsers.ProfileSwitch;


/**
 * A simple {@link Fragment} subclass.
 */
public class DashFragment extends android.support.v4.app.Fragment implements ProfileSwitch {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Tweet> mTweetObjects = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String[] mDataset;
    private final String LOG_TAG = "TweetFetcher";
    private int mTotalItems = 20;


    //tWITTER DATABASE objects

    private TweetDataHelper mHelper;
    private SQLiteDatabase mTimelineDB;
    private Cursor mCursor;
    private TweetAdapter mTweetAdapter;
    private BroadcastReceiver mTweetReciever;

    private TimelineUpdater mTimelineUpdater;
    private TimelineExtender mTimelineExtender;


    private int mShortAnimationDuration;
    private View fragView;

    private ProfileSwitch mFragment;


    public DashFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        fragView = rootView;
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_timeline);

        mTimelineUpdater = new TimelineUpdater();
        mTimelineExtender = new TimelineExtender();
        mFragment = this;
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);





        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(){

                mTimelineUpdater.run();
            }

        });

        mLayoutManager = new LinearLayoutManager(getActivity());


        mRecyclerView.setLayoutManager(mLayoutManager);

        setupTimeline();






        mRecyclerView.setOnScrollListener(
                new EndlessRecyclerOnScrollListener((LinearLayoutManager)mLayoutManager) {
                  @Override
                  public void onLoadMore() {

                      mTimelineExtender.run();

                  }
              });
//
//        getMainUserTweets(true);

        return rootView;
    }

    public void setupTimeline() {

        try
        {
            //get reference to the list view

            //instantiate database helper
            mHelper = new TweetDataHelper(getActivity());
            //get the database
            mTimelineDB = mHelper.getReadableDatabase();

            //query the database, most recent tweets first
            mCursor = mTimelineDB.query("home", null, null, null, null, null, "update_time DESC");

            //manage the updates using a cursor

            //instantiate adapter
            mTweetAdapter = new TweetAdapter(mCursor, fragView, mShortAnimationDuration, mFragment );

            //apply the adapter to the timeline view
            //this will make it populate the new update data in the view
            mRecyclerView.setAdapter(mTweetAdapter);
            //instantiate receiver class for finding out when new updates are available
            mTweetReciever = new TwitterUpdateReceiver();
            //register for updates
            getActivity().registerReceiver(mTweetReciever, new IntentFilter("TWITTER_UPDATES"));

            //start the service for updates now
            getActivity().getApplicationContext().startService(
                    new Intent(getActivity().getApplicationContext(), TimelineService.class));
            Log.e(LOG_TAG, "Finished Setup!");

        }
        catch(Exception te) { Log.e(LOG_TAG, "Failed to fetch timeline: "+te.getMessage()); }
    }

    public void swapToProfile(String uId){
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, ProfileFragment.newInstance(uId)).addToBackStack(null).commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            //stop the updater Service
            getActivity().stopService(new Intent(getActivity(), TimelineService.class));
            //remove receiver register
            getActivity().unregisterReceiver(mTweetReciever);
            //close the database
            mTimelineDB.close();
        }
        catch(Exception se) { Log.e(LOG_TAG, "unable to stop Service or receiver"); }
    }


    class TwitterUpdateReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("Reciver", "Recieiving?");
            int rowLimit = 600;
            if(DatabaseUtils.queryNumEntries(mTimelineDB, "home") > rowLimit) {
                String deleteQuery = "DELETE FROM home WHERE "+BaseColumns._ID+" NOT IN " +
                        "(SELECT "+BaseColumns._ID+" FROM home ORDER BY "+"update_time DESC " +
                        "limit "+rowLimit+")";
                mTimelineDB.execSQL(deleteQuery);
            }
            mCursor = mTimelineDB.query("home", null, null, null, null, null, "update_time DESC");
            getActivity().startManagingCursor(mCursor);
            mTweetAdapter = new TweetAdapter(mCursor, fragView, mShortAnimationDuration, mFragment);
            mRecyclerView.setAdapter(mTweetAdapter);

        }
    }

    class TimelineUpdater implements Runnable {

        @Override
        public void run() {

            Log.e(LOG_TAG, "Getting Tweets!");
            final StatusesService service = Twitter.getApiClient().getStatusesService();
            service.homeTimeline(50, null, null, null, null, null, true, new Callback<List<Tweet>>() {
                @Override
                public void success(Result<List<Tweet>> listResult) {
                    boolean statusChanges = false;
                    try {
                        for (Tweet t : listResult.data) {
                            ContentValues tweetValues = mHelper.getValues(t);
                            mTimelineDB.insertOrThrow("home", null, tweetValues);
                            statusChanges = true;

                        }
                    }catch (Exception te) { Log.e(LOG_TAG, "Exception: " + te);
                    }

                    if (statusChanges){
                        int rowLimit = 600;
                        if(DatabaseUtils.queryNumEntries(mTimelineDB, "home") > rowLimit) {
                            String deleteQuery = "DELETE FROM home WHERE "+BaseColumns._ID+" NOT IN " +
                                    "(SELECT "+BaseColumns._ID+" FROM home ORDER BY "+"update_time DESC " +
                                    "limit "+rowLimit+")";
                            mTimelineDB.execSQL(deleteQuery);
                        }
                        mCursor = mTimelineDB.query("home", null, null, null, null, null, "update_time DESC");
                        getActivity().startManagingCursor(mCursor);
                        mTweetAdapter = new TweetAdapter(mCursor, fragView, mShortAnimationDuration, mFragment);
                        mRecyclerView.setAdapter(mTweetAdapter);

                    }
                    mSwipeRefreshLayout.setRefreshing(false);



                }

                @Override
                public void failure(TwitterException e) {
                    Log.e(LOG_TAG, "Exception " + e);

                }
            });


        }
    }

    class TimelineExtender implements Runnable {

        @Override
        public void run() {

            Log.e(LOG_TAG, "Getting Tweets Extending!");
            long numItems = DatabaseUtils.queryNumEntries(mTimelineDB, "home") - 1;
            mCursor.moveToLast();
            long tweetId = mCursor.getLong(mCursor.getColumnIndex(BaseColumns._ID));
            final StatusesService service = Twitter.getApiClient().getStatusesService();

            service.homeTimeline(50, null, tweetId, null, null, null, true, new Callback<List<Tweet>>() {
                @Override
                public void success(Result<List<Tweet>> listResult) {
                    boolean statusChanges = false;
                    List<Tweet> list = listResult.data.subList(0,listResult.data.size());

                        for (Tweet t : list) {
                            try {
                                ContentValues tweetValues = mHelper.getValues(t);
                                mTimelineDB.insertOrThrow("home", null, tweetValues);
                                statusChanges = true;
                            }catch (Exception te) { Log.e(LOG_TAG, "Exception: " + te);}

                        }


                    if (statusChanges){
                        int rowLimit = 600;
                        if(DatabaseUtils.queryNumEntries(mTimelineDB, "home") > rowLimit) {
                            String deleteQuery = "DELETE FROM home WHERE "+BaseColumns._ID+" NOT IN " +
                                    "(SELECT "+BaseColumns._ID+" FROM home ORDER BY "+"update_time DESC " +
                                    "limit "+rowLimit+")";
                            mTimelineDB.execSQL(deleteQuery);
                        }
                        mCursor = mTimelineDB.query("home", null, null, null, null, null, "update_time DESC");
                        getActivity().startManagingCursor(mCursor);
                        mTweetAdapter = new TweetAdapter(mCursor, fragView, mShortAnimationDuration, mFragment);
                        mRecyclerView.setAdapter(mTweetAdapter);

                    }



                }

                @Override
                public void failure(TwitterException e) {
                    Log.e(LOG_TAG, "Exception " + e);

                }
            });


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
