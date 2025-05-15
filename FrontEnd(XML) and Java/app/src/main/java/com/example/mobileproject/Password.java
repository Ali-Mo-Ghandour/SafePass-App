package com.example.mobileproject;
public class Password {

    public int id;
    public String account;
    public String username;
    public String password; // Decrypted password
    //public String iv; // Encryption IV (used for decryption)
    //public String salt; // Salt (used for decryption)
    public String notes;
    public String createdAt;

    public Password(int id, String account, String username, String password, String notes, String createdAt) {
        this.id = id;
        this.account = account;
        this.username = username;
        this.password = password;

        this.notes = notes;
        this.createdAt = createdAt;
    }
}
