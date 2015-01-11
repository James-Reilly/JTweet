package me.jreilly.JamesTweet;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.twitter.sdk.android.core.models.Tweet;

import java.util.ArrayList;

/**
 * Created by jreilly on 1/11/15.
 */
public class NavAdapter extends RecyclerView.Adapter<NavAdapter.ViewHolder> {

private String[] mDataset;
private int currentPosition;

public static class ViewHolder extends RecyclerView.ViewHolder {
    public TextView mRowText;
    public ImageView mRowIcon;
    public ViewHolder(View v){
        super(v);
        mRowText = (TextView) v.findViewById(R.id.rowText);
        mRowIcon = (ImageView) v.findViewById(R.id.rowIcon);

    }
}

    public NavAdapter(String[] myDataset){
        mDataset = myDataset;
    }

    @Override
    public NavAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.nav_item, viewGroup, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(NavAdapter.ViewHolder viewHolder, int i) {
        viewHolder.mRowText.setText(mDataset[i]);
    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }


}
