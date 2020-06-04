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

    public DocumentLink(String documentName, String documentLink) {
        this.documentName = documentName;
        this.documentLink = documentLink;
    }

    private String documentName;
    private String documentLink;
}
