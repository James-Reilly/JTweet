package me.jreilly.JamesTweet.Etc.unfollow;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jamesreilly on 5/8/15.
 */
public class Large {

    private int w;
    private String resize;
    private int h;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The w
     */
    public int getW() {
        return w;
    }

    /**
     *
     * @param w
     * The w
     */
    public void setW(int w) {
        this.w = w;
    }

    /**
     *
     * @return
     * The resize
     */
    public String getResize() {
        return resize;
    }

    /**
     *
     * @param resize
     * The resize
     */
    public void setResize(String resize) {
        this.resize = resize;
    }

    /**
     *
     * @return
     * The h
     */
    public int getH() {
        return h;
    }

    /**
     *
     * @param h
     * The h
     */
    public void setH(int h) {
        this.h = h;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
