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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.ScrollDirectionListener;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.ArrayList;
import java.util.List;

import me.jreilly.JamesTweet.Adapters.TweetAdapter;
import me.jreilly.JamesTweet.TweetParsers.ProfileSwitch;


/**
 * A simple {@link Fragment} subclass.
 */
public class DashFragment extends android.support.v4.app.Fragment implements ProfileSwitch {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Tweet> mTweetObjects = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private final String LOG_TAG = "DashFragment";
    private int mMaxItems = 100;

    private FloatingActionButton mFab;

    private PopupWindow popUp;
    private LinearLayout mainLayout;



    /*variables for the timeline database and reciever */
    private TweetDataHelper mHelper;
    private SQLiteDatabase mTimelineDB;
    private Cursor mCursor;
    private Cursor mCursorAdapter;
    private TweetAdapter mTweetAdapter;
    private BroadcastReceiver mTweetReciever;


    /*Variables for updating the timeline */
    private TimelineUpdater mTimelineUpdater;
    private TimelineExtender mTimelineExtender;
    private Callback<List<Tweet>> mCallBack;


    /*Variables for the adapter */
    private int mShortAnimationDuration;
    private View fragView;
    private ProfileSwitch mFragment;


    public DashFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //Inflate the Recyclerview
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_timeline);

        //Initilize the Classes that refresh the timeline and extend the timeline on scrolling;
        mTimelineUpdater = new TimelineUpdater();
        mTimelineExtender = new TimelineExtender();


        //Initialize Variables for Adapter and (ProfileSwitch)
        fragView = rootView;
        mFragment = this;
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);




        //Enable pull down to refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(){

                mTimelineUpdater.run();
            }

        });

        //Initialize the layout to a LinearLayout
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        popUp = new PopupWindow(getActivity());

        mFab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        mFab.attachToRecyclerView(mRecyclerView);
        mFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ComposeActivity.class);
                mFab.hide();
                startActivity(intent);
            }
        });


        //Sets up the timeline with a DB and a cursor
        //It also sets up the service
        setupTimeline();

        //Allows endless scrolling (theoretically)
        mRecyclerView.setOnScrollListener(
                new EndlessRecyclerOnScrollListener((LinearLayoutManager)mLayoutManager) {


                  @Override
                  public void onLoadMore() {

                      mTimelineExtender.run();

                  }
              });


        return rootView;
    }

    /**
    setupTimeline()

     Initializes the database helper, the database, the TweetAdapter to fill the Recycler view,
     and intializes the The Tweet Service which collects new tweets every 5 minutes.
     */
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
        Intent intent = new Intent(getActivity(), ProfileActivity.class)
                .putExtra(ProfileActivity.PROFILE_KEY, uId);
        startActivity(intent);
    }

    public void swapToTweet(long tweetId){
        Intent intent = new Intent(getActivity(), TweetActivity.class)
                .putExtra(TweetActivity.TWEET_KEY, tweetId);
        startActivity(intent);
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
                Log.v(LOG_TAG, "Deleteing Tweets!");
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

                        for (Tweet t : listResult.data) {
                            try {
                            ContentValues tweetValues = mHelper.getValues(t);
                            mTimelineDB.insertOrThrow("home", null, tweetValues);
                            statusChanges = true;


                        } catch (Exception te) { Log.e(LOG_TAG, "Exception: " + te);
                            }
                    }


                    int rowLimit = mMaxItems;
                    if(DatabaseUtils.queryNumEntries(mTimelineDB, "home") > rowLimit) {
                        String deleteQuery = "DELETE FROM home WHERE "+BaseColumns._ID+" NOT IN " +
                                "(SELECT "+BaseColumns._ID+" FROM home ORDER BY "+"update_time DESC " +
                                "limit "+rowLimit+")";
                        mTimelineDB.execSQL(deleteQuery);
                        Log.v(LOG_TAG, "Deleteing Tweets!");
                    }
                    mCursor = mTimelineDB.query("home", null, null, null, null, null, "update_time DESC");
                    getActivity().startManagingCursor(mCursor);
                    mTweetAdapter = new TweetAdapter(mCursor, fragView, mShortAnimationDuration, mFragment);
                    mRecyclerView.setAdapter(mTweetAdapter);


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
            final int position = mCursor.getPosition();
            mCursor.moveToLast();
            long tweetId = mCursor.getLong(mCursor.getColumnIndex(BaseColumns._ID));
            final StatusesService service = Twitter.getApiClient().getStatusesService();

            service.homeTimeline(50, null, tweetId, null, null, null, true, new Callback<List<Tweet>>() {
                @Override
                public void success(Result<List<Tweet>> listResult) {

                    long numEntries = DatabaseUtils.queryNumEntries(mTimelineDB, "home");
                    List<Tweet> list = listResult.data.subList(0,listResult.data.size());

                        for (Tweet t : list) {
                            try {
                                ContentValues tweetValues = mHelper.getValues(t);
                                mTimelineDB.insertOrThrow("home", null, tweetValues);

                            }catch (Exception te) { Log.e(LOG_TAG, "Exception: " + te);}

                        }



                        int rowLimit = mMaxItems;

                        if(DatabaseUtils.queryNumEntries(mTimelineDB, "home") > rowLimit) {
                            String deleteQuery = "DELETE FROM home WHERE "+BaseColumns._ID+" NOT IN " +
                                    "(SELECT "+BaseColumns._ID+" FROM home ORDER BY "+"update_time DESC " +
                                    "limit "+rowLimit+")";
                            mTimelineDB.execSQL(deleteQuery);
                            Log.v(LOG_TAG, "Deleteing Tweets!");
                        }
                        mCursor = mTimelineDB.query("home", null, null, null, null, null, "update_time DESC");
                        getActivity().startManagingCursor(mCursor);
                        mTweetAdapter = new TweetAdapter(mCursor, fragView, mShortAnimationDuration, mFragment);
                        mRecyclerView.setAdapter(mTweetAdapter);


                    mRecyclerView.scrollToPosition(position);




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
