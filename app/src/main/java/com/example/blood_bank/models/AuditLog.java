package com.example.blood_bank.models;

public class AuditLog {
    private String logId;
    private String actorId;
    private String actorRole;
    private String action;
    private String entityId;
    private String timestamp;
    private String prevStatus;
    private String newStatus;
    private String details;

    public AuditLog(String logId, String actorId, String actorRole, String action, String entityId, String timestamp, String prevStatus, String newStatus, String details) {
        this.logId = logId;
        this.actorId = actorId;
        this.actorRole = actorRole;
        this.action = action;
        this.entityId = entityId;
        this.timestamp = timestamp;
        this.prevStatus = prevStatus;
        this.newStatus = newStatus;
        this.details = details;
    }

    public String getLogId() { return logId; }
    public String getActorId() { return actorId; }
    public String getActorRole() { return actorRole; }
    public String getAction() { return action; }
    public String getEntityId() { return entityId; }
    public String getTimestamp() { return timestamp; }
    public String getPrevStatus() { return prevStatus; }
    public String getNewStatus() { return newStatus; }
    public String getDetails() { return details; }
}
