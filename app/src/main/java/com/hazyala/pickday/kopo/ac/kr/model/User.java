package com.hazyala.pickday.kopo.ac.kr.model;


public class User {
    public String name;
    public String role;
    public String profileImageName;

    public User(String name, String role, String profileImageName) {
        this.name = name;
        this.role = role;
        this.profileImageName = profileImageName;
    }
}
