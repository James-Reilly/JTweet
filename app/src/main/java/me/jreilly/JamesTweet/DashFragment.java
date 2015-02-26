package me.jreilly.JamesTweet;


import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import me.jreilly.JamesTweet.Adapters.RealmAdapter;
import me.jreilly.JamesTweet.Models.TweetRealm;
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
    private RealmAdapter mTweetAdapter;
    private BroadcastReceiver mTweetReciever;


    /*variables for the timeline queue */

    private boolean newPosts = false;


    /*Variables for updating the timeline */
    private TimelineUpdater mTimelineUpdater;

    private Callback<List<Tweet>> mCallBack;


    /*Variables for the adapter */
    private int mShortAnimationDuration;
    private View fragView;
    private ProfileSwitch mFragment;

    RealmResults<TweetRealm> mDataset;


    public DashFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //Inflate the Recyclerview
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_timeline);

        //Initilize the Classes that refresh the timeline and extend the timeline on scrolling;




        //Initialize Variables for Adapter and (ProfileSwitch)
        fragView = rootView;
        mFragment = this;
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);



        //Realm Test Code!





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


        //Sets up the timeline with a DB and a cursor
        //It also sets up the service
        setupTimeline();

        //Allows endless scrolling (theoretically)



        return rootView;
    }

    public Realm getRealm(){
        return Realm.getInstance(this.getActivity());
    }

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
    setupTimeline()

     Initializes the database helper, the database, the TweetAdapter to fill the Recycler view,
     and intializes the The Tweet Service which collects new tweets every 5 minutes.
     */
    public void setupTimeline() {

        try
        {


            //instantiate adapter

            //instantiate receiver class for finding out when new updates are available

            mCallBack = new Callback<List<Tweet>>() {
                @Override
                public void success(Result<List<Tweet>> listResult) {

                    List<Tweet> list = listResult.data;
                    Realm realm = Realm.getInstance(getActivity());

                    for (Tweet t : list) {
                        try {
                            insertToRealm(t);
                        }catch (Exception te) { Log.e("NEW CONTEXT", "Exception: " + te);}

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
            Realm realm = getRealm();
            RealmResults<TweetRealm> result = realm.where(TweetRealm.class).findAll();
            result.sort("date", RealmResults.SORT_ORDER_DESCENDING);

            mDataset = result;
            mTimelineUpdater = new TimelineUpdater();
            mTimelineUpdater.run();
            mTweetAdapter = new RealmAdapter(mDataset, fragView, mShortAnimationDuration, mFragment );

            //apply the adapter to the timeline view
            //this will make it populate the new update data in the view
            mRecyclerView.setAdapter(mTweetAdapter);




            Log.e(LOG_TAG, "Finished Setup!");

        }
        catch(Exception te) { Log.e(LOG_TAG, "Failed to fetch timeline: "+te.getMessage()); }
    }

    public void swapToProfile(String uId){
        Intent intent = new Intent(getActivity(), ProfileActivity.class)
                .putExtra(ProfileActivity.PROFILE_KEY, uId);
        mFab.hide();
        startActivity(intent);
    }

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
