package com.study.user.dto;

public class LoginVerifyRequest {

    private String username;
    private String password;

    public LoginVerifyRequest() {}

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
}