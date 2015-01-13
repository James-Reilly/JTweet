package me.jreilly.JamesTweet;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.twitter.sdk.android.core.models.Tweet;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by jreilly on 1/12/15.
 */
public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {

    private ArrayList<Tweet> mDataset;
    private int currentPosition;
    private Cursor mCursor;
    private ImageLoader mImageLoader;

    /**strings representing database column names to map to views*/
    static final String[] from = { "update_text", "user_screen",
            "update_time", "user_img" };

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTweet;
        public TextView mUser;
        public ImageView mImage;
        public NetworkImageView mProfileImage;
        public ViewHolder(View v){
            super(v);
            mUser = (TextView) v.findViewById(R.id.my_user);
            mTweet = (TextView) v.findViewById(R.id.my_text);
            mImage = (ImageView) v.findViewById(R.id.my_picture);
            mProfileImage = (NetworkImageView) v.findViewById(R.id.user_image);

        }
    }

    public TweetAdapter(Cursor cursor){
        mImageLoader = VolleySingleton.getInstance().getImageLoader();
        mCursor = cursor;
    }

    @Override
    public TweetAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.text_layout, viewGroup, false);



        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(TweetAdapter.ViewHolder viewHolder, int i) {

        if(!mCursor.moveToPosition(i)){
            Log.e("TweetAdapter", "Illegal State Exception!");

        } else {

            try {

                viewHolder.mProfileImage.setImageUrl(mCursor.getString(mCursor.getColumnIndex("user_img")), mImageLoader);
            } catch (Exception te) {
                Log.e("TweetAdapter", "Error" + te.getMessage());
            }


            String createdAt = mCursor.getString(mCursor.getColumnIndex("update_time"));
            viewHolder.mUser.setText(mCursor.getString(mCursor.getColumnIndex("user_screen")))
            ;
            viewHolder.mTweet.setText(mCursor.getString(mCursor.getColumnIndex("update_text")));

        }

    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }




}