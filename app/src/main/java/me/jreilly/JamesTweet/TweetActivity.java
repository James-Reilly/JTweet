package me.jreilly.JamesTweet;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import me.jreilly.JamesTweet.R;
import me.jreilly.JamesTweet.TweetParsers.ProfileSwitch;

public class TweetActivity extends ActionBarActivity implements ProfileSwitch {


    public static final String TWEET_KEY = "tweet_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new TweetFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tweet, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
    public void swapToProfile(String uId){
        Intent intent = new Intent(this, ProfileActivity.class)
                .putExtra(ProfileActivity.PROFILE_KEY, uId);
        startActivity(intent);
    }
    public void swapToTweet(long tweetId){
        Intent intent = new Intent(this, TweetActivity.class)
                .putExtra(TweetActivity.TWEET_KEY, tweetId);
        startActivity(intent);
    }


}
