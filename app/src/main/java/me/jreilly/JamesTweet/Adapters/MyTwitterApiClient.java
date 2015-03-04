package me.jreilly.JamesTweet.Adapters;

import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;

/**
 * Created by jamesreilly on 3/3/15.
 */
public class MyTwitterApiClient extends TwitterApiClient {
    public MyTwitterApiClient(TwitterSession session) {
        super(session);
    }

    /**
     * Provide CustomService with defined endpoints
     */
    public CustomService getCustomService() {
        return getService(CustomService.class);
    }
}