package me.jreilly.JamesTweet.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import me.jreilly.JamesTweet.R;

/**
 * Created by jreilly on 1/11/15.
 */
public class NavAdapter extends RecyclerView.Adapter<NavAdapter.ViewHolder> {

private String[] mDataset;
private Context mContext;
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

    public NavAdapter(String[] myDataset, Context context){
        mDataset = myDataset;
        mContext = context;
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

        if(mDataset[i].equals("Timeline")){
            viewHolder.mRowIcon.setImageDrawable(mContext.getResources()
                    .getDrawable(R.drawable.ic_timeline));
        } else if(mDataset[i].equals("Mentions")){
            viewHolder.mRowIcon.setImageDrawable(mContext.getResources()
                    .getDrawable(R.drawable.ic_mentions));
        } else if(mDataset[i].equals("Favorites")){
            viewHolder.mRowIcon.setImageDrawable(mContext.getResources()
                    .getDrawable(R.drawable.ic_favorite));
        } else if(mDataset[i].equals("Settings")){
            viewHolder.mRowIcon.setImageDrawable(mContext.getResources()
                    .getDrawable(R.drawable.ic_settings_applications));
        }

        viewHolder.mRowText.setText(mDataset[i]);
    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }


}
