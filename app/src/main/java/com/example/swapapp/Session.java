package com.example.swapapp;

public class Session {

    private int transactionId;
    private int requesterId;
    private int providerId;

    private String skillTitle;
    private String partnerName;
    private String status;

    public Session(
            int transactionId,
            int requesterId,
            int providerId,
            String skillTitle,
            String partnerName,
            String status
    ) {
        this.transactionId = transactionId;
        this.requesterId = requesterId;
        this.providerId = providerId;
        this.skillTitle = skillTitle;
        this.partnerName = partnerName;
        this.status = status;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public int getRequesterId() {
        return requesterId;
    }

    public int getProviderId() {
        return providerId;
    }

    public String getSkillTitle() {
        return skillTitle;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}