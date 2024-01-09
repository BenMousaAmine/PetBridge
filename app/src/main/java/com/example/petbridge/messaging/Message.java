package com.example.petbridge.messaging;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Message {
    private String senderId ;
    private String reciverId ;
    private String textMsg ;
    @ServerTimestamp
    private Date timestamp ;
    private String reciverFullName ;
    private String  reciverProfileImage ;

    public Message() {
    }

    public Message(String senderId, String reciverId, String textMsg, Date timestamp) {
        this.senderId = senderId;
        this.reciverId = reciverId;
        this.textMsg = textMsg;
        this.timestamp = timestamp;
    }

    public Message(String senderId, String reciverId, String textMsg, Date timestamp, String reciverFullName, String reciverProfileImage, String reciverFullName1, String reciverProfileImage1) {
        this.senderId = senderId;
        this.reciverId = reciverId;
        this.textMsg = textMsg;
        this.timestamp = timestamp;
        this.reciverFullName = reciverFullName;
        this.reciverProfileImage = reciverProfileImage;
        this.reciverFullName = reciverFullName1;
        this.reciverProfileImage = reciverProfileImage1;
    }

    public String getReciverFullName() {
        return reciverFullName;
    }

    public void setReciverFullName(String reciverFullName) {
        this.reciverFullName = reciverFullName;
    }

    public String getReciverProfileImage() {
        return reciverProfileImage;
    }

    public void setReciverProfileImage(String reciverProfileImage) {
        this.reciverProfileImage = reciverProfileImage;
    }



    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReciverId() {
        return reciverId;
    }

    public void setReciverId(String reciverId) {
        this.reciverId = reciverId;
    }

    public String getTextMsg() {
        return textMsg;
    }

    public void setTextMsg(String textMsg) {
        this.textMsg = textMsg;
    }
    @ServerTimestamp
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
