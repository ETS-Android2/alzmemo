package com.main.alz2.pojo;

public class Relation {

    private int relationId;
    private String description;
    private int userId;

    private String userFName;
    private String userMName;
    private String userLName;
    private String uBluetoothHandle;
    private String username;
    private String userType;

    private int relatedUserId;
    private String relatedUserFName;
    private String relatedUserMName;
    private String relatedUserLName;
    private String relatedBluetoothHandle;
    private String relatedUsername;
    private String relatedUserType;
    private Boolean isAccepted;

    public Relation(int relationId, String description, int userId, String userFName, String userMName, String userLName, String uBluetoothHandle, String username, String userType, int relatedUserId, String relatedUserFName, String relatedUserMName, String relatedUserLName, String relatedBluetoothHandle, String relatedUsername, String relatedUserType, Boolean isAccepted) {
        this.relationId = relationId;
        this.description = description;
        this.userId = userId;
        this.userFName = userFName;
        this.userMName = userMName;
        this.userLName = userLName;
        this.uBluetoothHandle = uBluetoothHandle;
        this.username = username;
        this.userType = userType;
        this.relatedUserId = relatedUserId;
        this.relatedUserFName = relatedUserFName;
        this.relatedUserMName = relatedUserMName;
        this.relatedUserLName = relatedUserLName;
        this.relatedBluetoothHandle = relatedBluetoothHandle;
        this.relatedUsername = relatedUsername;
        this.relatedUserType = relatedUserType;
        this.isAccepted = isAccepted;
    }

    public String getUserFName() {
        return userFName;
    }

    public void setUserFName(String userFName) {
        this.userFName = userFName;
    }

    public String getUserMName() {
        return userMName;
    }

    public void setUserMName(String userMName) {
        this.userMName = userMName;
    }

    public String getUserLName() {
        return userLName;
    }

    public void setUserLName(String userLName) {
        this.userLName = userLName;
    }

    public String getuBluetoothHandle() {
        return uBluetoothHandle;
    }

    public void setuBluetoothHandle(String uBluetoothHandle) {
        this.uBluetoothHandle = uBluetoothHandle;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Boolean getIsAccepted() {
        return isAccepted;
    }

    public void setIsAccepted(Boolean isAccepted) {
        this.isAccepted = isAccepted;
    }

    public String getRelatedUserType() {
        return relatedUserType;
    }

    public void setRelatedUserType(String relatedUserType) {
        this.relatedUserType = relatedUserType;
    }

    public int getRelationId() {
        return relationId;
    }

    public void setRelationId(int relationId) {
        this.relationId = relationId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRelatedUserId() {
        return relatedUserId;
    }

    public void setRelatedUserId(int relatedUserId) {
        this.relatedUserId = relatedUserId;
    }

    public String getRelatedUserFName() {
        return relatedUserFName;
    }

    public void setRelatedUserFName(String relatedUserFName) {
        this.relatedUserFName = relatedUserFName;
    }

    public String getRelatedUserMName() {
        return relatedUserMName;
    }

    public void setRelatedUserMName(String relatedUserMName) {
        this.relatedUserMName = relatedUserMName;
    }

    public String getRelatedUserLName() {
        return relatedUserLName;
    }

    public void setRelatedUserLName(String relatedUserLName) {
        this.relatedUserLName = relatedUserLName;
    }

    public String getRelatedBluetoothHandle() {
        return relatedBluetoothHandle;
    }

    public void setRelatedBluetoothHandle(String relatedBluetoothHandle) {
        this.relatedBluetoothHandle = relatedBluetoothHandle;
    }

    public String getRelatedUsername() {
        return relatedUsername;
    }

    public void setRelatedUsername(String relatedUsername) {
        this.relatedUsername = relatedUsername;
    }
}
