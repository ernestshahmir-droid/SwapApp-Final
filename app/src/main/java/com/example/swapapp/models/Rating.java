package com.example.swapapp.models;

public class Rating {
    private int ratingId;
    private int sessionId;
    private int reviewerId;
    private int revieweeId;
    private float score;
    private String comment;

    public Rating(int ratingId, int sessionId, int reviewerId, int revieweeId, float score, String comment) {
        this.ratingId = ratingId;
        this.sessionId = sessionId;
        this.reviewerId = reviewerId;
        this.revieweeId = revieweeId;
        this.score = score;
        this.comment = comment;
    }

    public float getScore() { return score; }
    public String getComment() { return comment; }
}