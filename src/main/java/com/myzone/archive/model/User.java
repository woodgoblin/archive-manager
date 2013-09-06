package com.myzone.archive.model;

/**
 * @author myzone
 * @date 9/6/13 10:23 AM
 */
public class User {

    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

}
