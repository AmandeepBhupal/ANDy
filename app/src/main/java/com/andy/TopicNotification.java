package com.andy;

public class TopicNotification {
    private String topicDescription;
    private String topicTitle;
    private String key;

    public TopicNotification(String topicDescription, String topicTitle, String key) {
        this.topicDescription = topicDescription;
        this.topicTitle = topicTitle;
        this.key = key;
    }

    public String getTopicDescription() {
        return topicDescription;
    }

    public void setTopicDescription(String topicDescription) {
        this.topicDescription = topicDescription;
    }

    public String getTopicTitle() {
        return topicTitle;
    }

    public void setTopicTitle(String topicTitle) {
        this.topicTitle = topicTitle;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
