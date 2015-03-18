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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.User;

import me.jreilly.JamesTweet.Adapters.MyTwitterApiClient;
import me.jreilly.JamesTweet.R;

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
        final TextView charcaterCount = (TextView) findViewById(R.id.character_counter);
        Long id = Twitter.getSessionManager().getActiveSession().getUserId();
        MyTwitterApiClient test  = new MyTwitterApiClient(Twitter.getSessionManager().getActiveSession());


        final TextWatcher mTextEditorWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //This sets a textview to the current length
                charcaterCount.setText(String.valueOf(140 - s.length()));
            }

            public void afterTextChanged(Editable s) {
            }
        };
        tweetText.addTextChangedListener(mTextEditorWatcher);

        test.getCustomService().show(id, new Callback<User>() {
            @Override
            public void success(Result<User> result) {
                User u = result.data;
                ImageView mProfileImage = (ImageView) findViewById(R.id.compose_profile_img);
                TextView mProfileName = (TextView) findViewById(R.id.compose_name);
                Picasso.with(context).load(u.profileImageUrl).transform(new CircleTransform()).into(
                        mProfileImage
                );
                mProfileName.setText("@" + u.screenName);
            }

            @Override
            public void failure(TwitterException e) {
                Log.v("Compose: ", "Exception " + e);
            }
        });

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
    public class CircleTransform implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size/2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }


}
