package com.cbtest.findadonortest.Model;

public class Request {

    String requestID,receiverId,senderId,tittle,description,timeStamp,reply,sate;

    public Request() {
    }

    public Request(String requestID, String receiverId, String senderId, String tittle, String description, String timeStamp, String reply, String sate) {
        this.requestID = requestID;
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.tittle = tittle;
        this.description = description;
        this.timeStamp = timeStamp;
        this.reply = reply;
        this.sate = sate;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getTittle() {
        return tittle;
    }

    public void setTittle(String tittle) {
        this.tittle = tittle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getSate() {
        return sate;
    }

    public void setSate(String sate) {
        this.sate = sate;
    }
}
