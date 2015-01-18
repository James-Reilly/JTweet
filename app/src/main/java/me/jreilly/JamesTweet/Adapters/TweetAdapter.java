package me.jreilly.JamesTweet.Adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.jreilly.JamesTweet.R;
import me.jreilly.JamesTweet.TweetParsers.ProfileLink;
import me.jreilly.JamesTweet.TweetParsers.ProfileSwitch;

/**
 * Created by jreilly on 1/12/15.
 */
public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {

    private static final int TYPE_TEXT_ONLY = 0;
    private static final int TYPE_IMAGE = 0;
    private static final int TYPE_URL = 0;



    private Cursor mCursor;

    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;
    private View mFragView;

    private ProfileSwitch mActivity;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTweet;
        public TextView mUser;
        public ImageButton mImage;
        public ImageButton mProfileImage;

        public View mlayout;
        public ViewHolder(View list_item){
            super(list_item);
            mUser = (TextView) list_item.findViewById(R.id.my_user);
            mTweet = (TextView) list_item.findViewById(R.id.my_text);
            mImage = (ImageButton) list_item.findViewById(R.id.my_picture);
            mProfileImage = (ImageButton) list_item.findViewById(R.id.user_image);

        }
    }

    public TweetAdapter(Cursor cursor, View fragView, int time, ProfileSwitch Activity){

        mCursor = cursor;
        mShortAnimationDuration = time;
        mFragView = fragView;
        mActivity = Activity;
    }

    @Override
    public TweetAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.text_layout, viewGroup, false);




        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final TweetAdapter.ViewHolder viewHolder, final int i) {

        if(!mCursor.moveToPosition(i)){
            Log.e("TweetAdapter", "Illegal State Exception!");

        } else {


            Picasso.with(viewHolder.mProfileImage.getContext()).load(mCursor.getString(
                    mCursor.getColumnIndex("user_img"))).into(
                    viewHolder.mProfileImage
            );
            viewHolder.mProfileImage.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(!mCursor.moveToPosition(i)){
                        Log.e("TweetAdapter", "Illegal State Exception!");

                    } else {
                        mActivity.swapToProfile(
                                mCursor.getString(mCursor.getColumnIndex("user_screen")));
                    }
                }
            });
            final String imageUrl = mCursor.getString(mCursor.getColumnIndex("update_media"));
            ViewGroup.LayoutParams params =  viewHolder.mImage.getLayoutParams();
            if (!imageUrl.equals("null")){

                viewHolder.mImage.getLayoutParams().height = 400;
                Picasso.with(viewHolder.mImage.getContext()).load(imageUrl).fit().centerCrop().into(
                        viewHolder.mImage
                );

                viewHolder.mImage.setOnClickListener( new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        zoomImageFromThumb(viewHolder.mImage, mFragView, imageUrl);

                    }
                });
            } else {
                viewHolder.mImage.setImageDrawable(null);

                viewHolder.mImage.getLayoutParams().height = 0;

            }




            String createdAt = mCursor.getString(mCursor.getColumnIndex("update_time"));
            viewHolder.mUser.setText(mCursor.getString(mCursor.getColumnIndex("user_screen")));
            String tweetText = mCursor.getString(mCursor.getColumnIndex("update_text"));

            ArrayList<int[]> hashtagSpans = getSpans(tweetText, '#');
            ArrayList<int[]> profileSpans = getSpans(tweetText, '@');

            SpannableString tweetContent = new SpannableString(tweetText);

            for( int j = 0; j < profileSpans.size(); j ++){
                int[] span = profileSpans.get(j);
                int profileStart = span[0];
                int profileEnd = span[1];

                tweetContent.setSpan(new ProfileLink(viewHolder.mTweet.getContext(), mActivity),
                        profileStart, profileEnd, 0);
            }
            viewHolder.mTweet.setMovementMethod(LinkMovementMethod.getInstance());
            viewHolder.mTweet.setText(tweetContent);



        }

    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public ArrayList<int[]> getSpans(String body, char prefix) {
        ArrayList<int[]> spans = new ArrayList<int[]>();

        Pattern pattern = Pattern.compile(prefix + "\\w+");
        Matcher matcher = pattern.matcher(body);

        // Check all occurrences
        while (matcher.find()) {
            int[] currentSpan = new int[2];
            currentSpan[0] = matcher.start();
            currentSpan[1] = matcher.end();
            spans.add(currentSpan);
        }

        return  spans;
    }


    private void zoomImageFromThumb(final View thumbView, final View mainView ,String imageResUrl) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) mainView.findViewById(
                R.id.expanded_image);
        Picasso.with(expandedImageView.getContext()).load(imageResUrl).into(
                expandedImageView);


        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        mainView
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;


        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;

        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }




}