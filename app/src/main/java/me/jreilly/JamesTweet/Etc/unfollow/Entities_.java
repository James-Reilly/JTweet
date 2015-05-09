package me.jreilly.JamesTweet.Etc.unfollow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jamesreilly on 5/8/15.
 */
public class Entities_ {

    private List<Object> urls = new ArrayList<Object>();
    private List<Medium> media = new ArrayList<Medium>();
    private List<Object> hashtags = new ArrayList<Object>();
    private List<Object> userMentions = new ArrayList<Object>();
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The urls
     */
    public List<Object> getUrls() {
        return urls;
    }

    /**
     *
     * @param urls
     * The urls
     */
    public void setUrls(List<Object> urls) {
        this.urls = urls;
    }

    /**
     *
     * @return
     * The media
     */
    public List<Medium> getMedia() {
        return media;
    }

    /**
     *
     * @param media
     * The media
     */
    public void setMedia(List<Medium> media) {
        this.media = media;
    }

    /**
     *
     * @return
     * The hashtags
     */
    public List<Object> getHashtags() {
        return hashtags;
    }

    /**
     *
     * @param hashtags
     * The hashtags
     */
    public void setHashtags(List<Object> hashtags) {
        this.hashtags = hashtags;
    }

    /**
     *
     * @return
     * The userMentions
     */
    public List<Object> getUserMentions() {
        return userMentions;
    }

    /**
     *
     * @param userMentions
     * The user_mentions
     */
    public void setUserMentions(List<Object> userMentions) {
        this.userMentions = userMentions;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
