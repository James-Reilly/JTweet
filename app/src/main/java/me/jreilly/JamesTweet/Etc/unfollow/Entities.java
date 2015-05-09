package me.jreilly.JamesTweet.Etc.unfollow;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jamesreilly on 5/8/15.
 */
public class Entities {

    private Description description;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The description
     */
    public Description getDescription() {
        return description;
    }

    /**
     *
     * @param description
     * The description
     */
    public void setDescription(Description description) {
        this.description = description;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
