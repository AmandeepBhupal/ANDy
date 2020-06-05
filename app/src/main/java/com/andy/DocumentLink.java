package com.andy;

public class DocumentLink {
    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getDocumentLink() {
        return documentLink;
    }

    public void setDocumentLink(String documentLink) {
        this.documentLink = documentLink;
    }

    public String getDocumentDescription(){return documentDescription;}


    public DocumentLink(String documentName, String documentLink) {
        this.documentName = documentName;
        this.documentLink = documentLink;
    }

    private String documentName;

    public DocumentLink(String documentName, String documentLink, String documentDescription, String timestamp) {
        this.documentName = documentName;
        this.documentLink = documentLink;
        this.documentDescription = documentDescription;
        this.timestamp = timestamp;
    }

    private String documentLink;
    private String documentDescription;

    public void setDocumentDescription(String documentDescription) {
        this.documentDescription = documentDescription;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    private String timestamp;
}
