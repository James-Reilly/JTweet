package me.jreilly.JamesTweet.Adapters;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.models.User;

import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by jamesreilly on 3/3/15.
 */
public interface CustomService {
    @GET("/1.1/users/show.json")
    void show(@Query("screen_name") String name,  Callback<User> cb);

    @GET("/1.1/users/show.json")
    void show(@Query("user_id") long id,  Callback<User> cb);


}
