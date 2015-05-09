package me.jreilly.JamesTweet.Etc.twitterRelationship;

import java.util.HashMap;
import java.util.Map;




public class Relationship {

    private Target target;
    private Source source;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The target
     */
    public Target getTarget() {
        return target;
    }

    /**
     *
     * @param target
     * The target
     */
    public void setTarget(Target target) {
        this.target = target;
    }

    /**
     *
     * @return
     * The source
     */
    public Source getSource() {
        return source;
    }

    /**
     *
     * @param source
     * The source
     */
    public void setSource(Source source) {
        this.source = source;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}


