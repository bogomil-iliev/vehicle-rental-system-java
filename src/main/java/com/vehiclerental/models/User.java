//This class represents a user of the vehicle rental system. Stores personal and contact details used for identification and communication.
package com.vehiclerental.models;

//It constructs a user with detailed personal information.
public class User {
    private String username;
    private String password;
    private String role;
    private String name;
    private String email;
    private String phone;
    private String address;

    public User(String username, String password, String role, String name, String email, String phone, String address) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    // Getters methods for details
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    // Setters methods for details.
    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
