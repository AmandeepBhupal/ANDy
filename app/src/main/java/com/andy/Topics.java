package com.andy;

import java.util.ArrayList;

public class Topics {
    //private String topicid;
    private ArrayList<String> documents;
    private String topicDesc;
    public Topics(){

    }

    public ArrayList<String> getDocuments() {
        return documents;
    }

    public void setDocuments(ArrayList<String> documents) {
        this.documents = documents;
    }

    public String getTopicDesc() {
        return topicDesc;
    }

    public void setTopicDesc(String topicDesc) {
        this.topicDesc = topicDesc;
    }
}
