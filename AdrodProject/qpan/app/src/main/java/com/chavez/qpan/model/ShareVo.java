package com.chavez.qpan.model;

import java.util.List;

public class ShareVo {
    private List<String> uuids;
    private String passWord;

    public List<String> getUuids() {
        return uuids;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public void setUuids(List<String> uuids) {
        this.uuids = uuids;
    }
}
