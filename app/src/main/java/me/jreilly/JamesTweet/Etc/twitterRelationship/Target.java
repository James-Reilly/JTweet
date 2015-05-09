package me.jreilly.JamesTweet.Etc.twitterRelationship;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jamesreilly on 5/7/15.
 */
public class Target {

    private String idStr;
    private int id;
    private String screenName;
    private boolean following;
    private boolean followedBy;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}

