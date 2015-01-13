package me.jreilly.JamesTweet;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.twitter.sdk.android.core.models.Tweet;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by jreilly on 1/9/15.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private ArrayList<Tweet> mDataset;
    private int currentPosition;
    private Cursor mCursor;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTweet;
        public TextView mUser;
        public ImageView mImage;
        public ImageView mProfileImage;
        public ViewHolder(View v){
            super(v);
            mUser = (TextView) v.findViewById(R.id.my_user);
            mTweet = (TextView) v.findViewById(R.id.my_text);
            mImage = (ImageView) v.findViewById(R.id.my_picture);
            mProfileImage = (ImageView) v.findViewById(R.id.user_image);
        }
    }

    public MyAdapter(ArrayList<Tweet> myDataset){
        mDataset = myDataset;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.text_layout, viewGroup, false);

        currentPosition = i;

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyAdapter.ViewHolder viewHolder, int i) {
        viewHolder.mUser.setText(mDataset.get(i).user.name + " - @" +
                mDataset.get(i).user.screenName);
        viewHolder.mTweet.setText(mDataset.get(i).text);
        /*
        if(!(mDataset.get(i).entities.equals(null))){
            for(int j = 0; j < mDataset.get(i).entities.media.size(); j++){
                if(mDataset.get(i).entities.media.get(j).type.equals("photo")){
                    viewHolder.mImage.setImageBitmap(
                            getBitmapFromURL(mDataset.get(i).entities.media.get(j).mediaUrl)
                    );
                }
            }
        }*/
        new LoadImageTask(mDataset.get(i).user.profileImageUrl,
                viewHolder.mProfileImage).execute();





    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }




}
