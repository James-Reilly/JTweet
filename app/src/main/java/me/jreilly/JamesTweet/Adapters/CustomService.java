
/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.jreilly.JamesTweet.Adapters;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.models.User;

import me.jreilly.JamesTweet.Etc.twitterRelationship.TwitterRelationship;
import me.jreilly.JamesTweet.Etc.unfollow.DestroyObject;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by jamesreilly on 3/3/15.
 */
public interface CustomService {
    @GET("/1.1/users/show.json")
    void show(@Query("screen_name") String name,  Callback<User> cb);

    @GET("/1.1/users/show.json")
    void show(@Query("user_id") long id,  Callback<User> cb);

    @GET("/1.1/friendships/show.json")
    void show(@Query("source_screen_name") String sourceId, @Query("target_screen_name") String targetId, Callback<TwitterRelationship> cb );


    @POST("/1.1/friendships/destroy.json")
    void destroy(@Query("screen_name") String name, Callback<DestroyObject> cb);

    @POST("/1.1/friendships/create.json")
    void create(@Query("screen_name") String name, @Query("follow") Boolean follow, Callback<DestroyObject> cb);


}
