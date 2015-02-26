package me.jreilly.JamesTweet.Models;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by jamesreilly on 2/24/15.
 */
public class TweetRealm extends RealmObject {
    @PrimaryKey private long id;
    private long originalId;

    public long getOriginalId() {
        return originalId;
    }

    public void setOriginalId(long originalId) {
        this.originalId = originalId;
    }

    private String name;
    private String screename;
    private Date date;
    private String text;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    private String mediaUrl;
    private String profileImageUrl;
    private String retweetedBy;

    public String getRetweetedBy() {
        return retweetedBy;
    }

    public void setRetweetedBy(String retweetedBy) {
        this.retweetedBy = retweetedBy;
    }

    private boolean retweetedStatus;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScreename() {
        return screename;
    }

    public void setScreename(String screename) {
        this.screename = screename;
    }



    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public boolean isRetweetedStatus() {
        return retweetedStatus;
    }

    public void setRetweetedStatus(boolean retweetedStatus) {
        this.retweetedStatus = retweetedStatus;
    }

    public long getId() {
        return id;

    }

    public void setId(long id) {
        this.id = id;
    }
}
