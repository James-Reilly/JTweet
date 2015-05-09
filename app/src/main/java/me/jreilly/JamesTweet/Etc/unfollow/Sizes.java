package me.jreilly.JamesTweet.Etc.unfollow;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jamesreilly on 5/8/15.
 */
public class Sizes {

    private Large large;
    private Medium_ medium;
    private Small small;
    private Thumb thumb;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The large
     */
    public Large getLarge() {
        return large;
    }

    /**
     *
     * @param large
     * The large
     */
    public void setLarge(Large large) {
        this.large = large;
    }

    /**
     *
     * @return
     * The medium
     */
    public Medium_ getMedium() {
        return medium;
    }

    /**
     *
     * @param medium
     * The medium
     */
    public void setMedium(Medium_ medium) {
        this.medium = medium;
    }

    /**
     *
     * @return
     * The small
     */
    public Small getSmall() {
        return small;
    }

    /**
     *
     * @param small
     * The small
     */
    public void setSmall(Small small) {
        this.small = small;
    }

    /**
     *
     * @return
     * The thumb
     */
    public Thumb getThumb() {
        return thumb;
    }

    /**
     *
     * @param thumb
     * The thumb
     */
    public void setThumb(Thumb thumb) {
        this.thumb = thumb;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
