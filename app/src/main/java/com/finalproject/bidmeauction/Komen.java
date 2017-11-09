package com.finalproject.bidmeauction;

/**
 * Created by RAIIKA on 10/12/2017.
 */

public class Komen {

    private String desc, username;
    private Long waktu;

    public Komen(){

    }

    public Komen(String desc, String username, Long waktu) {
        this.desc = desc;
        this.username = username;
        this.waktu = waktu;
    }

    public Long getWaktu() {
        return waktu;
    }

    public void setWaktu(Long waktu) {
        this.waktu = waktu;
    }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
