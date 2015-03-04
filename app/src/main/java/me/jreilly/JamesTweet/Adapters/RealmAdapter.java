package me.jreilly.JamesTweet.Adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.RealmResults;
import me.jreilly.JamesTweet.Models.TweetRealm;
import me.jreilly.JamesTweet.R;
import me.jreilly.JamesTweet.TweetParsers.ProfileLink;
import me.jreilly.JamesTweet.TweetParsers.ProfileSwitch;
import retrofit.RestAdapter;

/**
 * Created by jamesreilly on 2/24/15.
 */
public class RealmAdapter extends RecyclerView.Adapter<RealmAdapter.ViewHolder>{
    private RealmResults<TweetRealm> mDataset;

    private String mProfileName;

    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;
    private View mFragView;

    private ProfileSwitch mActivity;

    private static final int TYPE_HEADER = 0;  // Declaring Variable to Understand which View is being worked on
    // IF the view under inflation and population is header or Item
    private static final int TYPE_ITEM = 1;

    private int lastPosition  = 5;

    boolean mNetwork;
    private Context context;

    RestAdapter mRestAdpater;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTweet;
        public TextView mUser;
        public ImageButton mImage;
        public ImageButton mProfileImage;
        public LinearLayout mContainer;
        public TextView mRetweeted;
        public TextView mTime;


        public TextView mFollowers;
        public TextView mFollowing;
        public TextView mDescription;
        public ImageView mBackground;
        public TextView mScreenName;
        public LinearLayout mTextUnderlay;

        public int Holderid;




