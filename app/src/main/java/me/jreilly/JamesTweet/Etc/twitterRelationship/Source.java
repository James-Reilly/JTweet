package me.jreilly.JamesTweet.Etc.twitterRelationship;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jamesreilly on 5/7/15.
 */
public class Source {

    private boolean canDm;
    private Object blocking;
    private Object muting;
    private String idStr;
    private Object allReplies;
    private Object wantRetweets;
    private int id;
    private Object markedSpam;
    private String screenName;
    private boolean following;
    private boolean followedBy;
    private Object notificationsEnabled;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The canDm
     */
    public boolean isCanDm() {
        return canDm;
    }

    /**
     *
     * @param canDm
     * The can_dm
     */
    public void setCanDm(boolean canDm) {
        this.canDm = canDm;
    }

    /**
     *
     * @return
     * The blocking
     */
    public Object getBlocking() {
        return blocking;
    }

    /**
     *
     * @param blocking
     * The blocking
     */
    public void setBlocking(Object blocking) {
        this.blocking = blocking;
    }

    /**
     *
     * @return
     * The muting
     */
    public Object getMuting() {
        return muting;
    }

    /**
     *
     * @param muting
     * The muting
     */
    public void setMuting(Object muting) {
        this.muting = muting;
    }

    /**
     *
     * @return
     * The idStr
     */
    public String getIdStr() {
        return idStr;
    }

    /**
     *
     * @param idStr
     * The id_str
     */
    public void setIdStr(String idStr) {
        this.idStr = idStr;
    }

    /**
     *
     * @return
     * The allReplies
     */
    public Object getAllReplies() {
        return allReplies;
    }

    /**
     *
     * @param allReplies
     * The all_replies
     */
    public void setAllReplies(Object allReplies) {
        this.allReplies = allReplies;
    }

    /**
     *
     * @return
     * The wantRetweets
     */
    public Object getWantRetweets() {
        return wantRetweets;
    }

    /**
     *
     * @param wantRetweets
     * The want_retweets
     */
    public void setWantRetweets(Object wantRetweets) {
        this.wantRetweets = wantRetweets;
    }

    /**
     *
     * @return
     * The id
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The markedSpam
     */
    public Object getMarkedSpam() {
        return markedSpam;
    }

    /**
     *
     * @param markedSpam
     * The marked_spam
     */
    public void setMarkedSpam(Object markedSpam) {
        this.markedSpam = markedSpam;
    }

    /**
     *
     * @return
     * The screenName
     */
    public String getScreenName() {
        return screenName;
    }

    /**
     *
     * @param screenName
     * The screen_name
     */
    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    /**
     *
     * @return
     * The following
     */
    public boolean isFollowing() {
        return following;
    }

    /**
     *
     * @param following
     * The following
     */
    public void setFollowing(boolean following) {
        this.following = following;
    }

    /**
     *
     * @return
     * The followedBy
     */
    public boolean isFollowedBy() {
        return followedBy;
    }

    /**
     *
     * @param followedBy
     * The followed_by
     */
    public void setFollowedBy(boolean followedBy) {
        this.followedBy = followedBy;
    }

    /**
     *
     * @return
     * The notificationsEnabled
     */
    public Object getNotificationsEnabled() {
        return notificationsEnabled;
    }

    /**
     *
     * @param notificationsEnabled
     * The notifications_enabled
     */
    public void setNotificationsEnabled(Object notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}

