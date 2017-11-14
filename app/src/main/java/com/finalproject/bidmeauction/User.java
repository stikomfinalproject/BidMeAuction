package com.finalproject.bidmeauction;

/**
 * Created by RAIIKA on 11/14/2017.
 */

public class User {

    String address, image, name, phone, pin, type;

    public User(){}

    public User(String address, String image, String name, String phone, String pin, String type) {
        this.address = address;
        this.image = image;
        this.name = name;
        this.phone = phone;
        this.pin = pin;
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
