package com.appstaticsx.app.agmeapp;

public class Helper {
    private String email;
    private String password;
    private String profession;

    // Default constructor required for calls to DataSnapshot.getValue(Helper.class)
    public Helper() {
    }

    // Constructor with parameters
    public Helper(String email, String password, String profession) {
        this.email = email;
        this.password = password;
        this.profession = profession;
    }

    // Getter and Setter for email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    // Getter and Setter for profession
    public String getJob() {return profession; }

    public void setJob(String profession) {this.profession = profession;}


    // Getter and Setter for password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}