package com.example.swapapp.models;

public class Session {
    private int sessionId;
    private int skillId;
    private int learnerId;
    private int teacherId;
    private String status;
    private String skillTitle; // NEW: To easily display the title in the UI

    public Session(int sessionId, int skillId, int learnerId, int teacherId, String status, String skillTitle) {
        this.sessionId = sessionId;
        this.skillId = skillId;
        this.learnerId = learnerId;
        this.teacherId = teacherId;
        this.status = status;
        this.skillTitle = skillTitle;
    }

    public int getSessionId() { return sessionId; }
    public int getSkillId() { return skillId; }
    public int getLearnerId() { return learnerId; }
    public int getTeacherId() { return teacherId; }
    public String getStatus() { return status; }
    public String getSkillTitle() { return skillTitle; }
}