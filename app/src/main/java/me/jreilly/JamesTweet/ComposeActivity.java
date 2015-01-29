package me.jreilly.JamesTweet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;

import java.util.ArrayList;

import me.jreilly.JamesTweet.TweetParsers.ProfileSwitch;

public class ComposeActivity extends Activity {

    public static final String REPLY_USER = "reply_user";
    public static final String REPLY_ID = "reply_id";

    private long mReplyId;
    private String mReplyUser = "";






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpWindow();
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(REPLY_USER) && intent.hasExtra(REPLY_ID)){
            mReplyUser = intent.getStringExtra(REPLY_USER);
            mReplyId = intent.getLongExtra(REPLY_ID, 0);

        }
        setContentView(R.layout.activity_compose);

        final Context context = this;

        final EditText tweetText = (EditText) findViewById(R.id.tweetEditText);
        tweetText.append(mReplyUser);
        ImageButton createTweet = (ImageButton) findViewById(R.id.composeButton);

        createTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = tweetText.getText().toString();
                Twitter.getApiClient().getStatusesService().update(text, mReplyId, null, null, null, null, null, null, new Callback<Tweet>() {
                    @Override
                    public void success(Result<Tweet> tweetResult) {
                        Toast.makeText(context, "TWEETED!", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }

                    @Override
                    public void failure(TwitterException e) {

                    }
                });
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

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
        }

        return super.onOptionsItemSelected(item);
    }


    public void setUpWindow() {
        // Creates the layout for the window and the look of it

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // Params for the window.
        // You can easily set the alpha and the dim behind the window from here
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 1.0f;    // lower than one makes it more transparent
        params.dimAmount = 0.5f;  // set it higher if you want to dim behind the window
        getWindow().setAttributes(params);

        // Gets the display size so that you can set the window to a percent of that
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;


        // You could also easily used an integer value from the shared preferences to set the percent
        if (height > width) {
            getWindow().setLayout((int) (width * .9), (int) (height * .7));
        } else {
            getWindow().setLayout((int) (width * .7), (int) (height * .8));
        }

    }


}
