package com.example.mjesto;

public class UserManager {

    private static UserManager userManagerInstance;

    private String user;
    private String parkLocationID;

    private UserManager() {
        user = null;
        parkLocationID = null;
    }

    public static synchronized UserManager getInstance() {
        if (userManagerInstance == null) {
            userManagerInstance = new UserManager();
        }
        return userManagerInstance;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getParkLocationID() {
        return parkLocationID;
    }

    public void setParkLocationID(String parkLocationID) {
        this.parkLocationID = parkLocationID;
    }

}
