package me.jreilly.JamesTweet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.User;
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


public class ProfileFragment extends android.support.v4.app.Fragment implements ProfileSwitch {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mUserId;
    private Callback<List<Tweet>> mCallBack;
    private RealmAdapter mTweetAdapter;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Tweet> mTweetObjects = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RealmResults<TweetRealm> mDataset;
    private final String LOG_TAG = "TweetFetcher";

    private int mShortAnimationDuration;
    private View fragView;
    private ProfileSwitch mFragment;

    private User mUser;





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

        mCallBack = new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> listResult) {

                List<Tweet> list = listResult.data;
                Realm realm = Realm.getInstance(getActivity(), "profile.realm");
                Log.v("Tester 1", list.toString());

                for (Tweet t : list) {
                    try {
                        Log.v("Tester 1", t.text);
                        insertToRealm(t);
                    }catch (Exception te) { Log.e("NEW CONTEXT", "Exception: " + te);}

                }




                int rowLimit = 50;



                RealmResults<TweetRealm> result = realm.where(TweetRealm.class).findAll();


                result.sort("date", RealmResults.SORT_ORDER_DESCENDING);
                Log.v("Test", result.toString());

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
        Realm realm = Realm.getInstance(this.getActivity(), "profile.realm");
        realm.beginTransaction();
        realm.clear(TweetRealm.class);
        realm.commitTransaction();
        RealmResults<TweetRealm> result = realm.where(TweetRealm.class).findAll();
        result.sort("date", RealmResults.SORT_ORDER_DESCENDING);
        mDataset = result;
        getTweets(true);
        mTweetAdapter = new RealmAdapter(mDataset, fragView, mShortAnimationDuration, mFragment, mUserId );

        //apply the adapter to the timeline view
        //this will make it populate the new update data in the view
        mRecyclerView.setAdapter(mTweetAdapter);




        Log.e(LOG_TAG, "Finished Setup!");


        mRecyclerView.setOnScrollListener(
                new EndlessRecyclerOnScrollListener((LinearLayoutManager)mLayoutManager) {
                    @Override
                    public void onLoadMore() {


                    }
                });



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
    public Realm getRealm(){
        return Realm.getInstance(this.getActivity(), "profile.realm");
    }

    public void insertToRealm(Tweet t) {
        Realm realm = Realm.getInstance(getActivity(), "profile.realm");

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




    public void getTweets(Boolean pageRefresh){
        final StatusesService service = Twitter.getApiClient().getStatusesService();


        long id;
        Log.e(LOG_TAG, "Getting Tweets!");
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
        getRealm().close();

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
