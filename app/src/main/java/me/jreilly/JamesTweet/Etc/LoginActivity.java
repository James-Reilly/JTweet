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
package me.jreilly.JamesTweet.Etc;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;
import me.jreilly.JamesTweet.Dashboard.DashActivity;
import me.jreilly.JamesTweet.R;


public class LoginActivity extends ActionBarActivity {
    TwitterLoginButton mLoginButton;
    private static final String TWITTER_KEY = "u3rtb2wblcZAS4SxsSwx4fcb5";
    private static final String TWITTER_SECRET = "NoT5fueZXHwRRnka9l0glPyojXtw64z5bnOd0RJlObSEKfkH4H";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fabric Authentication
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);

        Fabric.with(this, new Twitter(authConfig));
        //Set Login View
        setContentView(R.layout.activity_login);

        //Check if there is a current session and skip to dashboard if there is
        TwitterSession session = Twitter.getSessionManager().getActiveSession();
        if (session != null){
            startDashboard();
        }

        //Load the login button and assign it a CallBack
        mLoginButton = (TwitterLoginButton)
                findViewById(R.id.login_button);
        mLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                //Starts the main activity

                startDashboard();

            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
            }
        });


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the login button.
        mLoginButton.onActivityResult(requestCode, resultCode,
                data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

    public void startDashboard(){
        final Intent mIntent = new Intent(LoginActivity.this,
                DashActivity.class);

        startActivity(mIntent);
        finish();
    }
}
