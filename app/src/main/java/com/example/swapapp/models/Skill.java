package com.example.swapapp.models;

public class Skill {
    private int skillId;
    private int userId;
    private String title;
    private String category;
    private int durationHrs;

    // Constructor
    public Skill(int skillId, int userId, String title, String category, int durationHrs) {
        this.skillId = skillId;
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.durationHrs = durationHrs;
    }

    // Getters
    public int getSkillId() { return skillId; }
    public int getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public int getDurationHrs() { return durationHrs; }
}