        public View mlayout;
        public ViewHolder(View list_item, int type){
            super(list_item);
            if (type == TYPE_ITEM){
                mUser = (TextView) list_item.findViewById(R.id.my_user);
                mTweet = (TextView) list_item.findViewById(R.id.my_text);
                mImage = (ImageButton) list_item.findViewById(R.id.my_picture);
                mProfileImage = (ImageButton) list_item.findViewById(R.id.user_image);
                mContainer = (LinearLayout) list_item.findViewById(R.id.item_layout_container);
                mRetweeted = (TextView) list_item.findViewById(R.id.my_retweeted);
                mTime = (TextView) list_item.findViewById(R.id.my_time);
                Holderid = 1;

            }else if ( type== TYPE_HEADER){
                mUser = (TextView) list_item.findViewById(R.id.my_user);
                mDescription = (TextView) list_item.findViewById(R.id.my_description);
                mBackground = (ImageView) list_item.findViewById(R.id.ic_background);
                mProfileImage = (ImageButton) list_item.findViewById(R.id.user_image);
                mContainer = (LinearLayout) list_item.findViewById(R.id.item_layout_container);
                mFollowers = (TextView) list_item.findViewById(R.id.my_followers);
                mFollowing = (TextView) list_item.findViewById(R.id.my_following);
                mScreenName = (TextView) list_item.findViewById(R.id.my_screename);
                mTextUnderlay = (LinearLayout) list_item.findViewById(R.id.linear_back);
                Holderid = 0;
            }



        }
    }

    public RealmAdapter(RealmResults<TweetRealm> realmResults, View fragView, int time, ProfileSwitch Activity, String profile){

        mDataset = realmResults;
        mShortAnimationDuration = time;
        mFragView = fragView;
        mActivity = Activity;
        mProfileName = profile;
        mRestAdpater = new RestAdapter.Builder()
                .setEndpoint("https://api.twitter.com")
                .build();

    }

    @Override
    public RealmAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        if (i == TYPE_HEADER){
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.profile_card, viewGroup, false);
            return new ViewHolder(v, i);
        }else if(i == TYPE_ITEM){
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.text_layout, viewGroup, false);
            return new ViewHolder(v, i);
        }


        return null;
    }

    @Override
    public void onBindViewHolder(final RealmAdapter.ViewHolder viewHolder, int i) {

        if(viewHolder.Holderid == TYPE_ITEM){
            if(mProfileName != null){
                i--;
            }
            String user_img = mDataset.get(i).getProfileImageUrl();
            final String user_screen = mDataset.get(i).getScreename();
            String media_url = mDataset.get(i).getMediaUrl();
            Date created = mDataset.get(i).getDate();
            boolean retweeted = mDataset.get(i).isRetweetedStatus();
            String original = mDataset.get(i).getRetweetedBy();
            String username = mDataset.get(i).getName();
            String text = mDataset.get(i).getText();
            final long tId = mDataset.get(i).getId();


            //Load Profile Image
            Picasso.with(viewHolder.mProfileImage.getContext()).load(user_img).transform(new CircleTransform()).into(
                    viewHolder.mProfileImage
            );

            //Set profile image to go to the users profile
            viewHolder.mProfileImage.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mActivity.swapToProfile(user_screen);
                }
            });

            final String imageUrl = media_url;
            ViewGroup.LayoutParams params =  viewHolder.mImage.getLayoutParams();

            //Set Cropped Media image and zoomImage animation
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
                //Media is not need so it is hidden.
                viewHolder.mImage.setImageDrawable(null);

                viewHolder.mImage.getLayoutParams().height = 0;

            }

            String retweetText = "";

            //Set "Retweeted By " Text
            if (retweeted){
                viewHolder.mRetweeted.setVisibility(View.VISIBLE);
                retweetText = "Retweeted by @";
                retweetText += original;
                viewHolder.mRetweeted.setText(retweetText);
            }else {
                viewHolder.mRetweeted.setText(null);
                viewHolder.mRetweeted.setVisibility(View.GONE);
            }

            //Set Username Text Field
            Calendar cal = Calendar.getInstance();
            viewHolder.mTime.setText(DateUtils.getRelativeTimeSpanString(created.getTime()));
            viewHolder.mUser.setText(username);
            String tweetText = text;

            //Highlight Profile names/hashtags and their clickable spans
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

            View.OnClickListener detailTweet =  new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mActivity.swapToTweet(tId);



                }
            };
            viewHolder.mTweet.setOnClickListener( detailTweet );
            viewHolder.mUser.setOnClickListener( detailTweet );
        /*
        setAnimation(viewHolder.mContainer, i);
        */

        }else if (viewHolder.Holderid == TYPE_HEADER){


            MyTwitterApiClient test  = new MyTwitterApiClient(Twitter.getSessionManager().getActiveSession());
            test.getCustomService().show(mProfileName, new Callback<User>() {
                @Override
                public void success(Result<User> results) {
                    User mProfile = results.data;

                    Picasso.with(viewHolder.mProfileImage.getContext()).load(mProfile.profileImageUrl).transform(new CircleTransform()).into(
                            viewHolder.mProfileImage
                    );
                    Picasso.with(viewHolder.mBackground.getContext()).load(mProfile.profileBannerUrl)
                            .resize(viewHolder.mBackground.getWidth(), viewHolder.mBackground.getHeight()).into(
                            viewHolder.mBackground
                    );
                    viewHolder.mDescription.setText(mProfile.description);
                    viewHolder.mUser.setText(mProfile.name);
                    viewHolder.mScreenName.setText("@"+mProfile.screenName);
                    viewHolder.mFollowers.setText("Followers: " + mProfile.followersCount);
                    viewHolder.mFollowing.setText("Following: " + mProfile.friendsCount);
                    viewHolder.mTextUnderlay.setBackgroundColor(Color.parseColor("#AA333333"));

                }

                @Override
                public void failure(TwitterException e) {

                }
            });





        }


    }

    @Override
    public int getItemCount() {
        if (mProfileName != null){
            return mDataset.size() + 1;
        }else{
            return mDataset.size();
        }


    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position) && mProfileName != null){
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }
    public RealmResults<TweetRealm> getRealmResults() {
        return mDataset;
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
    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
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



    // example users/show service endpoint

}
