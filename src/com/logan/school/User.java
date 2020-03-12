package com.logan.school;
import java.security.PublicKey;
import java.util.ArrayList;

public class User {
    private String userName;
    private PublicKey publicKey;
    private String serverURL;
    private int serverPort;

    public User(String userName, PublicKey publicKey, String serverURL, int serverPort){
        this.userName = userName;
        this.publicKey = publicKey;
        this.serverURL = serverURL;
        this.serverPort = serverPort;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
    public String getUserName(){
        return userName;
    }
    public String getServerURL() { return serverURL; }
    public int getServerPort() { return serverPort; }
}
