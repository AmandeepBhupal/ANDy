package com.andy;

import java.util.ArrayList;

public class Profile {
    private String Name;
    private String Email;
    private ArrayList<String> Documents;
    private ArrayList<String> Tags;
    private String Profilepicture;
    public Profile(){

    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        this.Email = email;
    }

    public ArrayList<String> getDocuments() {
        return Documents;
    }

    public void setDocuments(ArrayList<String> documents) {
        Documents = documents;
    }

    public ArrayList<String> getTags() {
        return Tags;
    }

    public void setTags(ArrayList<String> tags) {
        Tags = tags;
    }

    public String getProfilePicture() {
        return Profilepicture;
    }

    public void setProfilePicture(String profilePicture) {
        Profilepicture = profilePicture;
    }
}
