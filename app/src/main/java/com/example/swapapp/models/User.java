package com.example.swapapp.models;

public class User {
    private int userId;
    private String name;
    private String email;
    private int timeCredits;

    public User(int userId, String name, String email, int timeCredits) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.timeCredits = timeCredits;
    }

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getTimeCredits() { return timeCredits; }
}