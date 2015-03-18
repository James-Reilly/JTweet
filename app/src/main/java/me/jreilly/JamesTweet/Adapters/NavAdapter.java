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
package me.jreilly.JamesTweet.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.User;

import me.jreilly.JamesTweet.R;

/**
 * Created by jreilly on 1/11/15.
 */
public class NavAdapter extends RecyclerView.Adapter<NavAdapter.ViewHolder> {

    private String[] mDataset;
    private Context mContext;
    private String mName;
    private String mProfileUrl;
    private int currentPosition;

    private static final int TYPE_HEADER = 0;  // Declaring Variable to Understand which View is being worked on
        // IF the view under inflation and population is header or Item
    private static final int TYPE_ITEM = 1;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mRowText;
        public ImageView mRowIcon;
        public int Holderid;
        public ImageView mBackground;
        public ViewHolder(View v, int type){
            super(v);
            if (type == TYPE_ITEM){
                mRowText = (TextView) v.findViewById(R.id.rowText);
                mRowIcon = (ImageView) v.findViewById(R.id.rowIcon);
                Holderid = 1;
            } else{
                mRowText = (TextView) v.findViewById(R.id.rowText);
                mRowIcon = (ImageView) v.findViewById(R.id.rowIcon);
                mBackground = (ImageView) v.findViewById(R.id.ic_background);
                Holderid = 0;
            }


        }
}

    public NavAdapter(String[] myDataset, Context context, String p, String pUrl){
        mDataset = myDataset;
        mContext = context;
        mName = p;
        mProfileUrl = pUrl;
    }

    @Override
    public NavAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_ITEM){
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.nav_item, viewGroup, false);

            return new ViewHolder(v, viewType);
        }else if (viewType == TYPE_HEADER){
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.nav_header, viewGroup, false);

            return new ViewHolder(v, viewType);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(final NavAdapter.ViewHolder viewHolder, int i) {

        if(viewHolder.Holderid == 1){
            if(mDataset[i - 1].equals("Timeline")){
                viewHolder.mRowIcon.setImageDrawable(mContext.getResources()
                        .getDrawable(R.drawable.ic_timeline));
            } else if(mDataset[i - 1].equals("Mentions")){
                viewHolder.mRowIcon.setImageDrawable(mContext.getResources()
                        .getDrawable(R.drawable.ic_mentions));
            } else if(mDataset[i - 1].equals("Favorites")){
                viewHolder.mRowIcon.setImageDrawable(mContext.getResources()
                        .getDrawable(R.drawable.ic_favorite));
            } else if(mDataset[i -1].equals("Settings")){
                viewHolder.mRowIcon.setImageDrawable(mContext.getResources()
                        .getDrawable(R.drawable.ic_settings_applications));
            }
            viewHolder.mRowText.setText(mDataset[i - 1]);

        } else {
            Twitter.getApiClient().getAccountService().verifyCredentials(true, null, new Callback<User>() {
                @Override
                public void success(Result<User> userResult) {
                    Picasso.with(viewHolder.mBackground.getContext()).load(userResult.data.profileBannerUrl)
                            .resize(viewHolder.mBackground.getWidth(), viewHolder.mBackground.getHeight()).into(
                            viewHolder.mBackground
                    );
                    Picasso.with(viewHolder.mRowIcon.getContext()).load(userResult.data.profileImageUrl).transform(new CircleTransform()).into(
                            viewHolder.mRowIcon
                    );

                    viewHolder.mRowText.setText(userResult.data.name);

                }

                @Override
                public void failure(TwitterException e) {

                }
            });

        }



    }
    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)){
            return TYPE_HEADER;
        }


        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    @Override
    public int getItemCount() {
        return mDataset.length + 1;
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
